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

# Composite transform
コードの可読性を上げる、とかPipeline graphを美しくする（わたしの主観）、とか使いまわしをする、って意味でComposite transform（合成変換って呼びますね）が便利です。
これは、複数の`PTransform`からなるユーザ定義の`PTransform`です。要素の合計を求めて出力みたいなときに、(1)合計の計算、(2)出力用に文字列に整える、の二つをまとめた`PTransform`を作る、みたいなノリです。
Beam SDKが作ってくれてる合成変換は[org.apache.beam.sdk.transform](https://beam.apache.org/releases/javadoc/2.13.0/index.html?org/apache/beam/sdk/transforms/package-summary.html)の中を見てくれ、ってなってます。

## <span class="head">合成変換を作ってみる</span>
特に難しいことはない。`PCollection`を受け取って、`PCollection`を返す`PTransform`のサブクラスを作れば良い。  
`PTransform`のサブクラスの`expand`メソッドをオーバーライドする。`expand`の中の処理が順に実行される感じです。

```java
static class MyTransform extends PTransform<PCollection<InputT>,PCollection<OutputT>> {
    @Override
    public PCollection<OutputT> expand(InputT input) {
       ... 処理 ...
       return [OutputT型のPCollection];
    }
}
```

作った合成変換をapplyするには、サブクラスのインスタンスをapplyに渡せば良い。

```java
apply( new MyTransform() )
```

code例として、公式ガイドに載ってる単語数登場回数をカウントするcodeの劣化版。

```java
import java.util.List;
import java.util.Arrays;
// beam sdk
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.FlatMapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

public class Main {
    public static void main(String[] args) {
        List<String> input = Arrays.asList(
                "To be, or not to be: that is the question: ",
                "Whether 'tis nobler in the mind to suffer ",
                "The slings and arrows of outrageous fortune, ",
                "Or to take arms against a sea of troubles, ");
        Pipeline pipeline = Pipeline.create();
        //
        pipeline
                .apply(Create.of(input))
                .apply(new CountWords())
                .apply(ParDo.of(
                        new DoFn<KV<String, Long>, String>() {
                            @ProcessElement
                            public void method(ProcessContext ctx) {
                                String key = ctx.element().getKey();
                                Integer value = ctx.element().getValue().intValue();
                                ctx.output(key + " : " + value.toString());
                            }
                        }))
                .apply(TextIO.write().to("result").withoutSharding())
        ;
        pipeline.run();
    }

    static class CountWords extends PTransform<PCollection<String>, PCollection<KV<String, Long>>> {
        @Override
        public PCollection<KV<String, Long>> expand(PCollection<String> lines) {
            /** FlatMapElements:
             * Iterableを返すSerializableFunctionを渡すと
             * Iterableの中身をばらしてくれる。
             */ 
            /** Count.perElement:
             * PCollection<T>を受け取り、
             * 要素の登場回数を数えてkey/valueペアを返す
             */
            return lines
                    .apply(FlatMapElements
                            .into(TypeDescriptors.strings())
                            .via(new SerializableFunction<String, Iterable<String>>() {
                                public Iterable<String> apply(String input) {
                                    return Arrays.asList(input.split(" "));
                                }
                            })
                    )
                    .apply(Count.<String>perElement());
        }
    }
}
```

合成変換のinput/outputは複数の`PCollection`でもよろしい。きちんと型宣言さえ出来てさえいれば、ですが。

例を考えるのが面倒だったので、複数の`PCollection`を"変換が一つの合成変換"を使って結合する例。

```java
import java.util.Arrays;
import java.util.List;
// beam
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;

public class Main {
    public static void main(String[] args) {
        List<String> inputA = Arrays.asList(
                "To be, or not to be: that is the question: ",
                "Whether 'tis nobler in the mind to suffer ");
        List<String> inputB = Arrays.asList(
                "The slings and arrows of outrageous fortune, ",
                "Or to take arms against a sea of troubles, ");
        Pipeline pipeline = Pipeline.create();
        //
        PCollection<String> colA = pipeline.apply("A", Create.of(inputA));
        PCollection<String> colB = pipeline.apply("B", Create.of(inputB));
        PCollectionList.of(colA).and(colB)
                .apply(new Join())
                .apply(TextIO.write().to("result").withoutSharding())
        ;
        pipeline.run();
    }

    static class Join extends PTransform<PCollectionList<String>, PCollection<String>> {
        @Override
        public PCollection<String> expand(PCollectionList<String> input) {
            return input.apply(Flatten.pCollections());
        }
    }
}
```
