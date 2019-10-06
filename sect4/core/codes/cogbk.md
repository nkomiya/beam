[戻る](../cogroupbykey.md)

```java
import java.util.List;
import java.util.Arrays;
// Beam SDK
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.transforms.join.CoGroupByKey;
import org.apache.beam.sdk.transforms.join.KeyedPCollectionTuple;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TupleTag;

public class CoGroupByKeyExample {
  // 右辺で明示的に型を指定しない場合、Coderの推定に失敗することがある
  private static final TupleTag<String> emailTag = new TupleTag<String>() {{
  }};
  private static final TupleTag<String> phoneTag = new TupleTag<String>() {{
  }};

  public static void main(String[] args) {
    // carlが重複
    final List<KV<String, String>> emailList =
        Arrays.asList(
            KV.of("amy", "amy@example.com"),
            KV.of("carl", "carl@example.com"),
            KV.of("julia", "julia@example.com"),
            KV.of("carl", "carl@example.mail.com")
        );
    // amyが重複
    final List<KV<String, String>> phoneList =
        Arrays.asList(
            KV.of("amy", "123-4567-8901"),
            KV.of("james", "111-2222-3333"),
            KV.of("amy", "000-123-1234"),
            KV.of("carl", "777-8888-9999")
        );

    // graph構築
    Pipeline pipeline = Pipeline.create();
    PCollection<KV<String, String>> emails = pipeline.apply("CreateEmailList",
        Create.of(emailList).withCoder(KvCoder.of(StringUtf8Coder.of(), StringUtf8Coder.of())));
    PCollection<KV<String, String>> phones = pipeline.apply("CreatePhoneList",
        Create.of(phoneList).withCoder(KvCoder.of(StringUtf8Coder.of(), StringUtf8Coder.of())));

    // PCollectionのペアを作ってから結合
    KeyedPCollectionTuple<String> keyedPCollectionTuple =
        KeyedPCollectionTuple.of(emailTag, emails).and(phoneTag, phones);

    // CoGroupByKey
    PCollection<KV<String, CoGbkResult>> cogbkResult =
        keyedPCollectionTuple.apply("JoinTwoSource",CoGroupByKey.create());

    cogbkResult
        .apply(ParDo.of(
            new DoFn<KV<String, CoGbkResult>, String>() {
              @ProcessElement
              public void formatter(ProcessContext ctx) {
                KV<String, CoGbkResult> e = ctx.element();
                String key = e.getKey();
                CoGbkResult value = e.getValue();

                // output用
                StringBuilder result = new StringBuilder();

                // keyの取得
                result.append(String.format("Name: %s\n", key));

                // CoGroupByKeyのValueには、tagを使ってアクセスする
                // email
                result.append("  - emails:\n");
                for (String em : value.getAll(emailTag))
                  result.append(String.format("    - %s\n", em));

                // phone
                result.append("  - phones:\n");
                for (String ph : value.getAll(phoneTag))
                  result.append(String.format("    - %s\n", ph));

                ctx.output(result.toString());
              }
            }))
        .apply(TextIO.write().to("result").withoutSharding());
    ;
    pipeline.run();
  }
}
```