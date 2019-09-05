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

# Sideインプット
Side inputを使うと、`ParDo`変換においてinputの`PColletion`とは別に、追加でデータが渡されます。`DoFn`での各要素の処理において、共通の値を参照させることが可です。
Side inputの形で`DoFn`にデータを渡すには、viewとやらを作ってやる必要があります。String型の`PCollection`があって、総文字数のviewを作りたければ、以下のようになります。

```java
PCollection<String> txt = ...;
PCollectionView<Integer> tot = txt
                .apply(MapElements.into(TypeDescriptors.integers()).via(x -> x.length()))
                .apply(Sum.integersGlobally().asSingletonView());
```

シングルトンについては[こちら](https://www.atmarkit.co.jp/ait/articles/0408/10/news088.html)あたりを。各スレッドやワーカーで、同じ値を参照させたいのかと思ってます（ワーカーについては自信なしです）。

Side inputで渡すデータは実行時に決定されるべきであって、ハードコードするんじゃない、って公式ガイドは言ってます（ハードコードするなら、わざわざSide inputで渡す必要は無いと思います）。基本的には、Side inputはpipelineのソースに依存して決めるものなのかと思います。

```java
import java.util.List;
import java.util.Arrays;
// beam sdk
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.TypeDescriptors;

public class Main {
    public static void main(String[] args) {
        // input text
        List<String> input = Arrays.asList(
                "To be, or not to be: that is the question: ",
                "Whether 'tis nobler in the mind to suffer ",
                "The slings and arrows of outrageous fortune, ",
                "Or to take arms against a sea of troubles, ");
        //
        Pipeline pipeline = Pipeline.create();
        // input
        PCollection<String> txt = pipeline.apply(Create.of(input));
        // singleton
        PCollectionView<Integer> tot = txt
                .apply(MapElements.into(TypeDescriptors.integers()).via(x -> x.length()))
                .apply(Sum.integersGlobally().asSingletonView());
        // with side input
        txt
                .apply(ParDo.of(
                        new DoFn<String, String>() {
                            @ProcessElement
                            public void method(@Element String e, OutputReceiver<String> o, ProcessContext c) {
                                int percent = ( e.length() * 100 ) / c.sideInput(tot);
                                o.output( String.format("%3d%% : %s",percent,e ));
                            }
                        }).withSideInputs(tot))
                .apply(TextIO.write().to("result").withoutSharding())
        ;
        pipeline.run();
    }
}
```
