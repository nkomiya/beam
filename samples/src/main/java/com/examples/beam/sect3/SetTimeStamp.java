package com.examples.beam.sect3;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.WithTimestamps;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;

public class SetTimeStamp {
  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();

    // Pipeline graphの構築
    pipeline
        .apply("ReadFromFile", TextIO.read().from("input.txt"))
        .apply("SetTimestamp", WithTimestamps.of(new TimeStampFn()))
        .apply("DumpElements", ParDo.of(new MyFn()));

    pipeline.run();
  }

  private static class TimeStampFn implements SerializableFunction<String, Instant> {
    /**
     * PCollectionの各要素にTimeStampを付与するメソッド
     *
     * @param input ISO 8601のフォーマットに従う文字列
     * @return 入力文字列をパースしたInstant
     */
    @Override
    public Instant apply(String input) {
      // (c.f. https://www.w3.org/TR/NOTE-datetime)
      return Instant.parse(input);
    }
  }

  /**
   * TimeStamp確認用のDoFnサブクラス
   */
  private static class MyFn extends DoFn<String, Void> {
    /**
     * TimeStampを標準出力に表示する
     *
     * @param e パース前の文字列
     * @param t パース後のタイムスタンプ
     */
    @ProcessElement
    public void m(@Element String e, @Timestamp Instant t) {
      DateTimeZone jst = DateTimeZone.forID("Asia/Tokyo");
      System.out.println(
          DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:SS (ZZ)")
              .withZone(jst)
              .print(t));
    }
  }
}