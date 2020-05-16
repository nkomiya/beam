package com.examples.beam.chapter8;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.MapCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.Sessions;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * Session windows のコードサンプル
 */
public class SessionWindows {
  /**
   * パイプライングラフの構築と実行
   *
   * @param args パイプラインの実行時引数
   */
  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();

    // graph作成
    pipeline
        .apply("Create Input items",
            Create.of(
                Arrays.asList(
                    getMap("2019/11/09-10:00:00", 0),
                    getMap("2019/11/09-10:01:00", 1),
                    getMap("2019/11/09-10:02:00", 2),

                    getMap("2019/11/09-10:05:00", 3),
                    getMap("2019/11/09-10:06:00", 4),
                    getMap("2019/11/09-10:07:00", 5)
                )).withCoder(MapCoder.of(StringUtf8Coder.of(), StringUtf8Coder.of())))

        // PCollection の要素にタイムスタンプを付与する。
        // 後で GroupByKey を使うため、KV型に変換しておく。
        .apply("Retrieve item and timestamp",
            ParDo.of(
                new DoFn<Map<String, String>, KV<String, String>>() {
                  @ProcessElement
                  public void method(ProcessContext ctx) {
                    DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy/MM/dd-HH:mm:ss");
                    dtf.withZone(DateTimeZone.forID("Asia/Tokyo"));

                    Map<String, String> m = ctx.element();
                    Instant ts = dtf.parseDateTime(m.get("time")).toInstant();

                    ctx.outputWithTimestamp(KV.of("x", m.get("item")), ts);
                  }
                }
            ))

        // Fixed time windows を適用
        .apply("Apply windowing",
            Window.<KV<String, String>>into(Sessions.withGapDuration(Duration.standardMinutes(3L))))

        // Window の効果を確認
        .apply("Group by key `x`", GroupByKey.create())
        .apply(ParDo.of(
            new DoFn<KV<String, Iterable<String>>, Void>() {
              @ProcessElement
              public void method(ProcessContext ctx) {
                System.out.println(ctx.element().getValue().toString());
              }
            }
        ))
    ;
    // 実行
    pipeline.run();
  }

  private static Map<String, String> getMap(String time, Integer item) {
    return new HashMap<String, String>() {{
      put("time", time);
      put("item", item.toString());
    }};
  }
}
