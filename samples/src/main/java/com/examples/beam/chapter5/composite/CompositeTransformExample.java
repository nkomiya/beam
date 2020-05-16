package com.examples.beam.chapter5.composite;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

/**
 * Composite Transformのサンプル
 *
 * <p>外部ファイル内の文字数をカウントし、ファイル出力する。</p>
 */
public class CompositeTransformExample {
  /**
   * カスタムオプション
   *
   * <p>読み込むファイル名を受け取る</p>
   */
  public interface MyOptions extends PipelineOptions {
    @Default.String("input.txt")
    String getInputFile();

    void setInputFile(String s);
  }

  /**
   * Pipeline Graphの構築と実行
   *
   * @param args パイプラインの実行時引数
   */
  public static void main(String[] args) {
    // 実行時オプション込みでPipelineインスタンスを作成
    MyOptions opt = PipelineOptionsFactory
        .fromArgs(args).withValidation().as(MyOptions.class);
    Pipeline pipeline = Pipeline.create(opt);

    // Graph構築
    pipeline
        .apply(new CharCountTransform(opt.getInputFile()))
        .apply(TextIO.write().to("result").withoutSharding());

    // 実行
    pipeline.run();
  }

  /**
   * ファイル内の総文字数を返すPTransform
   */
  private static class CharCountTransform extends
      PTransform<PBegin, PCollection<String>> {
    // 読み込むファイル名
    private String inputFile;

    /**
     * コンストラクタ
     *
     * @param inputFile 読み込むファイル名
     */
    private CharCountTransform(String inputFile) {
      this.inputFile = inputFile;
    }

    /**
     * 実処理を行うメソッド
     *
     * @param input PBegin
     * @return 総文字数を文字列にしたもの
     */
    @Override
    public PCollection<String> expand(PBegin input) {
      return input
          .apply("ReadFromText", TextIO.read().from(inputFile))
          .apply("CountCharacters",
              MapElements.into(TypeDescriptors.integers()).via(String::length))
          .apply("SumOverCharCounts", Sum.integersGlobally())
          .apply("ConvertToString",
              MapElements.into(TypeDescriptors.strings()).via((Integer i) -> String.format("%d", i)));
    }
  }
}
