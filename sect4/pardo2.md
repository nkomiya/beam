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

# ParDo - 発展編
## <span class="head">複数のPCollectionを返す</span>
今までの例だと、`ParDo`は一つの`PCollection`を返すだけでした。ですが、`ParDo`は任意の個数の`PCollection`をapplyの戻り値として返すことができるようです。
Outputの各`PCollection`は`CoGroupByKey`で出てきたtagで管理します。まずtagの作成についてですが、前と違って以下のように代入します。右辺でも型を明示的にしていすることで、型OutputTのCoderの推測をしてくれるっぽいです。"new TupleTag<>()"、みたいに省略すると実行時にエラーとなる場合があります。

```java=
TupleTag<OutputT> mytag = new TupleTag<OutputT>(){}; 
```

次に`DoFn`の実装ですが、複数の出力先を作ってあげるためにOutputReceiverを使わず、MultiOutputReceiverを使います。getメソッドを使うと、通常のOutputReceiverが取れます。

```java=
new DoFn<InputT,OutputT>() {
    @ProcessElement
    public void method(@Element InputT e, MultiOutputReceiver out) {
        // `tagName`でタグ付けするPCollectionへの出力
        out.get(mytag).output( ... )
    }
}
```

最後に`ParDo`のapplyですが、MultiOutputReceiverにtagを伝えてあげる必要があります。

```java=
apply( 
  ParDo.of( new DoFn<InputT,OutputT>(){
    ...
  }).withOutputTag(
    tag1,TupleTagList.of(tag2).and(tag3)... 
  )
)
```

tag1がメインのoutput、tag2, tag3, ...がサブのoutputになります。メインとサブの差は無い気がしますが...
applyの結果`PCollectionTuple`が得られますが、特定のtagを付けた`PCollection`が欲しければ、`PCollectionTuple`に対してgetメソッドを使えばokです。

code例として、名前・性別のkey/valueペアを持っていて、それを男女で分割してみます。このcode例だと単純な分割をするだけなので、`Partition`の方がはるかにラクに実装できる気はしますが...
（`PCollection`の要素と出力先が一対一対応でないときに便利なメソッドなのかと思ってます）

```java=
import java.util.Arrays;
import java.util.List;
// beam sdk
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.TupleTagList;

public class Main {
    public static void main(String[] args) {
        List<KV<String, String>> input = Arrays.asList(
                KV.of("taro", "m"),
                KV.of("hanako", "f"),
                KV.of("jiro", "m"),
                KV.of("yuka", "f")
        );

        // pipeline & input
        Pipeline pipeline = Pipeline.create();
        PCollection<KV<String, String>> members = pipeline.apply(Create.of(input));
        // tag作成
        final TupleTag<String> male = new TupleTag<String>() {};
        final TupleTag<String> female = new TupleTag<String>() {};
        //
        // 男女でPCollectionを分割
        PCollectionTuple splitted = members.apply(ParDo.of(
                new DoFn<KV<String, String>, String>() {
                    @ProcessElement
                    public void method(@Element KV<String, String> e, MultiOutputReceiver out) {
                        String name = e.getKey();
                        String gender = e.getValue();
                        if (gender.equals("m")) {
                            out.get(male).output(name);
                        } else if (gender.equals("f")) {
                            out.get(female).output(name);
                        }
                    }
                }).withOutputTags(male, TupleTagList.of(female)));

        // `male`でタグ付けしたPCollection
        splitted.get(male)
                .apply("male", TextIO.write().to("male").withoutSharding());
        // `female`でタグ付けしたPCollection
        splitted.get(female)
                .apply("female", TextIO.write().to("female").withoutSharding());
        //
        pipeline.run();
    }
}
```

## <span class="head">ProcessElementについて</span>
今までのcode例でProcessElementのアノテーションをつけたメソッドの仮引数はいつも同じ形、ってわけではありませんでした。
このメソッドの仮引数は、型やアノテーションで区別されるので、順番とか受け付けるものは柔軟にいじれます。詳しくは[ガイド](https://beam.apache.org/documentation/programming-guide/#additional-outputs)か、[beamのドキュメント](https://beam.apache.org/releases/javadoc/2.13.0/org/apache/beam/sdk/transforms/DoFn.ProcessElement.html)を参照してください。

+ Timestampアノテーション  
Inputの`PCollection`の各要素についているtimestampにアクセス可能です。
+ PipelineOptions  
`PipelineOptions`型の仮引数を渡しておくと、`DoFn`のサブクラス内でコマンドライン引数にアクセスできる。