<style type="text/css">
  .head { 
    border-left:5px solid #00f;
    padding:3px 0 3px 10px;
    font-weight: bold;
  }
  .lhead { 
    border-left:5px solid #00f;
    padding:3px 0 3px 10px;
    font-size:14pt;
    font-weight: bold;
  }
</style>
[topへ](../index.html)

# CoGroupByKey
`CoGroupByKey`は複数の`KV`型の`PCollection`をまとめるためのメソッド。
![multi-source](https://beam.apache.org/images/design-your-pipeline-join.png)

想定としては同じタイプのkeyを持つ複数のsource（dataに関連のある複数のsource、みたいな）があって、それらを結合して一つにしたい...って感じ？

例として挙げられているのは
1. 名前とemailのdataset
2. 名前と電話番号のdataset

みたいな。名前をkeyに、valueを{email:"hoge@example.com", phone:"090-XXXX-XXXX"} みたいなmapにしたくなります。

`CoGroupByKey`では`TupleTag`、というものを使います。各`PCollection`をタグ付けしておいて、アクセスの際はタグ経由になります。

`TupleTag`の作り方はこんな感じで、まとめたいkev/valueペアにおける、valueの型を指定してあげる。

```java=
import org.apache.beak.sdk.values.TupleTag;
TupleTag<valueの型> tag = new TupleTag<>();
```

`TupleTag`を作った後は、`PCollection`を結合して、`KeyedPCollectionTuple<T>`を作る。これは、同じ型の**key**を持つ`KV`型の`PCollection`を、一つの`KV`型の`PCollection`にまとめるもの。
各`PCollection`にはタグを付けられ、結合後も一つの`PCollection`にアクセスできる。

二つの`KV`型の`PCollection`を結合するときの`KeyedPCollectionTuple`の使い方はこんな感じ。

```java=
import org.apache.beam.sdk.transform.join.KeyedPCollectionTuple;
PCollection<KV<K,V1>> p1 = ...
PCollection<KV<K,V2>> p1 = ...
KeyedPCollectionTuple.of( p1tag,p1 ).and( p2tag,p2 );
```

`K`がkeyの型、`V1`, `V2`がvalueの型。`KeyedPCollectionTuple<K>`が返ってくるから、変数に格納してもいいし、`apply`を作用させてってもいい。

長々やってきましたが、あとは`KeyedPCollectionTuple`に`CoGroupByKey`をapplyすればおしまいです。

```java=
apply(CoGroupByKey.create())
```

Keyでひとまとめにする、という意味では`GroupByKey`と同じですが、違いとして思っているのは

- `CoGroupByKey`では結合前の`PCollection`の情報を保持  
結合後でもemailだけ知りたい、みたいなことができる
- 型が違うvalueも結合できる  
たぶん`PCollection`の型指定では、`<? extends Object>`みたいな境界型の型引数は（たぶん）とれないし、やらない方がいい気がします...



例でだした、emailと住所の動くやつ。長い。。。
ただ、`PCollection`を作成、import、出力のための整形が大半です。。。

```java=
import java.util.List;
import java.util.Arrays;
// pipeline
import org.apache.beam.sdk.Pipeline;
// types
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
// transform
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.io.TextIO;
// for CoGroupByKey
import org.apache.beam.sdk.transforms.join.CoGroupByKey;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.transforms.join.KeyedPCollectionTuple;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
//
public class Main {
    public static void main(String[] args) {
        // 二つのdatasetで、片方にしかないkeyがあってもok
        // email, 重複あり
        final List<KV<String, String>> elist =
                Arrays.asList(
                        KV.of("amy", "amy@example.com"),
                        KV.of("carl", "carl@example.com"),
                        KV.of("julia", "julia@example.com"),
                        KV.of("carl", "carl@example.mail.com")
                );
        // phone, 重複あり
        final List<KV<String, String>> plist =
                Arrays.asList(
                        KV.of("amy", "123-4567-8901"),
                        KV.of("james", "111-2222-3333"),
                        KV.of("amy", "000-123-1234"),
                        KV.of("carl", "777-8888-9999")
                );
        //
        // pipeline と pcollectionの作成
        Pipeline pipeline = Pipeline.create();
        PCollection<KV<String, String>> emails = pipeline.apply("CreateEmailList", Create.of(elist));
        PCollection<KV<String, String>> phones = pipeline.apply("CreatePhoneList", Create.of(plist));
        //
        // tag作成. CoGroupByKeyにも必要になるけど、
        // Group化の結果にアクセスするのにも使える
        final TupleTag<String> emailTag = new TupleTag<>();
        final TupleTag<String> phoneTag = new TupleTag<>();
        // PCollectionのペアを作ってから結合
        KeyedPCollectionTuple.of(emailTag, emails).and(phoneTag, phones)
                .apply("JoinTwoSource", CoGroupByKey.create())
                .apply(ParDo.of(
                        new DoFn<KV<String, CoGbkResult>, String>() {
                            @ProcessElement
                            public void formatter(ProcessContext ctx) {
                                KV<String, CoGbkResult> e = ctx.element();
                                String result = e.getKey();
                                // tagでアクセス
                                result += "\n  email:";
                                for (String em : e.getValue().getAll(emailTag)) {
                                    result += String.format("  %s", em);
                                }
                                result += "\n  phone:";
                                for (String ph : e.getValue().getAll(phoneTag)) {
                                    result += String.format("  %s", ph);
                                }
                                ctx.output(result);
                            }
                        }))
                .apply(TextIO.write().to("result"));
        //
        pipeline.run();
    }
}
```