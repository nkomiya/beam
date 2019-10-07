package com.examples.beam.sect4.combinePerkey;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.BigEndianIntegerCoder;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

import java.util.Arrays;
import java.util.List;

/**
 * KeyごとにCombineを行うサンプル
 */
public class CombineExample {
  public static void main(String[] args) {
    // input作成
    List<KV<String, Integer>> inputList =
        Arrays.asList(
            KV.of("cat", 1), KV.of("cat", 5), KV.of("cat", 9),
            KV.of("dog", 5), KV.of("dog", 2),
            KV.of("and", 1), KV.of("and", 2), KV.of("and", 6),
            KV.of("jump", 3),
            KV.of("tree", 2)
        );

    // inputのPCollection
    Pipeline pipeline = Pipeline.create();
    PCollection<KV<String, Integer>> input = pipeline
        .apply("CreateInputList",
            Create.of(inputList).withCoder(KvCoder.of(
                StringUtf8Coder.of(), BigEndianIntegerCoder.of())));

    // Combineで総和を取る
    PCollection<KV<String,Integer>> summed = input
        .apply("ApplyCombineTransform",
            Combine.perKey(new SumOver()));

    // Stringに変換し、ファイル出力
    summed
        .apply("ConvertIntegerToString",
            MapElements.into(TypeDescriptors.strings())
                .via((KV<String,Integer> in) -> String.format("%s: %d", in.getKey(),in.getValue())))
        .apply("WriteOutToText",
            TextIO.write().to("result").withoutSharding());

//    // 実行
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
