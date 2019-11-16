package com.examples.beam.chapter7;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.BigEndianIntegerCoder;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.joda.time.Instant;

import java.util.Arrays;

/**
 * Timestamp にアクセスするサンプル
 */
public class AddTimestamp {
  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();

    pipeline
        // インプットの作成
        .apply("Create Input",
            Create.of(Arrays.asList(1, 2, 3))
                .withCoder(BigEndianIntegerCoder.of()))

        // Timestampの確認と更新
        .apply("Access and update element timestamp",
            ParDo.of(
                new DoFn<Integer, Integer>() {
                  @ProcessElement
                  public void method(@Element Integer i,
                                     @Timestamp Instant t,
                                     OutputReceiver<Integer> out) {
                    // 更新前の timestamp を確認
                    System.out.printf("%d: %d\n", i, t.getMillis());
                    // 更新
                    out.outputWithTimestamp(i, Instant.ofEpochSecond(1546268400L));
                  }
                }))

        // Timestamp が更新されるか確認
        .apply("Check timestamp",
            ParDo.of(
                new DoFn<Integer, Void>() {
                  @ProcessElement
                  public void method(@Element Integer i, @Timestamp Instant t) {
                    // 更新後の timestamp を確認
                    System.out.printf("%d: %d\n", i, t.getMillis()/1000L);
                  }
                }
            ));

    // 実行
    pipeline.run();
  }
}
