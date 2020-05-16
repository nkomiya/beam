package com.examples.beam.chapter9;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.AfterPane;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.joda.time.Duration;

import java.util.Arrays;

/**
 * DiscardingFiredPane のコードサンプル
 */
public class DiscardingMode {
  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();

    // Pipeline Graph の構築
    pipeline
        .apply("Create Input items",
            Create.of(
                Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9"
                )).withCoder(StringUtf8Coder.of()))

        // 後で GroupByKey を使うため、KV型に変換
        .apply("To key value pair",
            MapElements.into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.strings()))
                .via(x -> KV.of("x", x)))

        // 要素が 3つ入った段階で処理を発火
        .apply("Apply windowing",
            Window.<KV<String, String>>into(FixedWindows.of(Duration.standardMinutes(10L)))
                .withAllowedLateness(Duration.ZERO)
                .triggering(
                    AfterWatermark.pastEndOfWindow()
                        .withEarlyFirings(
                            AfterPane.elementCountAtLeast(3))
                ).discardingFiredPanes())

        // window, trigger の結果を確認するため、GroupByKey を apply する
        .apply("Group by key `x`", GroupByKey.create())

        // Data-driven triggers では処理順序に保証がないため、出力の順序は毎回異なる。
        .apply(ParDo.of(
            new DoFn<KV<String, Iterable<String>>, Void>() {
              @ProcessElement
              public void method(ProcessContext ctx) {
                System.out.println(ctx.element().getValue().toString());
              }
            }
        ));

    // 実行
    pipeline.run();
  }
}
