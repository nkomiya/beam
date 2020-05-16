package com.examples.beam.chapter7;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.values.TypeDescriptors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 自作Coderのサンプル
 */
public class CustomCoder {
  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();

    // 合計値は265
    List<Integer> lst = Arrays.asList(1, 2, 3, 4, 5, 250);

    // シリアライズは1バイトしか行わない。そのため、
    //   256 -> 0
    //   257 -> 1 ...
    // のようになるため、9が出力される。
    pipeline
        .apply("CreateInput",
            Create.of(lst).withCoder(TinyIntegerCoder.of()))
        .apply("ApplyCombine", Sum.integersGlobally())
        .apply("ToString",
            MapElements.into(TypeDescriptors.strings()).via(x -> String.format("%d", x)))
        .apply("WriteToText",
            TextIO.write().to("result").withoutSharding());

    // 実行
    pipeline.run();
  }

  /**
   * 4バイト整数の下位1バイトのみをシリアライズするCoder
   */
  private static class TinyIntegerCoder extends Coder<Integer> {
    // インスタンスがsingletonになるようにする。
    private static final TinyIntegerCoder INSTANCE = new TinyIntegerCoder();

    private TinyIntegerCoder() {
    }

    // 必須ではないが、他のCoderと同様にof経由でインスタンス生成をさせる
    public static TinyIntegerCoder of() {
      return INSTANCE;
    }

    ////////////////////////////////////////////////////////////
    @Override
    public void encode(Integer v, OutputStream outStream)
        throws IOException {
      // 整数を byteに変換
      byte[] ba = new byte[]{v.byteValue()};
      new DataOutputStream(outStream).write(ba, 0, 1);
    }

    @Override
    public Integer decode(InputStream inStream)
        throws IOException {
      byte[] ba = new byte[1];
      int byteSize = new DataInputStream(inStream).read(ba, 0, 1);
      if (byteSize != 1) throw new IOException("Decode fail");

      return (int) ba[0];
    }

    /**
     * シリアル化が一意かどうかを検証するメソッド
     */
    @Override
    public void verifyDeterministic() throws NonDeterministicException {
    }

    /**
     * このCoderで使用されるCoderの一覧を返すメソッド
     *
     * <p>
     * このメソッドの使用例は、org.apache.beam.sdk.transforms.GroupIntoBatches
     * などをみると分かるかもです。
     * </p>
     *
     * @return 空のリスト
     */
    @Override
    public List<? extends Coder<?>> getCoderArguments() {
      return Collections.emptyList();
    }
  }
}
