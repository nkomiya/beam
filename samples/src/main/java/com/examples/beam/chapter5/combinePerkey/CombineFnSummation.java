package com.examples.beam.chapter5.combinePerkey;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.BigEndianIntegerCoder;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.Combine.CombineFn;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

import java.util.Arrays;
import java.util.List;

/**
 * CombineFnを使ったサンプル
 *
 * <p>整数のPCollectionに対し、総和の計算を行う</p>
 */
public class CombineFnSummation {
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

    // 平均の計算
    PCollection<KV<String, Integer>> average = input
        .apply("CalculateSummation",
            Combine.perKey(new SummationOverIntegersFn()));

    // 型変換と出力
    average
        .apply("ConvertIntegerToString",
            MapElements.into(TypeDescriptors.strings())
                .via((KV<String, Integer> in) -> String.format("%s: %d", in.getKey(), in.getValue())))
        .apply("WriteToText",
            TextIO.write().to("result").withoutSharding());

    // 実行
    pipeline.run();
  }

  /**
   * CombineFnを継承したサブクラス
   *
   * <p>型引数の意味はそれぞれ、Input、集計係、Output、である。</p>
   */
  private static class SummationOverIntegersFn
      extends CombineFn<Integer, Integer, Integer> {
    /**
     * 初期化された集計係を作成するメソッド
     *
     * @return 集計係
     */
    @Override
    public Integer createAccumulator() {
      return 0;
    }

    /**
     * 新規要素を集計の途中結果に加えるメソッド
     *
     * @param a 集計係
     * @param i 集計に加えられる新たな要素
     * @return 新規要素を追加した集計係
     */
    @Override
    public Integer addInput(Integer a, Integer i) {
      return a + i;
    }

    /**
     * 複数作られうる集計係をまとめるメソッド
     *
     * @param accumulators 集計係たち
     * @return 集計係をまとめた新たな集計係
     */
    @Override
    public Integer mergeAccumulators(Iterable<Integer> accumulators) {
      Integer merged = 0;
      for (Integer a : accumulators) merged += a;
      return merged;
    }

    /**
     * 集計結果を返すメソッド
     *
     * @param a 最終的な集計結果
     * @return 平均値
     */
    @Override
    public Integer extractOutput(Integer a) {
      return a;
    }
  }
}
