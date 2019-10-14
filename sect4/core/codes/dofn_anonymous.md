[戻る](../pardo.md)

```java
package com.examples.beam.sect4.pardo;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;

import java.util.Arrays;
import java.util.List;

/**
 * 匿名クラスとしてDoFnを使うサンプル
 */
public class DoFnAnonymous {
  /**
   * Graph構築と実行
   *
   * @param args 実行時オプション
   */
  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();

    // input
    List<String> input = Arrays.asList("hoge", "fuga");

    // graph
    pipeline
        .apply("CreateInput",
            Create.of(input).withCoder(StringUtf8Coder.of()))
        .apply("ApplyParDo",
            ParDo.of(
                new DoFn<String, Integer>() {
                  @ProcessElement
                  public void method(@Element String e, OutputReceiver<Integer> o) {
                    // 確認のため標準出力へ表示
                    System.out.println(e.length());

                    o.output(e.length());
                  }
                }));

    // 実行
    pipeline.run();
  }
}
```