package com.examples.beam.chapter7;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

/**
 * SetCoderの挙動確認用のコード
 */
public class SetCoderExample {
  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();

    // PCollectionの作成
    PCollection<String> input = pipeline
        .apply("CreateInput", Create.of("hoge"));

    // transformのapply前は、Coderの変更が可能
    input.setCoder(StringUtf8Coder.of());

    // Coderの確認
    System.out.println(input.getCoder());

    // 何もしないtransform
    PCollection<String> pCollection = input
        .apply("DoNothing",
            MapElements.into(TypeDescriptors.strings()).via(x -> x));

    // transformのapply後にCoder指定をすると、graph構築の段階でビルドに失敗する
    input.setCoder(StringUtf8Coder.of());
  }
}
