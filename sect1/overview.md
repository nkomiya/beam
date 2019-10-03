[topへ](../index.md)

# Overview
## <span class="head">Beamによるデータ処理</span>
Apache Beamでは大規模なデータ処理をわりと簡単に (?) 実装できます。並列化の実装を全くせずに、パイプラインにおける処理が並列に捌かれます。たとえばDataflowで複数のVMに処理をさせるときに、コードの変更であったり並列処理の仕掛けを作ってやる必要がありません。

またストリーミング処理をバッチ処理とほぼ同じ感じで実装可能です。ストリーミング処理で気にするのは、どのタイミングで処理を発火させるのか、くらいですかね。


Beamを使ったプログラミング・実行の際に、ユーザが決めるべきことは

+ プログラミング時
  + 実行時オプション  
    プログラムの実行時にパラメータをとれますが、当然定義は必要です
  + Input / Output  
    けっこうな入出力先がサポートされてます[^1]  
  + 行う変換処理

+ 実行時
  + 実行環境[^2]
  + 実行時オプション

[^1]: https://beam.apache.org/documentation/io/built-in/
[^2]: https://beam.apache.org/documentation/runners/capability-matrix

## <span class="head">Beamで現れる概念</span>
Beamには特有の用語がいくつかあります。

<ul>
<li><code>Pipeline</code></li>
データ処理の最初から最後まで。

<li><code>PCollection</code></li>
処理を行うデータの集まりで、Beamではこのデータの塊に対して処理を加えていきます。  
原則、<code>PCollection</code>の中の要素は等価だと思うべきです。
<pre><code>苗字の集まり。
[
  "佐藤","鈴木","田中", ...
]  
文字列の集まり。佐藤さんの情報たち、と捉えない方が良いです。
[
  "佐藤","32歳","172cm", ...
]
</code></pre>

バッチ処理とストリーミング処理の違いは、データの終わりの有無です。前述の通りBeamではどちらもほぼ同じように扱えます。ただ全く同じわけではないので、それぞれ名前が付いてます。

<ul>
<li>Bounded</li>
データに終わりがある。(e.g. ファイル読み込み)
<li>Unbound</li>
データに終わりがない。(e.g. Twitterとか)
</ul>

通常は外部ソースからの読み込みですが、unit testや練習のために、コード内でデータを作ることもできます（BeamではJUnitが使えます）。

<li><code>PTransform</code></li>
Pipelineにおける各処理のステップです。Beam SDKで提供される`PTransform`はたくさんありますが、ユーザ定義の変換処理を適用可能です。
`PCollection`をinputとして受け取り、処理を加えた`PCollection`をoutputとして返す。

<li>I/O Transform</li>
入出力を行う`PTransform`なのですが、`Pipeline`の始めと終わりになります。
Beamでは様々な入出力先をサポートしてます。Datastore, BigQuery, Cloud Storage, Amazon S3, ... etc.
</ul>

> #### I/O Transformについてメモ
> `pipeline`の途中の`PTransform`では`PCollection`を受け取り、変換処理を行います。ですがBeam SDKによるデータの読み込みでは、`PCollection`を受け取りません。  
> なのでBeamを使って、GCSにアップされたファイルを読み込み、そのファイルに書かれた別のファイルたちを読み込みたい、みたいな処理は難しかったりします。  
> 入出力を可変にするならば、`pipeline`の実行時オプションとして指定するのが吉です。


## <span class="head">Codingの流れ</span>
### 1. Pipelineオブジェクトの作成
実行時に受け付けるオプション（入出力先とか）を定義した後、`Pipeline`オブジェクトを作ります。

### 2. PCollectionの作成
I/O Transformを使って外部から読むか、コード内で適当に初期値を作るか（主に）のどちらか。Pipelineのデータソースは複数指定可能で、`Pipeline`オブジェクトに関連づけられます。

### 3. PTransformの適用
`PCollection`に処理を加えていきます。できることを大雑把に触れておくと、

* データの変換、整形
* フィルター
* グループ化（国籍で分ける、みたいな）
* 解析（平均値の計算とか）
* `PCollection`内の要素を調べる

とかです。

### 4. I/OTransformで、外部への出力
ローカルファイルとか、BigQueryのtableにデータを出力します。

### 5. Pipelineの実行
一連の処理の流れを作り終えたら、Pipelineを実行するためのメソッドの呼び出します。処理の一連の流れはgraphっていう。こんな感じでgraphに分岐があってもokです。  

<img src="https://cloud.google.com/dataflow/images/monitoring-side-input-write.png" width="800">

また、Pipeline処理の完了後に何か処理を行うこともできます。

Beamの動作詳細としては、

1. Pipeline graphの作成（&uarr;の写真みたいな）  
処理の依存関係をみたりしてるのかと思ってます。  
2. 作成されたgraphの実行  

BeamでのPipeline処理は並行分散処理なので、捌かれる要素の順番を指定するのは原則不可です。また、場合によっては複数回同じ処理が実行されたりします。

> #### memo
> Cloud SQL &rarr; Cloud Spannerのデータ移行をDataflowで行なった際、同一レコードが複数回書き込まれる現象に遭遇しました。