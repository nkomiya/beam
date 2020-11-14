package com.examples.beam.chapter3;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;

/**
 * 最小構成の Pipeline 作成サンプル
 */
public class MinimumPipeline {
  /**
   * Pipeline 作成
   *
   * @param args コマンドライン引数
   */
  public static void main(String[] args) {
    // 実行時オプションをコマンドライン引数から取得
    PipelineOptions opt = PipelineOptionsFactory
        .fromArgs(args)
        .withValidation()
        .create();

    // オプション付き Pipeline インスタンスの作成
    Pipeline pipeline = Pipeline.create(opt);

    // オプションの参照
    System.out.println(opt.getRunner());
  }
}
