package com.examples.beam.sect4.pardo;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;

import java.util.Arrays;
import java.util.List;

public class DoFnSubclass {
  public static void main(String[] args) {
    // pipeline作成
    Pipeline pipeline = Pipeline.create();

    // input
    List<String> input = Arrays.asList("hoge","fuga");

    // graph構築
    pipeline
        .apply(Create.of(input).withCoder(StringUtf8Coder.of()))
        .apply(ParDo.of(new MyFn()));

    // 実行
    pipeline.run();
  }

  /**
   * 文字数を返すDoFn
   */
  private static class MyFn extends DoFn<String, Integer> {
    MyFn() {
      System.out.println("Constructor was called !");
    }
    @ProcessElement
    public void method(@Element String input, OutputReceiver<Integer> o) {
      o.output(input.length());
    }
  }
}