[戻る](../built-in.md)

# Fixed time windows
## Overview
Fixed time windows は**固定のwindow幅**、**重なりを持たない** window が作られる window変換です。

<img src="./figs/fixed-time-windows.png" width=600>


上の図では、30 秒の固定幅を持つ window が作成され、`PCollection`の要素はどれか一つの window に属することになります。

windowの左端は window に含まれ、右端は含まれません。つまり、上の図のパイプラインを 00:00:00 ちょうどに起動した場合、各要素の window への割り当ては次のようになります。

|window|Timestamp の範囲    |
|:----:|:-----------------:|
|0     |00:00:00 - 00:00:29|
|1     |00:00:30 - 00:00:59|
|2     |00:01:00 - 00:01:29|

## Windowの設定
Window の設定は、`PCollection`に`Window.<T>into`を作用させるのみです。

```java
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;

PCollection<T> pCollection = ...;
PCollection<T> windowed = pCollection
    // 30 秒間隔の window を設定
    .apply(Window.<T>into(FixedWindows.of(Duration.standardSeconds(30L))));
```