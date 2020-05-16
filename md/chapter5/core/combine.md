[戻る](../core.md)

# Combine
`Combine`は`PCollection`の要素に対して何かしらの集計処理を行うtransformです。`Combine`には集計する"範囲"によって、二通りのパターンがある。

- `PCollection`の要素全てを集計  
&rarr; 総売上を計算したい、みたいなときに。
- `KV`型の`PCollection`で、keyごとに集計を行う  
&rarr; 支店ごとの売り上げを計算したい、みたいなときに。

集計処理のロジックは自分で決めることが多くなると思います。その際に注意すべきは、以下二点です。

1. <u>処理順序が交換可能</u>  
形式的に書くと、  
　$x \oplus y = y \oplus x$
2. <u>処理が結合則を満たす</u>  
形式的に書くと、  
　$(x \oplus y) \oplus z = x \oplus (y \oplus z)$

ダメな例は、平均値の計算です。( $x \oplus y$ を $(x+y)\div2$ と読み替えてください。)  
1つ目は大丈夫です。例えば、$x=3,~y=5$とすると、  
　$3 \oplus 5 = 5 \oplus 3 = 4$  
になりますが、二つ目はダメです。例えば、$x=2,~y=6,~z=10$とすると、  
　$(x \oplus y) \oplus z = 4 \oplus 10 = 7$  
　$x \oplus (y \oplus z) = 2 \oplus 8 = 5$  
(※ Beamで平均値の計算はできます。詳しくは後述。)

Beamは分散処理のフレームワークなので、処理される順番を決めることは基本的にできないですし、`PCollection`の一部に対して集計処理が行われることもあります。そのため、正しい集計結果を得るには、上の制約を満たしておくのが安全です。

簡単な集計処理 (min, max, sum, ... など) であれば、Beam SDKに組み込まれているtransformを使うこともできます。

## <span class="head">やってみよう - 簡易版</sapn>
`PCollection`が準備できていれば、`Combine.globally`をapplyします。これは要素全体に対して、一様に同じ集計処理を行います。

```java
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.values.PCollection;

// ... 中略 ...

PCollection<InputT> input = ...;
PCollection<OutputT> output = input
    .apply("ApplyCombineTransform", Combine.globally(new MyFn()));
```

ただ、集計処理のロジックは自分で組みます。コーディングとしては、`SerializableFunction`インターフェースを実装したクラスを作ります。必要になるのは、`PCollection`の**一部**に対して集計処理を行うメソッドのOverrideだけです。

```java
import org.apache.beam.sdk.transforms.SerializableFunction;

// ... 中略 ...

class MyFn implements SerializableFunction<Iterable<InputT>, OutputT> {
    @Override
    public OutputT apply(Iterable<InputT> in) { ... }
}
```

例として、整数の`PCollection`に対して総和を取る[サンプル](./codes/combine_globally.md)です。

## <span class="head">やってみよう - もっと簡易版</span>
総和のような簡単な集計処理ならば、built-inのtransformを使うのが簡単です。
  
例えば、`Sum.integersGlobally`を使うと、整数型の`PCollection`に対しての総和が計算できます。  
(公式ガイドだと`Sum.SumIntegerFn`ですが、無くなったぽいです。)

```java
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.values.PCollection;

// ... 中略 ...

PCollection<Integer> input = ...;
PCollection<Integer> summed = input
    .apply("CalculateSummation",Sum.integersGlobally());
```

動作するコード全体は[こちら](./codes/sum_integers.md)です。

## <span class="head"> やってみよう - 発展版</span>
前処理、もしくは後処理を必要とする集計処理をしたければ、`CombineFn`のサブクラスを作り、これを`Combine.globally`に渡します。  
使用例としては平均値の計算です。Inputの要素数と総和を計算をし、後処理として総和を要素数で割れば平均値が出せます。

`CombineFn`サブクラスの定義では、３つの型引数が現れます。

```java
import org.apache.beam.sdk.transforms.Combine.CombineFn;

// ... 中略 ...

class MyCombineFn extends CombineFn<InputT, AccumulatorT, OutputT> { 
  // ... 後述 ...
}
```

それぞれ、

1. InputT  
集計前`PCollection`の型
2. AccumulatorT  
集計の途中結果を保持させるための型。`Integer`とかでもOKです
3. OutputT  
出力後`PCollection`の型

です。applyの仕方は前と同じです。

```java
PCollection<InputT> input = ...;
PCollection<OutputT> output = input.apply("ApplyCombine",
    Combine.globally(new MyCombineFn()));
```

道のりは少し長いですが、集計の途中結果を保持用のクラスの作り方、および`CombineFn`サブクラス内でOverrideする必要がある４つのメソッドについて説明します。

### 0. "集計係"の定義
行う集計処理によっては、集計途中の結果を保持するクラスを定義します。

平均の計算だと、要素数・総和を保持しなければいけないので、カスタムクラスを作ります。

```java
static class Accumulator implements Serializable {
  int sum;
  int items;

  Accumulator() {
    this.sum = 0;
    this.items = 0;
  }

  @Override
  public boolean equals(Object o) { ... }
}
```

集計係はシリアル化可能でなければならず、最低限の労力で実行できるようにするには以下二つが必要です。

1. java.io.Serializableの継承
2. equalsメソッドの定義  
&rarr; equalsが無いと、警告がたくさん出ます

**1. "集計係"の作成**  
`createAccumulator`をOverrideします。  
集計処理の途中結果を保持させるためのインスタンス (集計係) を、初期化した上で返すようにします。前処理が必要ならば、ここで行います。

**2. "集計係"への要素の追加**  
`addInput`をOverrideします。  
どのように新規要素を集計係に追加させるか、を定義します。

**3. "集計係たち"の集計**  
`mergeAccumulator`をOverrideします。  
分散処理のため、集計係が複数できることもあり得ます。`mergeAccumulator`は、複数できた集計係をとりまとめ、新たな集計係として返すメソッドになります。

**4. 最終結果の出力**  
`extractOutput`をOverrideします。  
transformの最後で呼ばれる、集計結果を返すためのメソッドです。後処理が必要ならば、ここで定義します。

全体として、見た目は以下のようになります。

```java
class MyCombineFn
    extends CombineFn<InputT, MyCombineFn.Accumulator, OutputT> {
  // 集計係
  private static class Accumulator implements Serializable {
    // コンストラクタ
    private Accumulator() { ... }

    @Override
    public boolean equals(Object o) { ... }
  }

  @Override
  public Accumulator createAccumulator() { ... }

  @Override
  public Accumulator addInput(Accumulator a, InputT i) { ... }

  @Override
  public Accumulator mergeAccumulators(Iterable<Accumulator> accumulators) { ... }

  @Override
  public OutputT extractOutput(Accumulator a) { ... }
}
```

平均値の計算を計算をするサンプルを作ったので、[こちら](./codes/combineFn.md)を確認してください。

## <span class="head">やってみよう - keyごとの集計処理</span>
ここまでで、`PCollection`全体に集計処理を行う方法として、

1. `SerializableFunction`を使う
2. Built-inのtransformを使う
3. `CombineFn`サブクラスを使う

の３つをやりました。keyごとに集計をするには、applyするメソッドを変えるだけでOKです。

`SerializableFunction`, `CombineFn`の場合だと、本質的には以下の変更のみです。

```java
import org.apache.beam.sdk.transforms.Combine;

//input.apply(Combine.globally(...));
input.apply(Combine.perKey(...));
```

Built-inのtransformを使う場合も、だいたい同じです。

```java
import org.apache.beam.sdk.transforms.Sum;

//input.apply(Sum.integersGlobally());
input.apply(Sum.integersPerkey());
```

以下、コードサンプルです。

+ [SerializableFunction](./codes/combine_perkey.md)
+ [Sum.integersPerkey](./codes/sum_integers_perkey.md)
+ [CombinFn](./codes/combineFn_perkey.md)


## <span class="head">諸注意</span>
`Combine`は要素全体に作用するため、[GroupByKey](./groupbykey.md#comment)などで出てきた、処理を発火するタイミングの問題を考えなければいけません。

それに加えて、`Combine`は集計結果を**単一の値**として返すため、`PCollection`が空の場合の挙動 (デフォルト値) を決めてあげる必要があります。Boundedな入力ソースでwindowを何も指定しない場合、デフォルト値は`Combine`に使う関数に依存します。例えば`Sum`であれば、入力が空の場合0を返します。  
入力が空の場合に`Combine`の出力も空にしたければ、以下のように`withoutDefaults`をつけます。

```java
// Beam SDKの関数を使う場合
input.apply(Max.integersGlobally().withoutDefaults());

// Combineを使う場合
input.apply(Combine.globally( ... ).withoutDefaults());
```

`CombineFn`では、デフォルト値をカスタマイズできます。追加で`defaultValue`をOverrideするだけです。詳しくは、[こちら](./codes/combineFn_withDefault.md)のサンプルを確認してください。

```java
@Override
public OutputT defaultValue() { ... }
```

> #### unbounded PCollectionとデフォルト値
> unbounded `PCollection`で`Combine`を使う場合、以下のいずれかが必要です。  
> (正確には、windowをデフォルトから変更している場合です。)
> 
> 1. withoutDefaultsを付ける  
> Beamがデフォルト値を返してくれないため明示的にデフォルト値を使わない、と宣言する必要があります。
> 2. asSingletonViewを付ける  
> 詳しくは後述しますが、`ParDo`において追加の副入力を用いる際に使います。これを使うことで、空のInputに対してデフォルト値を返すようになるようです。