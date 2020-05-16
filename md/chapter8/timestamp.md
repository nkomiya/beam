[戻る](./overview.md)

# Timestampの更新
unbounded な `PCollection`では明示的に指定せずとも、要素に自動でタイムスタンプが付けられます。タイムスタンプの付けられ方は入力ソースに依存し、必要であれば修正します。

ですが、bounded な入力ソースでは、通常 timestamp は付けられません。なので、バッチパイプラインで window を扱いたい場合は、明示的にタイムスタンプを付与する必要があります。

Timestamp の更新には`ParDo`を使います。以前は要素の出力に`OutputReceiver`の`output`メソッドを使っていましたが、timestamp の更新をする場合は、`outputWithTimestamp`を使うのみです。

```java
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Instant;

PCollection<InputT> pCollection = ...;
// timestamp の更新のみを行う ParDo
PCollection<InputT> updated = pCollection
    .apply(ParDo.of(
        new DoFn<InputT, InputT>() {
          @ProcessElement
          public void method(@Element Integer i, OutputReceiver<Integer> o) {
            o.outputWithTimestamp(i, Instant.ofEpochSecond(1546268400L));
          }
    }));
```

きちんと動作するコードは、[こちら](./codes/timestamp.md)です。