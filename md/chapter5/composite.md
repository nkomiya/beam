[topへ](../index.md)  
[PTransformの概要へ](./ptransform.md)

# Composite transform
コードの可読性を上げる、とかPipeline graphを美しくする（わたしの主観）、とか使いまわしをする、って意味でComposite transform（今後、合成変換と呼びます）が便利です。  
これは、複数の`PTransform`からなるユーザ定義の`PTransform`です。要素の合計を求めて出力みたいなときに、(1)合計の計算、(2)出力用に文字列に整える、の二つをまとめた`PTransform`を作る、みたいなノリです。

Built-inの合成変換は[org.apache.beam.sdk.transform](https://beam.apache.org/releases/javadoc/2.16.0/index.html?org/apache/beam/sdk/transforms/package-summary.html)を確認してください。

## <span class="head">合成変換を作ってみる</span>
特に難しいことはない。`PCollection`を受け取って、`PCollection`を返す`PTransform`のサブクラスを作れば良い。  
`PTransform`のサブクラスの`expand`メソッドをオーバーライドする。`expand`の中の処理が順に実行される感じです。

```java
class MyTransform extends
    PTransform<PCollection<InputT>, PCollection<OutputT>> {
  @Override
  public PCollection<OutputT> expand(PCollection<InputT> input) {
    // ... 処理 ...
    return [OutputT型のPCollection];
  }
}
```

ただ、Pipelineの始まり (読み込み) と、終わり (書き込み) はやや特殊で、それぞれ`PBegin`と`PDone`という型を使います。

```java
class MyTransform extends
    PTransform<PBegin, PDone> {
  @Override
  public PDone expand(PBegin input) {
    // PBeginに対しては、TextIOなどで読み込みが可能
    input.apply(TextIO.read().from(...));

    // PDoneを作るには、PDone.inを使います。
    // 実引数にはPipelineインスタンスを取ります。
    return PDone.in(input.getPipeline());
  }
}
```

合成変換をapplyするには、インスタンスをapplyに渡すだけです。

```java
pCollection.apply(new MyTransform());
```

[コードサンプル](./codes/composite.md)
