[topへ](../index.md)  
[PTransformの概要へ](./ptransform.md)

# ParDo - 発展編
## <span class="head">複数のPCollectionを返す</span>
`ParDo`は、実は複数の`PCollection`を出力として返すことができます。複数の出力先を管理するにあたり、[以前](./core/cogroupbykey.md)使用した`TupleTag`を使用します。

```java
TupleTag<T> mytag = new TupleTag<T>(){{
}}; 
```

次に`DoFn`ですが、複数の出力先を作るためにOutputReceiverを使わず、MultiOutputReceiverを使います。getメソッドを使うと、通常のOutputReceiverが取れます。

```java
class DoFn<InputT, OutputT>() {
  @ProcessElement
  public void method(@Element InputT e, MultiOutputReceiver o) {
    OutputReceiver<OutputT> out = o.get(mytag);
    // ... 中略 ...
  }
}
```

最後に`ParDo`のapplyですが、MultiOutputReceiverにtagを伝えてあげる必要があります。

```java
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTagList;

PCollectionTuple pCollectionTuple = pCollectionTuple
    .apply(ParDo.of(...)
        .withOutputTag(tag1,TupleTagList.of(tag2).and(tag3)...));
```

tag1がメインのoutputで、`DoFn`の出力型に合わせる必要があります。tag2, tag3, ...がサブのoutputで型は自由です。

applyの結果`PCollectionTuple`が得られますが、tupleTag経由で特定の`PCollection`を取り出せます。

```java
PCollection<T> pCollection = pCollectionTuple.get(tag1);
```

コードサンプルは[こちら](./codes/multiOut.md)です。

## <span class="head">ProcessElementについて</span>
`DoFn`でProcessElementのアノテーションをつけたメソッドの仮引数はいつも同じではありませんでした。このメソッドの仮引数は、型やアノテーションで区別されるので、順番や受け付けるものは柔軟にいじれます。詳しくは[ガイド](https://beam.apache.org/documentation/programming-guide/#additional-outputs)か、[Beamのドキュメント](https://beam.apache.org/releases/javadoc/2.13.0/org/apache/beam/sdk/transforms/DoFn.ProcessElement.html)を参照してください。

+ Timestampアノテーション  
`PCollection`の要素の持つtimestampにアクセス可能です。  
型は`org.joda.time.Instant`です。
+ PipelineOptions  
`PipelineOptions`型の仮引数を渡しておくと、`DoFn`のサブクラス内でコマンドライン引数にアクセスできます。

[コードサンプル](./codes/dofn_args.md)
