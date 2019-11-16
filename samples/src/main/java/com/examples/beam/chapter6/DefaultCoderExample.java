package com.examples.beam.chapter6;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.CannotProvideCoderException;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.coders.SerializableCoder;

import java.io.Serializable;

/**
 * DefaultCoderアノテーションを使ったサンプル
 */
public class DefaultCoderExample {
  public static void main(String[] args) {
    // CoderRegistryの取得
    Pipeline pipeline = Pipeline.create();
    CoderRegistry cr = pipeline.getCoderRegistry();

    try {
      System.out.println(cr.getCoder(MyCustomDataType.class));
    } catch (CannotProvideCoderException e) {
      System.out.println("Cannot provide coder for class MyCustomDataType");
    }
  }

  /**
   * SerializableCoderを指定するカスタムクラス
   *
   * <p>Serializableのimplementが必要</p>
   */
  @DefaultCoder(SerializableCoder.class)
  private static class MyCustomDataType implements Serializable {
    public int a;
    public int b;
  }
}
