[topへ](../index.md)

## Window
**目次**

1. [概要](#overview)
2. [ウィンドウの基礎](#basic)
3. [ウィンドウに関する注意点](#note)
4. [Bounded PCollectionとウィンドウ](#bounded)
5. [Built-inのwindow変換](./built-in.md)
6. [Watermarkと遅延データ](./watermark.md)
7. [Timestampの更新](./timestamp.md)

## <span class="lhead" id="overview">概要</span>
この章では、`PCollection`を要素の持つtimestampに基づいてグループ化を行う`window`について扱います。

`GroupByKey`や`Combine`のようなtransformでは、`PCollection`の要素全体に作用して集計処理などを行いました。しかし、全データが決して揃うことのない、unboundedな`PCollection`でこのような処理をするには、どうすればよいのか？となります。

この問題を解決するのが`window`の考え方です。Beamではtimestampに基づいて、入力データを有限サイズのデータの塊に分割して取り扱うことができます。特定の時間範囲に収まるデータをひと塊りにすることで、分割された有限サイズのデータの塊とみなします。この時間範囲が`window`です。

時間範囲を持つ`window`を作れば、ここに収まるデータが揃い終えるときが来ます。そのため`PCollection`がunboundedであっても、`window`であれば`GroupByKey`のような集計処理が可能になります。Beamでは常に、集計のような処理は<u>windowごと</u>に行われます。

## <span class="lhead" id="basic">ウィンドウの基礎</span>
`PCollection`がboundedでもunboundedでも、`window`に分割することが可能です。また、どんな`PCollection`であっても、少なくとも一つの`window`を持ちます。  

概要でも触れた通り、`GroupByKey`のような処理は`window`単位での処理です。なので、`GroupByKey`であれば、keyと**window**によってデータをグループ化します。

デフォルトでは、`PCollection`全体に対して単一かつ共通の`window`が設けられます。なので、unboundedな`PCollection`に対して`GroupByKey`のような処理を行うには、以下のうちの少なくとも一つを行う必要があります。

+ デフォルトでないwindowを使用する。
+ triggerの設定をする。詳しくは次章。

いずれも行わずに`GroupByKey`のような処理を行うと、エラーが発生します。

## <span class="lhead" id="note">ウィンドウに関する注意点</span>
`PCollection`に`window`を設けると、これが使われるのは<u>集計処理を行う</u>タイミングです。window変換 (windowは`PTransform`で設定します) を行うと各要素が属するwindowは決まるだけで、`PCollection`が分割されるわけではありません。

<img src="./figs/windowing-pipeline-unbounded.png" width=600>

繰り返しになりますが上の図のように、window変換は`window`の割り当てをするだけであって、`GroupByKey`のような集計処理が行われるまで<u>何も影響が現れない</u>です。なので`ParDo`単体でみると、一つ前のwindow変換はあってもなくても結果は変わりません。  
ですが次の`GroupByKey`では、設定した`window`によって結果が変わります (集計を行う間隔が5分なのか10分なのか、みたいな具合です)。

## <span class="lhead" id="bounded">Bounded PCollectionとウィンドウ</span>
もともと有限サイズであるboundedな`PCollection`であっても、windowを使うことはできます。

window変換はtimestampに基づきますが、有限サイズのデータソースから読み込む場合 (`TextIO`など)、デフォルトだと全要素に同一のtimestampが割り当てられます。つまり、明示的にtimestampを設定しない限り、window変換を行っても単一のwindowに全要素が入ります。  
timestampを割り当てるtransformがあるのですが、これは後々紹介します。

Boundedな`PCollection`に対して、window変換の有無で動作がどのように変わるかを説明します。key/valueペアを読み込んだ後に`GroupByKey`を行い、何かしらの変換を`ParDo`で行う場合を例に考えてみます。

まず、window変換を行わない場合です。  
globalなwindowに対して`GroupByKey`が処理されるため、`ParDo`はユニークなkeyごとに処理されます。

<img src="./figs/unwindowed-pipeline-bounded.png" width=400>

一方で、window変換を行う場合です。  
`GroupByKey`の結果は、keyと**window**に対してユニークになります。つまり、keyが同じであっても属するwindowが異なれば別物として扱われます。そのため、`GroupByKey`の結果に同じkeyが現れ得ますし、後続の`ParDo`も別個に処理されます。

<img src="./figs/windowing-pipeline-bounded.png" width=520>