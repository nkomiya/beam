package com.examples.beam.chapter5.wildcard;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;

/**
 * ワイルドカードを使ったファイル読み込みのサンプル
 */
public class WildCardExample {
  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();

    // カレントディレクトリにある3文字のファイルを読み込み,
    // 読み込み結果をファイル出力する.
    pipeline
        .apply(TextIO.read().from("???"))
        .apply(TextIO.write().to("result").withoutSharding());

    pipeline.run();
  }
}
