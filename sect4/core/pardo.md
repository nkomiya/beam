[戻る](../core.md)

# ParDo
並行処理（**Par**allel **Do**）の意味で`ParDo`です。`PCollection`の要素に任意の変換処理を行うことができます。ですが、要素ごとに個別の処理を行うため`PCollection`の全要素数を参照する、みたいなことはできません。


`ParDo`は、[_Map/Shuffle/Reduce_](https://enterprisezine.jp/dbonline/detail/4440)モデルにおける"Map"処理に似ているとのこと。  
下の画像だと、Map処理は各要素に"1"をマップする処理になります。

> <img src="./figs/what-is-mapreduce.jpg" width="700">  
> [https://jp.talend.com/resources/what-is-mapreduce/](https://jp.talend.com/resources/what-is-mapreduce)

`ParDo`を行うケースは以下のようになります。

1. フィルタリング  
`PCollection`の要素を取捨選択します。欠損値を捨てる、みたいな処理です。
2. データ整形や型変換  
HashMapからJSON文字列への変換や、文字列をパースして整数に型変換する、とかです。
3. 要素の一部を引っ張りたい  
データ整形みたいなものですがCSVフィールドの一部を落とす、みたいな処理です。
4. datasetに演算を加えたい  
UNIXタイムスタンプから時間文字列に変換、とかです。
    
`ParDo`変換で行う処理のロジックは、Beam SDKの`DoFn`クラスを継承させたサブクラス内で定義する必要があります。

## <span class="head">DoFn編 - サブクラスの作成</span>
先で触れたとおり、`ParDo`に限らず`PCollection`に変換を加える場合は`apply`メソッドを使う。`ParDo`だと、new演算子で作った`DoFn`サブクラスのインスタンスを渡します。

```java
[ PCollection ].apply(ParDo.of( [DoFnサブクラスのインスタンス] ));
```

`DoFn`サブクラスで書くべきなのはInput / Outputの型と、InputのPCollectionの各要素に対して行われる処理だけでokで、要素抽出などはSDKがやってくれます。  
`DoFn`のサブクラスの宣言の例はこんな感じになります。

```java
import org.apache.beam.sdk.transforms.DoFn;

... 中略 ...

class MyFn extends DoFn<String, Integer> {
  @ProcessElement
  public void method(@Element String input, OutputReceiver<Integer> o) {
    o.output(input.length());
  }
}
```

input, outputの型は`DoFn`の型引数で指定する。上の例だとinputはString型、outputはInteger型。
処理で行われるのは、`ProcessElement`アノテーションを付けたメソッドに書きます。メソッド名は何でも良いです。  
Inputの要素は`Element`アノテーションをつけた仮引数に入り、出力は`OutputReceiver<T>`型の変数のoutputメソッドに渡します。

inputの`PCollection`は適当に分割されるので、`DoFn`インスタンスは複数回呼び出されることになる。ただ失敗時に備えるとかで複数回呼ばれることもあるため、分割した分だけ`DoFn`インスタンスが呼ばれる訳ではない。呼び出し回数はキャッシュできるけど、呼び出し回数に依存した処理は実装すべきでないとのこと。

また、`PCollection`がimmutableであることを保証するため、メソッドの実装の際には以下の変数の値を
いじくるな、とのこと。

- `Element`アノテーションをつけた変数
- `OutputReceiver`で出力をした後では、任意の変数

## <span class="head">DoFn - 匿名クラスの利用</span>
もし同じ変換処理を繰り返すつもりがなく、サブクラスを定義するのが面倒という場合は、匿名クラスを使って処理を書いても大丈夫です。

```java
// input
List<String> input = ...;

// graph作成
pipeline
    .apply(Create.of(input))
    .apply(ParDo.of(
        new DoFn<String, Integer>() {
          @ProcessElement
          public void method(@Element String e, OutputReceiver<Integer> o) {
            o.output(e.length());
          }
        }));
```

