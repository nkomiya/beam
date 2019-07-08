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

# Overview
## <span class="head">Beamによるデータ処理</span>
Beamを使ったプログラミング、および実行の際に、ユーザが決めるべきことは
1. Beamプログラミング  
    + 実行時オプションの定義
    + Inputの定義
    + 行う変換処理の定義
    + Outputの定義
2. Pipelineの実行
    + 実行環境（Runner）の指定
    + 実行時オプションの指定

Beamが動く環境はたくさんある。ローカルだったらDirectRunner、
DataflowだったらDataflowRunnerみたいな。詳しくは[こちら](https://beam.apache.org/documentation/runners/capability-matrix/)を参照。

## <span class="head">beamで現れる概念</span>
Beamには特有の用語がいくつかあります。

* `Pipeline`  
データ処理の最初から最後まで。全体的な何か。
実行時オプションは`Pipeline`を作る時に指定する。


* `PCollection`  
`Pipeline`で捌くdatasetの単位。`PCollection`のソース（データの入力元）はboundでもunboundでもいい。
  + bound  
  ファイルみたいに、決まったサイズを持つもの。
  + unbound  
  Twitterみたいにデータに終わりのないもの。

  通常は外部ソースからdataを読み込むけど、unit testや練習のために、in-memoryに記述してもok。

* `PTransform`  
変換処理のこと。`PCollection`をinputとして受け取り、処理を加えた`PCollection`をoutputとして返す。input, outputともに複数の`PCollection`を取れる。  
Beam SDKで提供される`PTransform`を使ってもいいし、user定義関数を作ってもいい。

* I/O Transform  
Beamでは様々な入出力先を指定できる。Datastore, BigQuery, Cloud Storage, Amazon S3, ... etc.

## <span class="head">Codingの流れ</span>
**1. Pipelineの作成**  
実行時のオプション（入出力先とか）、Runnerの指定はこの段階で。ただ、Runnerを指定するためにコードを返る必要は特に無い、はず。

**2. PCollectionの初期値作成**  
I/O Transformを使って外部から読むか、in-memoryのdataを使うかのどちらか。

**3. PTransformの適用**  
できることを大雑把に触れておくと、

* データの変換、整形
* フィルター
* グループ化
* 解析
* `PCollection`内の要素を調べる

とか。`PTransform`では、入力の`PCollection`は変換されず、新たな`PCollection`を出力として返すため、変換前の`PCollection`は再利用ができる。

**4. I/OTransformで、外部への出力**  
ローカルファイルとか、BigQueryのtableに保存できる。

**5. Pipelineの実行**  
上記の一連の処理の流れを作り終えたら、`pipeline`を実行してくれ！っていうメソッドの呼び出してcodingは終わり。  
処理の一連の流れはgraphっていう。こんな感じでgraphに分岐があってもokです。
![graph](https://cloud.google.com/dataflow/images/monitoring-side-input-write.png "graph")

Beamの動作詳細としては、まずpipelineのgraphを作成する。これは、後に触れる（かも）テンプレートの作成においてもそう。
もしテンプレート作成でなくてpipelineを実行させる場合は、作成されたgraphをもとに実際の処理が行われる。  
BeamでのPipeline処理は**非同期**処理です。並行分散処理なので、特定の処理は一回だけ行いたい、とか行う処理の順番を指定したい、とかは苦手なはずです。