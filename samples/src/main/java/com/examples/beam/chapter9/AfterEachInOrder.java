package com.examples.beam.chapter9;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.BigEndianIntegerCoder;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.AfterEach;
import org.apache.beam.sdk.transforms.windowing.AfterPane;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.IntervalWindow;
import org.apache.beam.sdk.transforms.windowing.Trigger;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.joda.time.Duration;

import java.util.Arrays;

public class AfterEachInOrder {
  public static void main(String[] args) {
    long WINDOW_DURATION = 5L;

    Pipeline pipeline = Pipeline.create();

    // trigger の作成
    Trigger trigger = AfterEach.inOrder(
        // 1. 3つ集まったら発火
        AfterPane.elementCountAtLeast(3),
        // 2. 5つ集まったら発火
        AfterPane.elementCountAtLeast(5),
        // 3. Watermark まで
        AfterWatermark.pastEndOfWindow()
    );

    // graph 構築
    pipeline
        // step 0: inputの作成
        .apply("CreateInput",
            Create.of(
                Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            ).withCoder(BigEndianIntegerCoder.of()))

        // step 1: window 変換
        .apply("ApplyWindowing",
            Window.<Integer>into(FixedWindows.of(Duration.standardMinutes(WINDOW_DURATION)))
                .withAllowedLateness(Duration.ZERO)
                .triggering(trigger).discardingFiredPanes())

        // step 2: key-value pair に変換
        .apply("Retrieve item and timestamp",
            MapElements.into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.strings()))
                .via(x -> KV.of("x", x.toString())))

        // step 3: GroupByKey を apply
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
    pipeline.run();
  }
}
