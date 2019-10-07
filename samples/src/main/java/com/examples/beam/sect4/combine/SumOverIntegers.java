package com.examples.beam.sect4.combine;

import java.util.List;
import java.util.ArrayList;
// Beam SDK
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.BigEndianIntegerCoder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

/**
 * Built-inの集計関数を使うサンプル
 *
 * <p>整数のコレクションに対して総和を取る。</p>
 */
public class SumOverIntegers {
  public static void main(String[] args) {
    // input作成
    List<Integer> nums = new ArrayList<>(100);
    for (int i = 1; i <= 100; i++) nums.add(i);

    // PCollectionへ変換
    Pipeline pipeline = Pipeline.create();
    PCollection<Integer> input = pipeline
        .apply("CreateInputList",
            Create.of(nums).withCoder(BigEndianIntegerCoder.of()));

    // 総和
    PCollection<Integer> summed = input
        .apply("CalculateSummation", Sum.integersGlobally());

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
}
