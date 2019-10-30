[戻る](../coder.md)

```java
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.CannotProvideCoderException;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.coders.SerializableCoder;

import java.io.Serializable;

/**
 * CoderRegistryを使ったCoder指定のサンプル
 */
public class CoderRegistryExample {
  public static void main(String[] args) {
    // CoderRegistryの取得
    Pipeline pipeline = Pipeline.create();
    CoderRegistry cr = pipeline.getCoderRegistry();

    // クラスに対してDefaultのCoderを指定する。
    // ここをコメントアウトすると、getCoderで例外が発生する。
    cr.registerCoderForClass(
        MyCustomDataType.class,
        SerializableCoder.of(MyCustomDataType.class));

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
  private static class MyCustomDataType implements Serializable {
    public int a;
    public int b;
  }
}
```