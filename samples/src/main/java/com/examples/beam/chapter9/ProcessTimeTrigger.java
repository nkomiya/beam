package com.examples.beam.chapter9;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.windowing.AfterPane;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Trigger.OnceTrigger;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.joda.time.Duration;

/**
 * Process-time trigger のコードサンプル
 */
public class ProcessTimeTrigger {
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
    // Option と pipeline の作成
    MyOptions opt = PipelineOptionsFactory
        .fromArgs(args)
        .withValidation()
        .as(MyOptions.class);
    Pipeline pipeline = Pipeline.create(opt);

    // Process-time triggers を作成
    OnceTrigger processTimeTrigger = AfterProcessingTime.pastFirstElementInPane()
        .plusDelayOf(Duration.standardSeconds(30L));

    // graph 構築
    pipeline
        // Step 0: Pub/Sub subscription から読み込み
        .apply(PubsubIO.readStrings().fromSubscription(opt.getSubscription()))

        // Step 1: Window の設定
        //   -> 5分間隔の Fixed time windows を作成
        .apply("ApplyWindowing",
            Window.<String>into(FixedWindows.of(Duration.standardMinutes(5L)))
                // Watermark以降、30秒の遅延データを許す
                .withAllowedLateness(Duration.standardSeconds(30L))
                // Trigger の設定
                .triggering(
                    AfterWatermark.pastEndOfWindow()
                        // 処理の早期発火: 30秒間隔で処理を実行する
                        .withEarlyFirings(processTimeTrigger)
                        // 遅延データが届いたら、即発火
                        .withLateFirings(AfterPane.elementCountAtLeast(1))
                ).accumulatingFiredPanes())

        // Step 2: ファイル出力
        .apply("WriteToText", TextIO.write().to("output")
            .withWindowedWrites()
            .withNumShards(1));

    // 実行
    System.out.printf("\nWatching subscription `%s`\n", opt.getSubscription());
    pipeline.run();
  }
}
