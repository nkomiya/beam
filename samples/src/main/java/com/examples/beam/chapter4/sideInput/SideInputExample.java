package com.examples.beam.chapter4.sideInput;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.TypeDescriptors;

import java.util.Arrays;
import java.util.List;

/**
 * SideInputを使うサンプル
 */
public class SideInputExample {
  public static void main(String[] args) {
    // input text
    List<String> input = Arrays.asList(
        "To be, or not to be: that is the question: ",
        "Whether 'tis nobler in the mind to suffer ",
        "The slings and arrows of outrageous fortune, ",
        "Or to take arms against a sea of troubles, ");

    // inputのPCollection
    Pipeline pipeline = Pipeline.create();
    PCollection<String> txt = pipeline.apply("CreateInput",
        Create.of(input).withCoder(StringUtf8Coder.of()));

    // 文字数を数えて、viewに変換
    PCollectionView<Integer> tot = txt
        .apply("CountCharacters",
            MapElements.into(TypeDescriptors.integers()).via(String::length))
        .apply("SumOverCharacterNumbers",
            Sum.integersGlobally().asSingletonView());

    // 文字数の比率を計算して出力
    txt
        .apply("ConvertToRatio",
            ParDo.of(new ConvertToRatioFn(tot)).withSideInputs(tot))
        .apply("WriteToText",
            TextIO.write().to("result").withoutSharding());

    // 実行
    pipeline.run();
  }

  /**
   * SideInputを受け取るDoFnサブクラス
   */
  private static class ConvertToRatioFn extends DoFn<String, String> {
    private PCollectionView<Integer> totalCharCounts;

    /**
     * コンストラクタ
     *
     * @param v 全文字数を保持するPCollectionView
     */
    private ConvertToRatioFn(PCollectionView<Integer> v) {
      totalCharCounts = v;
    }

    @ProcessElement
    public void method(ProcessContext ctx, OutputReceiver<String> out) {
      // Inputの文字列
      String input = ctx.element();
      // 特定の行の文字数
      float wc = (float) input.length();
      // 全文字数をSide Inputから取得
      float total = (float) ctx.sideInput(totalCharCounts);

      out.output(String.format("%.2f%% : %s", wc * 100F / total, input));
    }
  }
}
