package com.examples.beam.chapter5.combinePerkey;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.BigEndianIntegerCoder;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

import java.util.Arrays;
import java.util.List;


/**
 * Built-inの集計関数を使うサンプル
 *
 * <p>整数のコレクションに対して総和を取る。</p>
 */
public class SumOverIntegers {
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
    // 総和
    PCollection<KV<String, Integer>> summed = input
        .apply("CalculateSummation", Sum.integersPerKey());

    // Stringに変換し、ファイル出力
    summed
        .apply("ConvertIntegerToString",
            MapElements.into(TypeDescriptors.strings())
                .via((KV<String, Integer> in) -> String.format("%s: %d", in.getKey(), in.getValue())))
        .apply("WriteOutToText",
            TextIO.write().to("result").withoutSharding());

    // 実行
    pipeline.run();
  }
}
