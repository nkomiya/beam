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

# PTransformの概要
いよいよ、`Pipeline`の中で行う処理である`PTransform`について。`PTransform`による処理を行うには、**関数オブジェクト**を定義する必要がある。Beam SDKで用意されてる処理を加えるのでもよいですが。

関数オブジェクトの要点としては、  
- 適切なBeam SDKのinterface, classを継承して作る
- Input/OutputのPCollectionの型を決める
- 処理を行うメソッドを定義する

って感じです。

## PTransformの適用
めんどくさい言い方をすれば、`PCollection`インスタンスの`apply`メソッドに、`PTransform`のインスタンスを渡すことで変換処理を実行する。

**単一のPTransform**

```java
[ Output ] = [ Input ].apply( [ Transform ] );
```

見た目としては、こんな感じです。

**複数のPTransform**

```java
[ Output ] = [ Input ]
                .apply( [ Transform 1 ] )
                .apply( [ Transform 2 ] ) ... 
```

`PTransform`は呼び出しはいつもapplyで、かつ基本的に`PCollection`を返す。なので、上みたいにapplyをくっつけまくることが可能です。

イメージは、こんな感じで`PTransform`を直線状につながれている。
![これ](https://beam.apache.org/images/design-your-pipeline-linear.png)


Inputの`PCollection`がimmutable、つまり**変更されないまま**のは少し嬉しい。なぜなら、分岐処理が可能になるので。

```java
// まずdataset読み込み
[ 親 PCollection ] = [ Input source].apply( [ 読み込み PTransform] )
// 同じ親をもとに、PCollectionを作る
[ 子 PCollection A ] = [ 親 PCollection ].apply( [ PTransform A ] )
[ 子 PCollection B ] = [ 親 PCollection ].apply( [ PTransform B ] )
```

分岐処理は図だとこれ。

![これ](https://beam.apache.org/images/design-your-pipeline-multiple-pcollections.png)

beamの推奨としては循環しないgraphを作るように、とのこと。
プログラミングにおける関数定義と一緒で、複数の`PTransform`を内包した"Composite transform"（合成変換？）を作っておくと、可読性もあがるし再利用もできるので便利です。詳しくは後述。

## 基本的なPTransform
Core Beam transformと公式guideで呼ばれてる、基本的なbuilt-inの`PTransform`たちです。

- `ParDo`
- `GroupByKey`
- `CoGroupByKey`
- `Combine`
- `Flatten`
- `Partition`
