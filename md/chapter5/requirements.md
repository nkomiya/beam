[topへ](../index.md)  
[PTransformの概要へ](./ptransform.md)

# ユーザ定義関数における注意点
ユーザ定義関数を作る際は、分散処理の特性に気を使う必要があります。処理は複数のワーカーで独立に、かつお互いの状態に無関係に行われます。  
この節で触れることは、`DoFn`, `CombineFn`, `WindowFn`において一般的にあてはまります (`WindowFn`については、後の章を参照)。具体的に注意することは、以下の３つです。

+ [シリアル化](https://www.ne.jp/asahi/hishidama/home/tech/java/serial.html)可能であること
+ スレッド互換であること  
マルチスレッドでも問題なく動く、の意味です。  
用語を詳しく知りたければ[こちら](https://www.ibm.com/developerworks/jp/java/library/j-jtp09263/index.html)が参考になるかもです。
+ 必須ではないが、処理が冪等であること

それぞれ、詳しく見ていきます。

## <span class="head">シリアル化可能性</span>
基本的に処理はワーカー間で独立なのですが、ワーカー間で通信が必要になることもあります (`Combine`で各ワーカーの集計結果をまとめるときなど)。  
動作の詳細としては、`DoFn`などのサブクラスのインスタンス (関数オブジェクト) が各ワーカーで生成され、必要に応じてシリアル化された関数オブジェクトが別のワーカーへ転送されます。  
ユーザ定義関数はクラスの継承やインターフェースの実装で作りますが、その際にシリアル化できないメンバを追加するとビルドは通りません。

以下、頭に入れておいた方が良さそうな点になります。

+ [Transient修飾子](http://java-code.jp/126)  
transient修飾子を付けたメンバはシリアル化されないため、ワーカーへ渡されません。  
詳細は[こちら](./codes/transient.md)のコードを参照してください。
+ シリアル化する前に大量のデータの読み込みは避ける
+ 関数オブジェクト  
  &#10004; 関数オブジェクトの間でデータ共有は不可。  
  &#10004; apply後の関数オブジェクトの変更は、影響無し
+ 匿名クラス  
staticでないコンテキストで作成された匿名クラスのインスタンスは、暗黙的にそのクラスへのポインタを持っています。これもシリアル化の対象になることに注意するように、とのことです。

## <span class="head">スレッド互換性</span>
ここは自分でマルチスレッド処理を実装するつもりがないならば、読み飛ばしても問題ないです。

マルチスレッド処理を自分で実装しない限り、各関数オブジェクトの処理はシングルスレッドで捌かれます。Beamはスレッドセーフではないので、独自で同期処理をしなければいけないようです。    

また、関数オブジェクト内のstaticメンバはワーカーに渡されないそうです。staticメンバは使用可能なので、staticメンバを参照するときは関数オブジェクト間でデータのやりとりがあるのかな、と思ってます。

## <span class="head">冪等性</span>
必須ではないのですが関数オブジェクトが冪等であること、つまり何度呼び出されても同じ結果を返すようにしておくことが推奨されています。

Beamでは関数オブジェクトの呼び出し回数の保証はしないそうです（一時的な通信エラーなどでリトライされることなどを指しているのかと思います）。

そのため、基本的には処理が複数回行われても問題ないようなコードにしておく方が安全です。