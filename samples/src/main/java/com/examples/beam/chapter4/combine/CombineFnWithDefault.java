package com.examples.beam.chapter4.combine;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.BigEndianIntegerCoder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.Combine.CombineFn;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

import java.util.ArrayList;
import java.util.List;

/**
 * CombineFnを使ったサンプル
 *
 * <p>空のPCollectionをInputとした場合のデフォルト値を変更する</p>
 */
public class CombineFnWithDefault {
  public static void main(String[] args) {
    // 空のinput作成
    List<Integer> nums = new ArrayList<>(0);

    // PCollectionへ変換
    Pipeline pipeline = Pipeline.create();
    PCollection<Integer> input = pipeline
        .apply("CreateInputList",
            Create.of(nums).withCoder(BigEndianIntegerCoder.of()));

    // 平均の計算
    PCollection<Integer> average = input.apply("CalculateSummation",
        Combine.globally(new SummationOverIntegersFn()));

    // 型変換と出力
    average
        .apply("ConvertIntegerToString",
            MapElements.into(TypeDescriptors.strings())
                .via((Integer in) -> String.format("Summation: %d", in)))
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

    /**
     * InputのPCollectionが空の場合に返す値を指定
     *
     * @return デフォルト値 (-1)
     */
    @Override
    public Integer defaultValue() {
      return -1;
    }
  }
}
