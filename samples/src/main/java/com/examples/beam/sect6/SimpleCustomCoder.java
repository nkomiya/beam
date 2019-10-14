package com.examples.beam.sect6;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.BigEndianIntegerCoder;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.CoderException;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.Combine.CombineFn;
import org.apache.beam.sdk.values.TypeDescriptors;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * 自作Coderのサンプル
 */
public class SimpleCustomCoder {
  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();

    List<MyCustomData> lst = Arrays.asList(
        new MyCustomData(1, 2),
        new MyCustomData(3, 4),
        new MyCustomData(5, 6));

    pipeline
        .apply("CreateInput",
            Create.of(lst).withCoder(MyCoder.of()))
        .apply("ApplyCombine",
            Combine.globally(new MyCombineFn()))
        .apply("ToString",
            MapElements.into(TypeDescriptors.strings()).via(
                (MyCustomData d) -> String.format("%d %d", d.a, d.b)))
        .apply(TextIO.write().to("result").withoutSharding());

    // 実行
    pipeline.run();
  }

  /**
   * Coderを設定するカスタムクラス
   */
  private static class MyCustomData implements Serializable {
    public int a;
    public int b;

    private MyCustomData(int a, int b) {
      this.a = a;
      this.b = b;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      MyCustomData other = (MyCustomData) o;
      return (this.a == other.a && this.b == other.b);
    }
  }

  /**
   * MyCustomData用のCoderサブクラス
   */
  private static class MyCoder extends Coder<MyCustomData> {
    // singletonの作成
    private final static MyCoder INSTANCE = new MyCoder();
    // Beam SDKのCoderを使う
    private final Coder<Integer> aCoder;
    private final Coder<Integer> bCoder;

    private MyCoder() {
      this.aCoder = BigEndianIntegerCoder.of();
      this.bCoder = BigEndianIntegerCoder.of();
    }

    public static MyCoder of() {
      return INSTANCE;
    }

    //////////////////////////////////////////////////
    @Override
    public void encode(MyCustomData value, OutputStream outStream)
        throws CoderException, IOException {
      // decodeする順序と合わせる必要がある
      aCoder.encode(value.a, outStream);
      bCoder.encode(value.b, outStream);
    }

    @Override
    public MyCustomData decode(InputStream inStream)
        throws CoderException, IOException {
      // encodeの順序と合わせる必要がある
      int a = aCoder.decode(inStream);
      int b = bCoder.decode(inStream);
      return new MyCustomData(a, b);
    }

    @Override
    public List<? extends Coder<?>> getCoderArguments() {
      // 外部でこのCoderサブクラスを使う際に,
      // MyCoder.getCoderArguments.get(0)とかでCoderを取得できるようにする。
      // encode / decodeの順序に合わせるのが普通。
      // org.apache.beam.sdk.transforms.GroupIntoBatchesが参考になります。
      return Arrays.asList(aCoder, bCoder);
    }

    @Override
    public void verifyDeterministic() throws NonDeterministicException {
      // シリアル化が一意であることを保証するために使うメソッド
      // 実装はさぼります。
    }
  }

  /**
   * Coderが上手く指定できていることを確認するためのCombineFnサブクラス
   */
  private static class MyCombineFn extends
      CombineFn<MyCustomData, MyCustomData, MyCustomData> {
    @Override
    public MyCustomData createAccumulator() {
      return new MyCustomData(0, 0);
    }

    @Override
    public MyCustomData addInput(MyCustomData acc, MyCustomData in) {
      return new MyCustomData(acc.a + in.a, acc.b + in.b);
    }

    @Override
    public MyCustomData mergeAccumulators(Iterable<MyCustomData> accumulators) {
      MyCustomData ret = new MyCustomData(0, 0);
      for (MyCustomData acc : accumulators) {
        ret.a += acc.a;
        ret.b += acc.b;
      }
      return ret;
    }

    @Override
    public MyCustomData extractOutput(MyCustomData acc) {
      return acc;
    }
  }
}