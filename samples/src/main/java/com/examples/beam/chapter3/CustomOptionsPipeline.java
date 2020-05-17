package com.examples.beam.chapter3;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation;

/**
 * ユーザ定義オプションを使った Pipeline 作成サンプル
 *
 * <p>オプション一覧</p>
 * <ul>
 *   <li>--option1: 必須オプション</li>
 *   <li>--option2: 任意オプション、default は 'default_value'</li>
 * </ul>
 */
public class CustomOptionsPipeline {

  /** ユーザ定義 オプション */
  public interface CustomOptions extends PipelineOptions {
    /**
     * option1 の getter
     *
     * @return option1 の値
     */
    @Description("Description for option 1")
    @Validation.Required
    String getOption1();

    void setOption1(String s);

    /**
     * option2 の getter
     *
     * @return option2 の値
     */
    @Default.String("default_value")
    @Description("Description for option 2")
    String getOption2();

    void setOption2(String s);
  }

  /**
   * Pipeline 作成
   *
   * @param args コマンドライン引数
   */
  public static void main(String[] args) {
    PipelineOptionsFactory.register(CustomOptions.class);
    CustomOptions opt = PipelineOptionsFactory
        .fromArgs(args)
        .withValidation()
        .as(CustomOptions.class);

    Pipeline pipeline = Pipeline.create(opt);
    System.out.printf("option1=%s\n", opt.getOption1());
    System.out.printf("option2=%s\n", opt.getOption2());
  }
}
