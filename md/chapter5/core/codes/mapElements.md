[戻る](../pardo.md)

```java
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

import java.util.Arrays;
import java.util.List;

/**
 * MapElementsのサンプル
 */
public class MapElementsExample {
  /**
   * Graph構築と実行
   *
   * @param args Pipelineの実行時引数
   */
  public static void main(String[] args) {
    // input
    List<String> inputLst = Arrays.asList("hoge", "fuga");

    // inputのPCollection
    Pipeline pipeline = Pipeline.create();
    PCollection<String> input = pipeline
        .apply("CreateInput",
            Create.of(inputLst).withCoder(StringUtf8Coder.of()));

    // MapElementsのapply
    PCollection<String> length = input
        .apply("ApplyMapElements",
            MapElements.into(TypeDescriptors.strings()).via(
                (String s) -> String.format("%d", s.length())));

    // ファイル出力
    length
        .apply(TextIO.write().to("result").withoutSharding());

    // 実行
    pipeline.run();
  }
}
```