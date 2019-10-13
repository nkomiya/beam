package com.examples.beam.sect4.requirements;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.BigEndianIntegerCoder;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;

/**
 * DoFnサブクラス内でのtransient修飾子の挙動を調べるコード
 */
public class checkTransient {
  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();

    // input作成
    PCollection<Integer> input = pipeline
        .apply(Create.of(1).withCoder(BigEndianIntegerCoder.of()));

    // transient修飾子のテスト
    input.apply(ParDo.of(new WithTransientFn()));

    pipeline.run();
  }

  /**
   * transientなメンバ変数を持つDoFnサブクラス
   */
  private static class WithTransientFn extends DoFn<Integer, Void> {
    private transient final Integer transientMember = 1;

    @ProcessElement
    public void method(@Element Integer in) {
      // ここではnullになってしまう。
      System.out.println(transientMember);
    }
  }
}
