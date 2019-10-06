[topへ](../index.md)

# PCollectionの作成
## PCollectionとは？？
すでに触れてる通り、Pipelineに流すデータの塊が`PCollection`です。`PCollection`は決まったデータ構造を持ちます。  
たとえば社員の給料を扱うときだと、`PCollection`は各社員の名前と給料を要素としてもち、`PCollection`の中には全社員分のデータが入ることになります。

## <span class="head">外部リソースの読み込み</span>
しばらく使うつもりがないので、詳しくは5章で説明します。

Pipelineのはじめに`PCollection`を作るのも`PTransform`です。Beam SDKではIOコネクタが用意されていますが、まずファイルを読み込む方法について触れます。

次のコード例では、ローカルファイルを読み込みます。ファイルの一行が一つの文字列として扱われ、改行は取り除かれます。

```java
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.io.TextIO;

public class ReadLocalFile {
  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();
    //
    // ローカルファイルの読み込み
    PCollection<String> col = pipeline
        .apply("ReadLocalFile", TextIO.read().from("input.txt"));
  }
}
```

同じメソッドでGCS上のファイルも読み込めます。コード上では`from`の引数をGCSのパス、たとえば "gs://someBucket/input.txt" みたいのに変更するのみです。`apply`の第一引数は`PTransform`につけるラベルです。DataflowのWeb UIで見れるgraphにラベルが表示されます (重複があるとビルド時に警告がでます)。ラベルは省略可能ですが、省略すると警告がでます。

GCSから読み込む場合、当然ながら認証情報を教えてあげる必要があります。ただ、ローカルでの実行とDataflowでの実行では、やや振る舞いが異なります。Dataflowでの実行では、(1) Dataflowジョブの作成、(2) 処理におけるリソースへのアクセス、の２箇所で認証が必要になるためです。  

とりあえずはローカルPCで実行する際に認証を通す方法を二つ紹介します。

**デフォルトの認証機構の利用**  
ターミナルで次のコマンドを打つとブラウザが起動し、アカウントの選択画面が開きます。ログインを終えると、ローカルPCにCreadential fileが作成されます。この他に何も認証設定をしていない場合に、このCredentialが使われます。

```bash
$ gcloud auth application-default login
```

コンテナ環境で実行していて直接ブラウザを起動できない場合は、--no-launch-browserのオプションをつけてください。

**環境変数の利用**  
こちらはサービスアカウントを利用する方法です。鍵ファイルを落とし、次の環境変数で鍵ファイルへのパスを指定します。作成したサービスアカウントには、必要なリソースへのアクセス権限をつけてください。

```bash
$ export GOOGLE_APPLICATION_CREDENTIALS=/path/to/credential/file
```

IntelliJで環境変数の設定もできます。気が向けば書きます。

## <span class="head">in-memoryデータの読み込み</span>
コード内に書いたデータをインプットに`PCollection`を作成することもできます。読み込ませるデータは、単一でも複数でもOKです。  
複数のデータをインプットにする場合は、配列ではなくリストにする必要があります。リストの各要素が`PCollection`の一つの要素になります。

```java
import java.util.*;
// Apache Beam
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.coders.BigEndianIntegerCoder;

public class ReadInMemoryData {
  public void main(String[] args) {
    // create pipeline
    Pipeline pipeline = Pipeline.create();

    // .............................. 単一のデータを読み込む
    pipeline.apply("ReadSingleValue",
        Create.of(1).withCoder(BigEndianIntegerCoder.of()));

    // .............................. 複数のデータを読み込む
    // 読み込むデータ。複数の場合、リストにする必要がある
    final List<String> input = Arrays.asList(
        "To be, or not to be: that is the question: ",
        "Whether 'tis nobler in the mind to suffer ",
        "The slings and arrows of outrageous fortune, ",
        "Or to take arms against a sea of troubles, ");

    pipeline.apply("ReadMultipleValues",
        Create.of(input).withCoder(StringUtf8Coder.of()));
  }
}
```

`withCoder`については、7章で詳しく説明します。

## PCollectionについて少し詳しく
大雑把には、`PCollection`は次の二つの性質を持ちます。

+ `PCollection`は一つの`Pipeline`に帰属  
複数の`Pipeline`を立てることもできるのですが、`PCollection`を`Pipeline`間で共有することはできないです。
+ `PCollection`は、Collectionsクラスっぽく振る舞う

### <span class="head">要素の型</span>
`Pcollection`の型は、自作のクラスも含めて何でもOKです。ただ、`PCollection`の要素は全て同じ型に制限されます。
Beamは平行分散で処理を行いますが、各worker（処理を行うもの）でデータをやりとりする際に、データをバイト列に変換する必要があります（encode / decode)。  
IntegerやStringなど、よく使われる型はこの処理は自動でやってくれます。

### <span class="head">イミュータブル</span>
`PCollection`インスタンスは一旦作ったら変更不可能です。`PTransform`では、インプットの`PCollection`の要素を参照しますが、元の`PCollection`は変更されないままです。

### <span class="head">ランダムアクセス</span>
`PCollection`の各要素への[ランダムアクセス](https://kb-jp.sandisk.com/app/answers/detail/a_id/8980/~/%E3%82%B7%E3%83%BC%E3%82%B1%E3%83%B3%E3%82%B7%E3%83%A3%E3%83%AB%E3%82%A2%E3%82%AF%E3%82%BB%E3%82%B9%E3%81%A8%E3%83%A9%E3%83%B3%E3%83%80%E3%83%A0%E3%82%A2%E3%82%AF%E3%82%BB%E3%82%B9%E3%81%AE%E6%93%8D%E4%BD%9C%E3%81%AE%E9%81%95%E3%81%84)はサポートしていません。つまり、`PCollection`に対し、`get("keyname")`のようにして要素を引っ張ることはできないです。  
Beamでは、`PCollection(s)`に`PTransform`を作用させ、`PCollection(s)`を得るという流れになります。

### <span class="head">サイズと有限性</span>
`PCollection`はイミュータブルなデータの集まりですが、格納できるデータ数に上限はないそうです（ローカルで走らせるとメモリ不足になることもあります）。

すでに触れている通り、`PCollection`として扱えるデータはBoundedでもUnboundedでも大丈夫です。

* **Bounded**  
ファイルやDBからの読み込みのような、入力データに終わりがあるもの。日次のバッチ処理とかです。
* **Unbounded**  
ストリーミング処理のような、入力データに終わりがあるもの (e.g. Pub/Sub, Apache Kafka)。  
データが流れ込み続けるのでPipelineは起動し続けることになります。Dataflowなら、VMが立ち上がりっぱなしです。

### <span class="head">要素のタイムスタンプ</span>
Beamでは入力ソースがBoundedでもUnboundedでも、同じロジックで処理を行うことができます。ですが、Unboundedなソースでは全てのデータが集まることはないので、データ集計のような処理は難しいように思えるかもしれません。

データ集計のような処理において、Beamでは`Window`という概念が重要になります。これは、`PCollection`の**各要素**に割り当てられたtimestampに基づき、要素をグループ化したものです。`Window`ごとに処理を行うことで、Unboundedなソースでも有限サイズのデータとして扱うことができます。

Beamが自動で割り当てるtimestampは入力ソースに依存します。ファイル読み込みだったら`Long.MIN_VALUE`（Boundedなソースでは、通常`Window`は必要にならないためです）、Pub/Subみたいのであればpublishしたときの時間です。ユーザ定義のtimestampをくっつける`PTransform`も用意されているため、CSVファイルの一列目をtimestampにする、みたいなことも可能です。