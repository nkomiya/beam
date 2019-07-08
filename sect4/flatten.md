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

# Flatten
複数のdataソースがあるとき、`PCollection`も複数作ることになる。`Flatten`は`PCollection`をひとまとめにするときに使えるメソッド。`CoGroupByKey`と似ているが、こちらはkeyでひとまとめにしてくれはしないし、そもそもkey/valueペアでなくてもよい。
ただ、ひとまとめにする`PCollection`は同じ型じゃなきゃいけない。

`Flatten`をapplyする前に、まず複数の`PCollection`をlistにしておく必要がある。`PCollectionList.of`でlistの初期化をし、`and`を繋いでいく。

```java=
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;

// 何かしらのPCollectionたち
PCollection<String> col1 = ...
PCollection<String> col2 = ...
// PCollectionのlistにする
PCollectionList<String> col = PCollectionList.of(col1).and(col2);
```

これができれば、`Flatten.pCollections`をapplyしておしまい。

```java=
import java.util.Arrays;
import java.util.List;
// beam sdk
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;

public class Main {
    public static void main(String[] args) {
        List<KV<String,Integer>> a = Arrays.asList( KV.of("komiya",1) );
        List<KV<String,Integer>> b = Arrays.asList( KV.of("komiya",2 ));
        List<KV<String,Integer>> c = Arrays.asList( KV.of("komiya",3 ));

        Pipeline pipeline = Pipeline.create();
        PCollection<KV<String,Integer>> pa = pipeline.apply("Data1",Create.of(a));
        PCollection<KV<String,Integer>> pb = pipeline.apply("Data2",Create.of(b));
        PCollection<KV<String,Integer>> pc = pipeline.apply("Data3",Create.of(c));
        //
        PCollectionList.of(pa).and(pb).and(pc)
                .apply(Flatten.pCollections())
                .apply(ParDo.of(new ToString()))
                .apply(TextIO.write().to("result").withoutSharding())
        ;
        pipeline.run();
    }
    //
    static class ToString extends DoFn<KV<String,Integer>,String> {
        @ProcessElement
        public void method(@Element KV<String,Integer> e, OutputReceiver<String> o) {
            o.output( e.getKey() + " : " + e.getValue().toString() );
        }
    }
}
```