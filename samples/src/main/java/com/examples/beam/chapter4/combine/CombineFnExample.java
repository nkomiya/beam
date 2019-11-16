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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * CombineFnを使ったサンプル
 *
 * <p>整数のPCollectionに対し、平均値の計算を行う</p>
 */
public class CombineFnExample {
  public static void main(String[] args) {
    // input作成
    List<Integer> nums = new ArrayList<>(100);
    for (int i = 1; i <= 100; i++) nums.add(i);

    // PCollectionへ変換
    Pipeline pipeline = Pipeline.create();
    PCollection<Integer> input = pipeline
        .apply("CreateInputList",
            Create.of(nums).withCoder(BigEndianIntegerCoder.of()));

    // 平均の計算
    PCollection<Double> average = input.apply("CalculateAverage",
        Combine.globally(new AverageOverIntegersFn()));

    // 型変換と出力
    average
        .apply("ConvertDoubleToString",
            MapElements.into(TypeDescriptors.strings())
                .via((Double in) -> String.format("Average: %.2f", in)))
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
  private static class AverageOverIntegersFn
      extends CombineFn<Integer, AverageOverIntegersFn.Accumulator, Double> {
    /**
     * 集計処理の途中結果を保持させるクラス
     *
     * <p>Serializeの継承、equalsのOverrideが必須</p>
     */
    private static class Accumulator implements Serializable {
      int sum;
      int items;

      /**
       * コンストラクタ
       */
      private Accumulator() {
        this.sum = 0;
        this.items = 0;
      }

      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Accumulator other = (Accumulator) o;
        return (this.sum == other.sum && this.items == other.items);
      }

      @Override
      public int hashCode() {
        final int prime = 31;
        int ret = 1;
        ret = ret * prime + sum;
        ret = ret * prime + items;
        return ret;
      }
    }

    /**
     * 集計用クラス Accumulator を作るメソッド
     *
     * @return 初期化されたAccumulator
     */
    @Override
    public Accumulator createAccumulator() {
      return new Accumulator();
    }

    /**
     * 新規要素を集計の途中結果に加えるメソッド
     *
     * @param a 集計係
     * @param i 集計に加えられる新たな要素
     * @return 新規要素を追加した集計係
     */
    @Override
    public Accumulator addInput(Accumulator a, Integer i) {
      a.sum += i;
      a.items++;
      return a;
    }

    /**
     * 複数作られうる集計係をまとめるメソッド
     *
     * @param accumulators 集計係たち
     * @return 集計係をまとめた新たな集計係
     */
    @Override
    public Accumulator mergeAccumulators(Iterable<Accumulator> accumulators) {
      Accumulator merged = new Accumulator();
      for (Accumulator a : accumulators) {
        merged.sum += a.sum;
        merged.items += a.items;
      }
      return merged;
    }

    /**
     * 集計結果を返すメソッド
     *
     * @param a 最終的な集計結果
     * @return 平均値
     */
    @Override
    public Double extractOutput(Accumulator a) {
      return (double) a.sum / a.items;
    }
  }
}
