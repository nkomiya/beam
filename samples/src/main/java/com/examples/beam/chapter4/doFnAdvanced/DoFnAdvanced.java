package com.examples.beam.chapter4.doFnAdvanced;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.BigEndianIntegerCoder;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.joda.time.Instant;

/**
 * DoFnの仮引数で渡せるもののテスト
 */
public class DoFnAdvanced {
  /**
   * カスタムオプション
   */
  public interface MyOptions extends PipelineOptions {
    @Default.String("default-value")
    String getMyOpt();

    void setMyOpt(String s);
  }

  public static void main(String[] args) {
    // コマンドラインオプション付きでpipelineを作成
    PipelineOptions opt = PipelineOptionsFactory
        .fromArgs(args).withValidation().as(MyOptions.class);
    Pipeline pipeline = Pipeline.create(opt);

    pipeline
        .apply("CreateInput",
            Create.of(1).withCoder(BigEndianIntegerCoder.of()))
        .apply("ApplyParDo", ParDo.of(new MyDoFn()));

    // 実行
    pipeline.run();
  }

  /**
   * 検証用DoFn
   */
  private static class MyDoFn extends DoFn<Integer, Void> {
    @ProcessElement
    public void method(@Timestamp Instant t, PipelineOptions opt) {
      // Createで作ったものは、timestampはセットされない
      System.out.println(t.getMillis());
      System.out.println(Long.MIN_VALUE);

      // PipelineOptionsを復活させる
      MyOptions myopt = opt.as(MyOptions.class);
      System.out.println(myopt.getMyOpt());
    }
  }
}
