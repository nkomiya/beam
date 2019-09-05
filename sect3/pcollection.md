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

# PCollectionの作成
## PCollectionとは？？
すでに触れてる通り、`pipeline`に流すdatasetのことを`PCollection`といいます。`PCollection`は決まったデータ構造を持つ配列みたいなものです。  
たとえば社員の給料を扱いたいとき、`PCollection`は社員の名前と給料を要素とした、データの寄せ集めになります。

## <span class="head">外部リソースの読み込み</span>
しばらく使うつもりがないので、詳しくは5章で説明します。

`PCollection`を作るにも、読み込み用の`PTransform`を`pipeline`に適用することになる。なので一番初めは、`pipeline` objectに対して`PTransform`を適用する、って意味でやや特殊。code上では特殊感はありませんが...

```java
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.io.TextIO;

public class Main {
  public static void main(String[] args){
    Pipeline pipeline = Pipeline.create( opt );
    //
    // file読み込み
    PCollection<String> col = pipeline
      .apply("ReadFromFile",TextIO.read().from("input.txt"));
    }
}
```

同じメソッドでGCS上のファイルも読み込める。`read`の引数をGCSのパス、たとえば"gs://komiya-no-test/input.txt"みたいのに変更するだけ。
`apply`の第一引数の"ReadFromLoacalFile"は処理につけるラベル。Dataflowで見れるgraphとかにラベルがつけられる。省略可。

GCSの場合、ローカルで実行すると認証エラー。回避するには、

1. プロジェクトのオーナー権限を持ったサービスアカウントの作成
2. キーファイルの作成
3. キーファイルのパスを環境変数`GOOGLE_APPLICATION_CREDENTIAL`にexport

```bash
$ export GOOGLE_APPLICATION_CREDENTIAL=/path/to/credential/file
```

IntelliJで環境変数の設定も可能です。気が向けば書きます。

## <span class="head">in-memoryデータの読み込み</span>
dataをソースにベタ打ちしてもok。リストを`Create.of`に渡してあげればよい。リストの各要素が、テキストファイルの一行として扱われます。

```java
import java.util.*;
// pipeline
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
// for passing the in-memory data to pipeline
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.coders.StringUtf8Coder;

public class Main {
    public static void main( String[] args ){
        // input source
        final List<String> input = Arrays.asList(
            "To be, or not to be: that is the question: ",
            "Whether 'tis nobler in the mind to suffer ",
            "The slings and arrows of outrageous fortune, ",
            "Or to take arms against a sea of troubles, " );
        // create pipeline
        Pipeline pipeline = Pipeline.create();
        // read
        pipeline.apply("ReadFromInMemoryData",Create.of(input));
    }
}
```

## PCollectionについて少し詳しく
二つ目は言ってることが良くわからん...

- `PCollection`は一つの`Pipeline`に帰属。複数の`Pipeline`での共有は不可。
- `PCollection`の関数は、ある意味クラスっぽく振る舞う。

まあ、以下の`PCollection`の性質は押さえておけば良さげ。

### <span class="head">要素の型</span>
`Pcollection`の型はなんでもよいですが、統一しなきゃだめです。  
Beamでは分散処理を行う上で、各worker（処理を行うもの）にdataを渡すために、dataをbyte列に変換する必要があります。
幸いBeam SDKでは、よく使われる型についてはencodingを自動でやってくれますが、自分でencodingの方法を指定することもできます。

### <span class="head">イミュータブル</span>
`PCollection`は一旦作ったら、追加とか、削除とか、変更とか、できないです。
`PTransform`では、inputの`PCollection`は参照するけど変更はされない。

### <span class="head">ランダムアクセス</span>
`PCollection`の各要素への[ランダムアクセス](https://kb-jp.sandisk.com/app/answers/detail/a_id/8980/~/%E3%82%B7%E3%83%BC%E3%82%B1%E3%83%B3%E3%82%B7%E3%83%A3%E3%83%AB%E3%82%A2%E3%82%AF%E3%82%BB%E3%82%B9%E3%81%A8%E3%83%A9%E3%83%B3%E3%83%80%E3%83%A0%E3%82%A2%E3%82%AF%E3%82%BB%E3%82%B9%E3%81%AE%E6%93%8D%E4%BD%9C%E3%81%AE%E9%81%95%E3%81%84)はサポートしてない。
各要素へのアクセスは、`PTransform`を経由で行う。

### <span class="head">datasetのサイズ</span>
`PCollection`はイミュータブルな（大きい）data。分散処理ができるため？、こいつにサイズ上限はないと謳っている。

`PCollection`として扱うdataは、あらかじめサイズが分かっていても（bounded）、サイズが分からないくてもよい（unbounded）。

<dl>
<dt>Bounded</dt>
<dd>
<a href="https://www.idcf.jp/words/batch-processing.html">バッチ処理</a>。
<br>
データサイズが分かっているから、全部読み込んで変換処理すればよい。
</dd>
<dt>unbounded</dt>
<dd>
<a href="https://www.imkk.jp/blog/what-is-stream-data-processing.html">ストリーミング処理</a>向け。Pub/Subとか、Apache Kafkaがあたる。
<br>
データが流れ込み続けるので、`pipeline`は起動しっぱなしになる。
</dd>
</dl>

### <span class="head">要素のタイムスタンプ</span>
Beamでは`PCollection`の要素にtimestampを割り当てることができる。
ファイルのtimestampとか、Twitterみたいな`unbounded`なソースだったら、tweetしたときの時間、にできる...？要確認です...。

また、inputのソースにtimestampがついてなければ、`PTransform`でくっつけることもできる。嬉しい？ことに、inputのdatasetには変更を加えず、timestampだけをつけたPCollectionを返せす`PTransform`もある(後述)。
