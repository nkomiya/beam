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

# ParDo
並行処理（**Par**allel **Do**）の意味で`ParDo`なはずです。
`ParDo`は、[_Map/Shuffle/Reduce_](https://enterprisezine.jp/dbonline/detail/4440)モデルにおける"map"処理に似ているそうです。Inputを分割して、各要素を何かしらの値にマップする。文章中の単語の登場回数みたいな。[ここ](https://jp.talend.com/resources/what-is-mapreduce/)の画像が分かりやすめです。
![がぞう](https://jp.talend.com/wp-content/uploads/what-is-mapreduce.jpg)

map処理を行うケースはこんな感じ。
1. datasetのフィルター  
    PCollection内の要素を調べ、いらない要素を捨てた新たなPCollectionに返す。欠損値を捨てる、みたいな処理。
2. datasetの整形や型変換  
    Inputのdatasetが必要なformatを持ってなかったりしたときに。csvの順序を変えたり、JSON文字列にしたり、みたいな。 
3. PCollectionの要素の一部を引っ張りたい  
    PCollectionの要素が複数のfieldを持っているときに、要らないfieldを落とす。
4. datasetに演算を加えたい  
    身長と体重からBMIに変換する、みたいな。
    
もし`ParDo`変換で自前の処理を加えたければ、Beam SDKの`DoFn`クラスを継承させたサブクラスを作る必要がある。`DoFn`は**DoF**u**n**ctionの意味だそう。

## <span class="head">やってみよう - DoFn 本格編</span>
いきなり本格編なのであれなのですが...。  
先で触れたとおり、`ParDo`に限らず`PCollection`に変換を加える場合は`apply`メソッドを使う。`ParDo`の場合はこんな感じ。基本的にはnewでインスタンスを作りますかね。

```java
[ PCollection ].apply( ParDo.of( [DoFnのサブクラスのインスタンス] ) )
```

書くべきなのはInput / Outputの型と、InputのPCollectionの各要素に対して行われる処理だけでokで、要素抽出などはSDKがやってくれる。`DoFn`のサブクラスの宣言の例はこんな感じ。

```java
static class MyDoFn extends DoFn<String, Integer> {
    @ProcessElement
    static void mymethod(@Element String in, OutputReceiver<Integer> o) {
        ...
    }
} 
```

input, outputの型は`DoFn`の型引数で指定する。上の例だとinputはString型、outputはInteger型。
処理で行われるのは、`ProcessElement`アノテーションを付けたメソッドに書きます。メソッド名は何でも良いです。
また、`Element`アノテーションをつけた変数にinputの`PCollection`の要素が渡り、`OutputReceiver<T>`型の変数にoutputの`PCollection`に加えたいdataを突っ込む。

```java
import java.util.List;
import java.util.Arrays;
//
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
//
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.coders.StringUtf8Coder;
//
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
//
public class Main {
    public static void main( String[] args ){
        // input source
        List<String> input =　Arrays.asList(
            "To be, or not to be: that is the question: ",
            "Whether 'tis nobler in the mind to suffer ",
            "The slings and arrows of outrageous fortune, ",
            "Or to take arms against a sea of troubles, " );
        //
        Pipeline pipeline = Pipeline.create();
        //
        pipeline
                .apply( "ReadFromInMemoryData", Create.of(input) )
                .apply( "Textlength", ParDo.of( new TextlengthFn() ) );
        //
        pipeline.run();
    }

    static class TextlengthFn extends DoFn<String,Integer> {
        @ProcessElement
        public void processElement(@Element String l, OutputReceiver<Integer> o){
            o.output( l.length() );
        }
    }
}
```

inputの`PCollection`は適当に分割されるので、`DoFn`インスタンスは複数回呼び出されることになる。ただ失敗時に備えるとかで複数回呼ばれることもあるため、分割した分だけ`DoFn`インスタンスが呼ばれる訳ではない。呼び出し回数はキャッシュできるけど、呼び出し回数に依存した処理は実装すべきでないとのこと。

また、`PCollection`がimmutableであることを保証するため、メソッドの実装の際には以下の変数の値を
いじくるな、とのこと。

- `Element`アノテーションをつけた変数
- `OutputReceiver`で出力をした後では、任意の変数

## <span class="head">やってみよう - DoFn お手軽編</span>
もし簡単な処理をしたいだけで、`DoFn`のサブクラスを定義するまでもない...という場合は、匿名クラスを使って処理を書いてもok。`apply( ParDo.of(...) )`を次のように変える。

```java
... 中略 ...

apply( ParDo.of(
        new DoFn<String, Integer>() {
            @ProcessElement
            public void mymethod(@Element String l, OutputReceiver<Integer> o) {
                o.output(l.length());
            }
        }))
```

## <span class="head"> やってみよう - Map編</span>
もし、inputの`PCollection`の各要素に一つの値を対応させるなら（数学的には写像）、`MapElements`メソッドが便利。上の場合、inputの`PCollection`の要素は文章中の一行で、行ごとに文字数を対応させているので、`MapElements`が使える。

```java
import org.apache.beam.sdk.transform.MapElements;
import org.apache.beam.sdk.values.TypeDescriptors;

... 中略 ...

/** intoの中でoutputの要素の型、 
/*  viaの中で要素に施す変換を定義する */
apply( MapElements.into(TypeDescriptors.integers() ).via( (String line) -> line.length() ))
```

`via`の中の処理は、Java8から追加された[ラムダ式](https://qiita.com/dev_npon/items/4edd925f0fafe969bd06)を使っています。
