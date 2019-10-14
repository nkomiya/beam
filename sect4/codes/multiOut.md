[戻る](../pardoAdvanced.md)

```java
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.TupleTagList;
import org.apache.beam.sdk.values.TypeDescriptors;

import java.util.Arrays;
import java.util.List;

/**
 * MultiOutputReceiverのサンプル
 */
public class MultiOutExample {
  // tupleTag作成
  private static final TupleTag<String> nameTag = new TupleTag<String>() {{
  }};
  private static final TupleTag<Integer> lengthTag = new TupleTag<Integer>() {{
  }};

  public static void main(String[] args) {
    // inputの作成
    List<String> inputList = Arrays.asList(
        "taro", "jiro", "saburo", "siro","goro"
    );

    // InputのPCollectionを作成
    Pipeline pipeline = Pipeline.create();
    PCollection<String> input = pipeline
        .apply("CreateInput", Create.of(inputList).withCoder(StringUtf8Coder.of()));

    // 男女でPCollectionを分割
    PCollectionTuple split = input
        .apply("ApplyMultiOut", ParDo.of(new SplitFn())
            // 第一引数がmainの出力。DoFnにおけるOutputの型に合わせる必要がある。
            // 第二引数がsubの出力。andメソッドで出力先のタグを追加することができる。
            .withOutputTags(nameTag, TupleTagList.of(lengthTag)));

    // タグ経由で各PCollectionへアクセスする
    PCollection<String> name = split.get(nameTag);
    PCollection<Integer> nameLength = split.get(lengthTag);

    // 結果確認のため、ファイル出力
    name.apply("WriteNamesToFile", TextIO.write().to("main").withoutSharding());
    nameLength
        .apply(MapElements.into(TypeDescriptors.strings()).via(
            (Integer x) -> String.format("%d", x)))
        .apply("WriteNameLengthToFile", TextIO.write().to("sub").withoutSharding());

    // 実行
    pipeline.run();
  }

  /**
   * MultiOutputReceiverを使ったDoFnサブクラス
   */
  private static class SplitFn extends DoFn<String, String> {
    @ProcessElement
    public void method(@Element String e, MultiOutputReceiver o) {
      OutputReceiver<String> mainOut = o.get(nameTag);
      OutputReceiver<Integer> subOut = o.get(lengthTag);
      // 出力
      mainOut.output(e);
      subOut.output(e.length());
    }
  }
}
```