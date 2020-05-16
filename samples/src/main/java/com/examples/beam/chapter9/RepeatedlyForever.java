package com.examples.beam.chapter9;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.AfterPane;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.IntervalWindow;
import org.apache.beam.sdk.transforms.windowing.Repeatedly;
import org.apache.beam.sdk.transforms.windowing.Trigger;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.joda.time.Duration;

/**
 * Repeatedly.forever のコードサンプル
 */
public class RepeatedlyForever {
  /**
   * 実行時オプション
   */
  public interface MyOptions extends PipelineOptions {
    @Description("Pub/Sub subscription name")
    String getSubscription();

    void setSubscription(String s);
  }

  /**
   * Pipeline Graph 構築と実行
   *
   * @param args パイプラインの実行時引数
   */
  public static void main(String[] args) {
    long WINDOW_DURATION = 5L;

    // Option と pipeline の作成
    MyOptions opt = PipelineOptionsFactory
        .fromArgs(args)
        .withValidation()
        .as(MyOptions.class);
    Pipeline pipeline = Pipeline.create(opt);

    // trigger の作成
    Trigger trigger = Repeatedly.forever(
        AfterProcessingTime.pastFirstElementInPane()
            .plusDelayOf(Duration.standardSeconds(30L))
    ).orFinally(AfterPane.elementCountAtLeast(5));

    // graph 構築
    pipeline
        // Step 0: Pub/Sub subscription から読み込み
        .apply("ReadFromSubscription",
            PubsubIO.readStrings().fromSubscription(opt.getSubscription()))

        // Step 1: Window の設定
        //   -> 5分間隔の Fixed time windows を作成
        .apply("ApplyWindowing",
            Window.<String>into(FixedWindows.of(Duration.standardMinutes(WINDOW_DURATION)))
                // Watermark以降、30秒の遅延データを許す
                .withAllowedLateness(Duration.standardSeconds(30L))
                // 遅延データを取り扱えるよう、デフォルトの trigger の構成から変更
                .triggering(trigger).accumulatingFiredPanes())

        // Step 2: key-value pair に変換
        .apply("Retrieve item and timestamp",
            MapElements.into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.strings()))
                .via(x -> KV.of("x", x)))

        // Step 3: group by key で Pane ごとに分解
        .apply(GroupByKey.create())

        // step 4: 標準出力に書き出し
        .apply(ParDo.of(
            new DoFn<KV<String, Iterable<String>>, Void>() {
              @ProcessElement
              public void method(ProcessContext ctx, IntervalWindow window) {
                System.out.println(
                    String.format("Window: %s\nItems : %s\n",
                        window.toString(), ctx.element().getValue().toString()));
              }
            }
        ));

    // 実行
    System.out.printf("\nWatching subscription `%s`\n", opt.getSubscription());
    pipeline.run();
  }
}
