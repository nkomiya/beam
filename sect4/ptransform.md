[topへ](../index.md)

# PTransform
**目次**

1. [概要](#overview)
2. [基本的なPTransform](./core.md)
3. [注意点](./warnings.md)
4. [Side Input](./sideInput.md)
5. [ParDo発展編](./pardo2.md)
6. [Composite transform](./composite.md)

## <span class="lhead" id="overview">概要</span>
いよいよ、`Pipeline`の中で行う処理である`PTransform`についてです。Beam SDKで用意されている`PTransform`を使うにしろ、処理のロジックを定義したカスタムクラスを作る必要があります（user codeと呼ばれます）。

user codeを作る手順は、

- 適切なBeam SDKのinterface, classを継承する
- Input/Outputの`PCollection`の型を決める
- 処理を行うメソッドを定義（Override）する

という具合になります。

## PTransformの適用
`PCollection`インスタンスの`apply`メソッドに、`PTransform`のインスタンスを渡すことで、処理が実行されます（正確には実行ではなく、graphの定義）。

**単一のPTransform**

```java
[ Output ] = [ Input ].apply( [ Transform ] );
```

**複数のPTransform**  
次のように、`apply`メソッドをつなげて、複数の`PTransform`を一気に行うこともできます。

```java
[ Output ] = [ Input ]
                .apply( [ Transform1 ] )
                .apply( [ Transform2 ] )
                .apply( [ Transform3 ] )
```

イメージとしては、`PTransform`を直線状につながれている感じです。
![これ](https://beam.apache.org/images/design-your-pipeline-linear.png)


Inputの`PCollection`がImmutableであるので、もとの`PCollection`は**変更されないまま**です。  
そのため、一つの`PCollection`から複数の`PCollection`を作るような分岐処理が可能です。

```java
// まず、親となるPCollectionの作成
[ 親 PCollection ] = [ Input ].apply( [ Transform0 ] )
// 同じ親をもとに、PCollectionを作る
[ 子 PCollection A ] = [ 親 PCollection ].apply( [ Transform1 ] )
[ 子 PCollection B ] = [ 親 PCollection ].apply( [ Transform2 ] )
```

イメージはこんな感じです。

![これ](https://beam.apache.org/images/design-your-pipeline-multiple-pcollections.png)

循環しないPipeline graphを作ることが推奨されています。Pipeline graphは、`PCollection`を頂点、`PTransform`を向きを持った辺とした[有向グラフ](https://jp.mathworks.com/help/matlab/math/directed-and-undirected-graphs.html)です。

また、Beamでは複数の`PTransform`をまとめた"Composite transform"（合成変換？）を作ることができます。コードの可読性も上がり、再利用も可能なため便利です。詳しくは[後述](./composite.md)。