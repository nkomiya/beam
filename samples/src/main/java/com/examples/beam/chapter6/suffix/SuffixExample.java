package com.examples.beam.chapter6.suffix;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;

/**
 * 分割されたファイル出力で、suffixを指定するサンプル
 */
public class SuffixExample {
  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();
    pipeline
        .apply("CreateInput",
            Create.of("test").withCoder(StringUtf8Coder.of()))
        .apply("WriteToText",
            // withSuffix(...)で拡張子が指定可能
            TextIO.write().to("result").withSuffix(".txt"));

    pipeline.run();
  }
}
