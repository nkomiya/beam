package com.examples.beam.chapter4.combine;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.BigEndianIntegerCoder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

import java.util.ArrayList;
import java.util.List;

/**
 * Combine transformを使ったコードサンプル
 */
public class CombineExample {
  public static void main(String[] args) {
    // input作成
    List<Integer> nums = new ArrayList<>(100);
    for (int i = 1; i <= 100; i++) nums.add(i);

    // inputのPCollection
    Pipeline pipeline = Pipeline.create();
    PCollection<Integer> input = pipeline
        .apply("CreateInputList",
            Create.of(nums).withCoder(BigEndianIntegerCoder.of()));

    // Combineで総和を取る
    PCollection<Integer> summed = input
        .apply("ApplyCombineTransform",
            Combine.globally(new SumOver()));

    // Stringに変換し、ファイル出力
    summed
        .apply("ConvertIntegerToString",
            MapElements.into(TypeDescriptors.strings())
                .via((Integer in) -> String.format("%d", in)))
        .apply("WriteOutToText",
            TextIO.write().to("result").withoutSharding());

    // 実行
    pipeline.run();
  }

  /**
   * Combine transformで行う結合処理を定義
   */
  private static class SumOver implements
      SerializableFunction<Iterable<Integer>, Integer> {
    /**
     * 行われる処理を記載したメソッド
     *
     * @param in inputの整数がiterableに変換されたもの
     * @return 総計
     */
    @Override
    public Integer apply(Iterable<Integer> in) {
      Integer s = 0;
      for (Integer n : in) s += n;
      return s;
    }
  }
}
