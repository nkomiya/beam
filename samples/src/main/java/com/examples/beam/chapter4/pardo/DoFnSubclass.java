package com.examples.beam.chapter4.pardo;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;

import java.util.Arrays;
import java.util.List;

/**
 * DoFnのサンプル
 */
public class DoFnSubclass {
  /**
   * Graph構築と実行
   *
   * @param args Pipelineの実行時引数
   */
  public static void main(String[] args) {
    // pipeline作成
    Pipeline pipeline = Pipeline.create();

    // input
    List<String> input = Arrays.asList("hoge", "fuga");

    // graph構築
    pipeline
        .apply("CreateInput",
            Create.of(input).withCoder(StringUtf8Coder.of()))
        .apply("ApplyParDo", ParDo.of(new MyFn()));

    // 実行
    pipeline.run();
  }

  /**
   * 文字数を返すDoFn
   */
  private static class MyFn extends DoFn<String, Integer> {
    /**
     * 実処理を行うメソッド
     *
     * @param input 任意の文字列
     * @param o     文字数
     */
    @ProcessElement
    public void method(@Element String input, OutputReceiver<Integer> o) {
      int len = input.length();
      // 確認のため標準出力
      System.out.println(len);

      o.output(len);
    }
  }
}