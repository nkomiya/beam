[戻る](../core.md)

# CoGroupByKey
`CoGroupByKey`は複数の`KV`型の`PCollection`を、Keyでまとめるためのtransformです。共通のkeyをもっているならば、valueは型が違ったりしても問題ありません。

> <img src="figs/design-your-pipeline-join.png">  
> [https://beam.apache.org/images/design-your-pipeline-join.png](https://beam.apache.org/images/design-your-pipeline-join.png)

想定としては同じタイプのkeyを持つ複数のソース（データに関連のある複数のソース）があって、それらをkeyで結合して一つにする、という処理を行う際に使います。

例として挙げられているのは

1. 名前とemailのdataset
2. 名前と電話番号のdataset

みたいな二つのソースがある場合です。名前をkeyに、valueを 

```json
{ 
  "email": "hoge@example.com", 
  "phone": "090-XXXX-XXXX"
}
```

みたいな一つのmapにしたくなります。

`CoGroupByKey`では`TupleTag`というものを使います。結合前の`PCollection`それぞれに一意のタグをつけておき、結合後に`PCollection`の要素にアクセスする際に用います。  
`TupleTag`の作り方は以下の通りです。`V`はkey/valueペアのvalueの型です。

```java
import org.apache.beak.sdk.values.TupleTag;

// ... 中略 ...

// PCollection<KV<K,V>>につけるタグ
TupleTag<V> tag = new TupleTag<V>(){{
}};

```

`TupleTag`の作成後、`PCollection`を結合して`KeyedPCollectionTuple<K>`を作る。`K`はkeyの型です。型引数が一つしか無いことから分かるかもしれませんが、結合する`KV`型の`PCollection`において、**keyの型は同じ**でなければいけません。  
`KeyedPCollectionTuple`の作り方は以下の通りです。`and`メソッドは複数回呼び出し可能なため、3つ以上の`PCollection`をつなげることもできます。

```java
import org.apache.beam.sdk.transform.join.KeyedPCollectionTuple;
import org.apache.beam.sdk.values.PCollection;

// ... 中略 ...

// 結合前のPCollection
PCollection<KV<K,V1>> p1 = ...;
PCollection<KV<K,V2>> p2 = ...;

// KeyedPCollectionTupleを作成
KeyedPCollectionTuple<K> keyedPCollectionTuple =
    KeyedPCollectionTuple.of(p1tag, p1).and(p2tag, p2);
```

この段階ではtupleを作成したにすぎないので、`CoGroupByKey`を`apply`します。

```java
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.transforms.join.CoGroupByKey;
import org.apache.beam.sdk.values.KV;

// ... 中略 ...

// Kはkeyの型
PCollection<KV<K, CoGbkResult>> cogbkResult =
    keyedPCollectionTuple.apply("JoinTwoSource",CoGroupByKey.create());
```

`DoFn`の中で、結合後のデータにアクセス

```java
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.join.CoGbkResult;

// ... 中略 ...

// 同じく、Kはkeyの型です。
cogbkResult
    .apply(ParDo.of(
        new DoFn<KV<K, CoGbkResult>, OutputT>() {
          @ProcessElement
          public void method(ProcessContext ctx) {
            // ProcessContextはinputのPCollectionの要素にアクセスできたり、
            // outputを突っ込めたりして便利です。
            KV<K, CoGbkResult> e = ctx.element();
            K key = e.getKey();
            CoGbkResult value = e.getValue();
            
            // あらかじめ作成したtagを使い、結合後のデータにアクセスします
            Iterable<V1> v1all = value.getAll(p1tag);
            
            // ... 中略 ...
          }
        }));
```

Keyでひとまとめにする、という意味では`GroupByKey`と同じですが、`CoGroupByKey`は

+ 共通のkeyを持つ、複数のデータソースがある
+ 各データソースの属性（Value）が異なる

であるような場合に、keyでの関連づけを行うためのtransformになります。

動作するコードの全体は、[こちら](./codes/cogbk.md)を参照してください。

> #### CoGroupByKeyとunbounded PCollection
> `CoGroupByKey`も、`GroupByKey`で説明した問題があてはまります。詳しくは[そちら](../groupbykey.md#comment)を確認してください。  
> Windowはタイムスタンプに基づいて要素をグループ化するものです。`CoGroupByKey`は複数の`PCollection`をinputに取りますが、どの`PCollection`も同じようにwindow化されなければいけません。
