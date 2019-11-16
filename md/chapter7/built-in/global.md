[戻る](../built-in.md)
# Global window
## Overview
既に触れている通り、デフォルトでは全体に共通で単一の window が用いられます。これは global window と呼ばれていて、入力ソースが有限であれば問題なく使えます。

入力ソースが無限 (unbounded) であっても、global window を指定できます。ただ、`GroupByKey`のような集計処理は、デフォルトでは全データが集まってから処理が実行されます。unbounded では全データが集まることはないので、処理の発火タイミングを変更しない限りビルドに失敗します。

## Windowの設定
ただ global window を明示的に指定するだけなので、難しいことはないと思います。

```java
import org.apache.beam.sdk.transforms.windowing.GlobalWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;

PCollection<T> pCollection = ...;
PCollection<T> windowed = pCollection
    .apply("Windowing", Window.<T>into(new GlobalWindows()));
```

コードサンプルは[こちら](./codes/global.md)です。`PCollection`の要素に擬似的なタイムスタンプを付与し、window の設定をしています。