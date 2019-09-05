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

# データのエンコードと型安全性
Beamではパイプラインにおける`PCollection`は、中間データも含めてバイト列と相互に変換できる必要があります。この変換を行うオブジェクトはBeamでは`Coder`とよばれ、元データとバイト列の間で変換することをencode / decodeと読んでいます。

JavaのBeam SDKでは、様々な型（Integer, Long, String, ... etc.）に対して`Coder`のサブクラスが定義されてます。（基本的には明示的に指定しなくても）`Coder`サブクラスを使えば（）

Note that coders do not necessarily have a 1:1 relationship with types. For example, the Integer type can have multiple valid coders, and input and output data can use different Integer coders. A transform might have Integer-typed input data that uses BigEndianIntegerCoder, and Integer-typed output data that uses VarIntCoder.



## <span class="head">Coderの指定</span>
Beamでは全ての`PCollection`は`Coder`が指定されている必要があるそうですが、基本的には`PCollection`の型やapplyする`PTransform`の型から自動的に`Coder`を決めてくれます。
たとえば`DoFn<Integer,String>`の関数オブジェクトでは、outputはString型の`PCollection`になります。なので、outputの`PCollection`の`Coder`は、String型のdefaultの`Coder`（何も指定がなければ`StringUtf8Coder`）となります。  
Beamで`Coder`がうまく推測出来ない場合は、ユーザ自身が明示的に`Coder`の指定を行わなければいけない場合もあるそうです。

`PCollection`の`Coder`を指定するには、`setCoder`メソッドを使います。たとえば、次のような感じです。

```java=
PCollection<String> pcol = ...;
pcol.setCoder(StringUtf8Coder.of());
```

`PCollection`にapplyを適用した後で、`Coder`の指定をすると実行時にエラーが出ます。

```java=
PCollection<String> pcol = ...;
pcol.apply( ... );
// pcolにapplyを適用した後なので、Coderを変更できない
//pcol.setCoder(StringUtf8Coder.of());
```

Beamは通常のプログラミングと違って、コードの一番上から順に、一つの行の処理を終えるごとに次の行の処理に移るわけではありません。  
たとえばBigQueryのテーブルをGCSにテキストファイルとしてエクスポートする、みたいなpipelineを実行すると、BigQueryのテーブルの大きさにもよるかもですが読み込みと同時に書き出しも行われます。  
そのため、applyを適用した段階で`PCollection`がfinalizeされ、変更が不可となります。

`PCollection`の`Coder`を調べたければ、`getCoder`メソッドを用います。

```java=
PCollection<String> pcol = ...;

// StringUtf8Coder,　と出力されるはずです
System.out.pritnln(pcol.getCoder());
```
もし`Coder`の指定がなく、また自動決定がされていなければ、`getCoder`メソッドはエラーを返します。

in-memoryのデータから`Create`メソッドで`PCollection`を作る場合は、`withCoder`メソッドを使って`Coder`の指定を行います。

```java
Pipeline pipeline = Pipeline.create();
pipeline
    .apply(
        Create.of( [リスト] )
        .withCoder( [Coder] )
    )
    ...
```

`Create`は引数の型情報を参照しないらしいので、あまり型推測を信頼すべきでなく、`withCoder`で明示的に指定した方がよいようです。

## <span class="head">デフォルトのCoderを指定する</span>