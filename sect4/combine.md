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

# Combine
`combine`は`PCollection`の要素を結合するためのメソッド。`Combine`には作用対象によって、二通りのパターンがある。

- `PCollection`の要素を集計  
整数型の`PCollection`を足し算する、みたいな
- `KV`型の`PCollection`で、valueごとに集計  
valueがリストだったりしたときに。

`combine`を行うには結合の仕方を自分で決めることになる。`PCollection`の値を安全に[シリアル化](http://www.ne.jp/asahi/hishidama/home/tech/java/serial.html) / キャッシュするために

- 各要素への処理順序が交換可能
- "連想的"であること  
&rarr; 要素に値の関係はMapであってほしい

を保証しろ、っていってる。Beamでは、inputのdataは複数のworker（処理を行うやつ）に渡る。workerはlocalだったらスレッドだし、dataflowとかならいくつかのVMに渡る（のか？）。要素をもれなく処理するため、`Combine`がdatasetの一部に作用したり、一つの要素に複数回処理がされることもある。

結合の仕方については、Beam SDK組み込み関数でmin, max, sumみたいな処理が作られてたりします。

## <span class="head">やってみよう - 簡易版</sapn>
例として、inputの要素が整数のリストで、総和の計算を行うことを考える。

まず、`combine`で行う結合処理部分について。

```java
public static class SumOver implements SerializableFunction<Iterable<Integer>, Integer> {
    @Override
    public Integer apply(Iterable<Integer> in) {
        Integer s = 0;
        for (Integer n : in) {
            s += n;
        }
        return s;
    }
}
```

こんな感じで、`SerializableFunction<I,O>`の`apply`メソッドをOverrideしたサブクラスを作ります。`I`がinputの`PCollection`の型、`O`がoutputの`PCollection`の型です。

作ったサブクラスのインスタンスを`Combine.globally`に渡せばokです。

```java
import java.util.List;
import java.util.ArrayList;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.apache.beam.sdk.io.TextIO;

public class Main {
    public static void main(String[] args) {
        Pipeline pipeline = Pipeline.create();
        List<Integer> nums = new ArrayList<Integer>() {
            { for( int i=1; i<=100; i++) { add(i); } }
        };
        pipeline
                .apply(Create.of(nums))
                .apply(Combine.globally(new SumOver()))
                .apply(MapElements.into(TypeDescriptors.strings()).via((Integer in)->in.toString()))
                .apply(TextIO.write().to("result"));
        pipeline.run();
    }

    public static class SumOver ... 略
}
```

## <span class="head">やってみよう - もっと簡易版</span>
何度か触れている通り、Beam SDKには良く用いられそうな変換処理についてはBeam SDKの組み込み関数がある。整数型の`PCollection`に対して和を取るだけなら、`Sum.integersGlobally`をapplyするだけで十分。
公式ガイドだと、`Sum.SumIntegerFn`があるってなってるけど、新しいSDKのバージョンだと消えてるっぽいです。

code例

```java
import java.util.List;
import java.util.ArrayList;
// beak sdk
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.values.TypeDescriptors;

public class Main {
    public static void main(String[] args) {
        Pipeline pipeline = Pipeline.create();
        List<Integer> nums = new ArrayList<Integer>(100) {{
            for (int i = 1; i <= 100; i++) {
                add(i);
            }
        }};
        //
        pipeline
                .apply(Create.of(nums))
                .apply(Sum.integersGlobally())
                .apply(MapElements.into(TypeDescriptors.strings()).via((Integer x) -> x.toString()))
                .apply(TextIO.write().to("result"))
        ;
        pipeline.run();
    }
}
```

## <span class="head"> やってみよう - 発展版</span>
複雑な結合処理をしたければ、`CombineFn`のサブクラスを作って、`Combine.globally`に作ったサブクラスのインスタンスを渡す。"複雑"な、と言っている意味は、

- 前処理、もしくは後処理を必要とする
- key/valueペアで、keyによって結合処理を変えたい

みたいな場合。`CombineFn`を使うには、４つのstepが必要。平均の計算を例に各stepの説明をします。

1. `createAccumulator`："寄せ集め係"の生成  
平均の計算では`PCollection`の要素ごとに処理していく際、合計と要素数を順次更新していく。この途中結果をとっておく用の変数（正確にはインスタンス）を返すためのメソッド。
ちなみに、前処理はこの段階で行う。合計値・要素数ともに0で初期化しておく、みたいな。
2. `addInput`："要素追加係"  
`PCollection`の要素が流れ込んできたときの処理を定義する。平均なら、合計に値の追加と要素数のインクリメントをする。
3. `mergeAccumulator`："集計係"  
Beamは並行処理が前提。なので、寄せ集め係が複数体できる。こいつらをまとめてあげる係を作んなきゃいけない。
4. `extractOutput`："出力係"  
最後に集計結果に処理を加え、新たなPCollectionを返すためメソッドを定義する。PTransformの最後に一度だけ呼ばれるそう。

code例

```java
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
// beam sdk
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.Combine.CombineFn;
import org.apache.beam.sdk.transforms.Create;

public class Main {
    public static void main(String[] args) {
        Pipeline pipeline = Pipeline.create();
        // input
        List<Integer> nums = new ArrayList<Integer>(100) {{
            for (int i = 1; i <= 100; i++) {
                add(i);
            }
        }};
        //
        pipeline
                .apply(Create.of(nums))
                .apply(Combine.globally(new MyAverageFn()))
                .apply(TextIO.write().to("result"));
        //
        pipeline.run();
    }
}

// CombineFnの型引数は、Inputの要素の型、集計係、Outputの型、の順番
class SumOver extends CombineFn<Integer, SumOver.Accum, String> {
    // Serializableの継承が必要
    public static class Acc implements Serializable {
        // 初期化
        int sum = 0;
        int items = 0;

        @Override
        public boolean equals(Object other) {
            if (other == null) return false;
            if (other == this) return true;
            if (!(other instanceof Acc)) return false;

            Acc o = (Acc) other;
            if (this.sum != o.sum || this.items != o.items ) {
                return false;
            } else {
                return true;
            }
        }
    }

    @Override
    public Acc createAccumulator() {
        return new Acc();
    }

    @Override
    public Acc addInput(Acc a, Integer i) {
        a.sum += i;
        a.items++;
        return a;
    }

    @Override
    public Acc mergeAccumulators(Iterable<Acc> accums) {
        Acc merged = new Acc();
        for (Acc a : accums) {
            merged.sum += a.sum;
            merged.items += a.items;
        }
        return merged;
    }

    @Override
    public String extractOutput(Acc a) {
        double ave = (double)a.sum / a.items;
        return "Average: " + String.valueOf(ave);
    }
}
```

公式ガイドのコード例だと、ビルドは通るが実行時にエラーを吐く...
要素集計係（上の例だとAccクラス）がシリアル化可能である必要があるっぽく、java.io.Serializableの継承も必要です（もしくはcoderの指定）。継承するだけだと実行は通りますが、equalsメソッドを定義しないと警告メッセージがたくさん出ます...  
どういう仕組みでシリアル化してるか分からず、なぜequalsが必要かも分からないです...

## <span class="head">やってみよう - keyごとでの結合</span>
先の`GroupByKey`変換ではkeyの重複があったとき、valueがlistにまとめられました。listにするのではなく、keyごとに合計や平均をとりたい、みたいな変換も可能です。
importすべきクラスは、

+ 合計 : org.apache.beam.sdk.transforms.Sum
+ 最大 : org.apache.beam.sdk.transforms.Max
+ 平均 : org.apache.beam.sdk.transforms.Mean

みたいな。`combine`のやり方は２パターンあって、

```java
[ Output ] = [ Input ].apply(Sum.integersPerKey());
```

のように、型ごとにメソッドが定義されてる場合と、

```java
[ Output ] = [ Input ].apply(Mean.<K,V>perKey(()))
```

のように型引数による指定でokな場合がある。Kがkeyの型、Vがvalueの型です。この型の指定法は[ここ](http://takahashikzn.root42.jp/entry/20090629/1246247577)が参考になります。

合計を取る場合のcode例

```java
import java.util.Arrays;
import java.util.List;
// beam sdk
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.values.KV;

public class Main {
    public static void main(String[] args) {
        Pipeline pipeline = Pipeline.create();
        // input
        List<KV<String, Integer>> input =
                Arrays.asList(
                        KV.of("cat", 1),
                        KV.of("dog", 5),
                        KV.of("and", 1),
                        KV.of("jump", 3),
                        KV.of("tree", 2),
                        KV.of("cat", 5),
                        KV.of("dog", 2),
                        KV.of("and", 2),
                        KV.of("cat", 9),
                        KV.of("and", 6)
                );
        // key/value pairの作成
        Pipeline pipeline = Pipeline.create();
        pipeline
                .apply(Create.of(input))
                .apply(Sum.integersPerKey())
                .apply(ParDo.of(new IntegerToStringFn()))
                .apply(TextIO.write().to("result").withoutSharding())
        ;
        pipeline.run();
    }

    static class IntegerToStringFn extends DoFn<KV<String, Integer>, String> {
        @ProcessElement
        public void method(@Element KV<String, Integer> in, OutputReceiver<String> out) {
            String key = in.getKey();
            Integer val = in.getValue();
            out.output(key + ":" + val.toString());
        }
    }
}
```
