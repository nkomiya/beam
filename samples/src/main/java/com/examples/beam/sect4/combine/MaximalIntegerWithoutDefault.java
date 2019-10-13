package com.examples.beam.sect4.combine;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.BigEndianIntegerCoder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.Max;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

import java.util.ArrayList;
import java.util.List;

/**
 * Built-inの集計関数を使うサンプル
 *
 * <p>整数のコレクションに対して総和を取る。</p>
 */
public class MaximalIntegerWithoutDefault {
  public static void main(String[] args) {
    // input作成
    List<Integer> nums = new ArrayList<>(0);

    // PCollectionへ変換
    Pipeline pipeline = Pipeline.create();
    PCollection<Integer> input = pipeline
        .apply("CreateInputList",
            Create.of(nums).withCoder(BigEndianIntegerCoder.of()));

    // 総和
    // withoutDefaultをつけると、出力が空になる。
    PCollection<Integer> summed = input
        .apply("CalculateMaximum", Max.integersGlobally().withoutDefaults());

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
