[topへ](../index.md)  
[PTransformの概要へ](./ptransform.md)

# Side Input
`ParDo`では、Inputの`PCollection`の要素ごとに変換処理を行い、別の`PCollection`を作りました。Side Inputは`ParDo`における要素ごとの各変換処理で、共通の副入力のデータを参照させる方法です。

事前準備として`ParDo`をapplyする前に、副入力として渡したい`PCollection`から`PCollectionView`を作ります。作り方は色々ありますが、`PCollection`の中には要素が一つだけである必要があります。

```java
import org.apache.beam.sdk.transforms.View;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;

PCollection<T> pCollection = ...;
PCollectionView<String> pCollectionView = pCollection.apply(View.asSingleton());
```

要素が一つだけ、という点と相性が良いため、`Combine`周りのtransformからも作れます。

```java
PCollection<Integer> pCollection = ...;
PCollection<Integer> pCollectionView =
    pCollection.apply(Sum.integersGlobally().asSingletonView());
```

シングルトンについては[こちら](https://www.atmarkit.co.jp/ait/articles/0408/10/news088.html)あたりを読んでみると勉強になるかもです。

`DoFn`にSide Inputを渡すには、`ParDo`インスタンスを作るときに`withSideInput`を使います。`DoFn`内部での参照は、ProcessContextを使います。  
コードの見た目は、次のようになります。

```java
PCollection<T1> pCollection = ...;
PCollectionView<T2> pCollectionView = ...;

pCollection.apply(ParDo.of(
    new DoFn<T1,T3> () {
      @ProcessElement
      public void method(ProcessContext ctx) {
        T1 input = ctx.element();
        T2 sideInput = ctx.sideInput(pCollectionView);
        // ... 中略 ...
      }
    }).withSideInput(pCollectionView));
```

`DoFn`サブクラス内で、pCollectionViewにアクセスできなければいけないので、匿名クラスを使ってインスタンスを作成しています。Side Inputを伴う`DoFn`を再利用するには、

+ `PTransform`サブクラスを作成し、`DoFn`をwrapする
+ `DoFn`サブクラスのコンストラクタで`PCollectionView`を渡す

あたりでしょうか。[こちら](./codes/sideInput.md)のサンプルでは、`DoFn`サブクラスのコンストラクタで`PCollectionView`を渡しています。サンプルでやっていることは、全文字数に対する一行あたりの文字数の計算をしています。

Side Inputで渡すviewは、Pipelineのに流すデータから動的につくるべきです。ハードコードや実行時オプションでデータを渡すならば、コンストラクタ経由で渡す方が簡単です。

<!-- どこに埋め込もうか...
#### memo
`PCollectionView`を作るには、`PCollection`を単一の値に絞り込む必要があります。そのため、元となる`PCollection`が有限でないと、viewを作ることはできません。  
また、globalでないwindowを使った`PCollection`からviewを作ると、windowにつき一つのviewができます。

mainのinputとside inputでwindowの時間間隔は違ってもよく、Beamが適切なside inputを選んでくれます。動作としては、main inputの要素（が持つwindow?）を、side inputのwindowたちに投影して適切なside inputのwindow、つまりside inputの値を選びます。

そのため、main inputを複数のwindowに分割させるなら、処理を発火させるタイミングに応じてside inputを変化させることも可能です。
-->