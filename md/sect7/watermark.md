[topへ](../index.md)  
[Windowへ戻る](./overview.md)

# Watermarkと遅延データ
## Overview
基本的にどんなデータ処理システムであっても、レイテンシやシステム時間のズレなどの影響で、以下二つの時刻にはラグが発生します。

1. イベント時刻  
データが発生した時刻
2. 処理時刻  
データがパイプラインに到着する時刻

さらにイベントが発生した順序と、データがパイプラインに到着する順序が同じである保証もありません。このような問題のため、Beam では watermark を追跡します。watermark はいつ window に全データが揃うかを表す印で、**入力ソースに依存します**。watermark が window に渡されると、それ以降のデータは遅延データとして扱われます。

Beam のデフォルトでは、遅延データは**捨てられます**。次章で扱う trigger を使うことで、遅延データの取り扱いや処理の発火タイミングを柔軟に変更することができます。

気をつけるべきは、デフォルトでは window が閉じたときに処理が発火されますが、window が閉じるのは watermark が window に渡されたときです。  
たとえば、30分の時間幅を持つ fixed-time windows で、watermark が windowの終端から 15分後に渡されるとします。このとき、一つの window が開いている（パイプラインがデータを受け付ける）のは 45分間であって、**30分間ではない**です。

## 遅延データを許容させる
window 変換を行う際に、`withAllowedLateness`メソッドを呼び出すことで、遅延データを取り扱うことができます。

下のサンプルでは watermark が window に渡った後、1分間の遅れを許容します。

```java
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;

PCollection<T> pCollection = ...;
PCollection<String> windowed = pCollection
    .apply(Window.<String>into(FixedWindows.of(Duration.standardMinutes(30L)))
        .withAllowedLateness(Duration.standardMinutes(1L)));
```
