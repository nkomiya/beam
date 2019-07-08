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

# GroupByKey
`GroupByKey`メソッドは、再び[Map/Shuffle/Reduce](https://enterprisezine.jp/dbonline/detail/4440)モデルでいうところのShuffleにあたるそう。Inputの`PCollection`としてmultimap（key/valueペアだけど、同じkeyが登場し得るやつ）をとり、同じkeyをひとまとめにしたkey/valueペアの`PCollection`を出力する。

Guideが例としてあげてるのは顧客の注文リスト。氏名・住所・商品名... etcがあったとき、注文者の住所をkey、その他の値をvalueとしてひとまとめにする処理。

簡単なinput/outputの例を見て期待する動作を説明します。

input : key/valueペアだけど、keyに重複あり。

```
cat, 1
dog, 5
and, 1
jump, 3
tree, 2
cat, 5
dog, 2
and, 2
cat, 9
and, 6
```

output: key/valueペアで、keyに重複無し。valueはリストに突っ込まれます。

```
cat, [1,5,9]
dog, [5,2]
and, [1,2,6]
jump, [3]
tree, [2]
```

こういう例だと足し合わせたくもなりますが、`GroupByKey`はあくまでグループ化をするためのメソッドで、keyをuniqueにすることが目的です。とりあえず、codeを貼ります。

```java
import java.util.Arrays;
import java.util.List;
// pipeline & options
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
// transform
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.io.TextIO;
// for GroupByKey
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.transforms.GroupByKey;

public class Main {
    public static void main(String[] args) {
        // ややこいですが今までと同じ要領で、ListからPCollectionを作ります
        // つけなくても動きますが、final修飾子をつけて変更不可にしてます
        final List<KV<String, Integer>> input =
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
        Pipeline pipeline = Pipeline.create();
        //
        pipeline
                .apply(Create.of(input))
                .apply(GroupByKey.create())
                .apply(ParDo.of(
                        new DoFn<KV<String, Iterable<Integer>>, String>() {
                            @ProcessElement
                            public void mymethod(@Element KV<String, Iterable<Integer>> e, OutputReceiver<String> o) {
                                String result = e.getKey() + ", [";
                                for (Integer i : e.getValue()) {
                                    result += String.format("%s, ", i.toString());
                                }
                                result = result.substring(0, result.length() - 2) + "]";
                                o.output(result);
                            }
                        }
                ))
                .apply(TextIO.write().to("result"));
        //
        pipeline.run();
    }
}
```

19行目のInputのList作成とか38行目以降の出力処理とかがうるさいですけど、`GroupByKey`のポイントのみ説明します。

- Beamではkey/valueペア用に、`KV<K,V>`型があります  
Kがkeyの型、Vがvalueの型です。`KV.of`で一つのkey/valueペアが作れる
- `GroupByKey`には`KV`型を要素に持つ`PCollection`を渡すだけ