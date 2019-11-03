[戻る](../built-in.md)

# Sliding time windows
## Overview
Sliding time windows も Fixed time windows と同じで、固定の時間幅を持つ window が作られます。ですが Sliding time windows では、各 window に重なりを持たせることができます。

<img src="./figs/sliding-time-windows.png" width=600>

上の図では 60秒の window 幅で、30秒おきに window が作成されます。そのため、各 window は 30秒重なり、`PCollection`の要素は基本的に二つの window に属することになります。

Sliding time windows は、「直近 1日の検索ワードランキングを 10分おきに更新する」みたいな場合に有用です。  
Fixed time windows でやろうとすると、更新間隔を短くするとデータを十分に溜めることができず、データを十分に溜めようとすると更新間隔が長くなってしまいます。

## Windowの設定
Windowの設定方法は、Fixed time windows とほぼ同じです。  
ですが Sliding time windows では<u>window 幅</u>と、 <u>window を切る時間間隔</u>の二つを指定します。

```java
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;

PCollection<T> pCollection = ...;
PCollection<T> windowed = pCollection
    // 60秒の window 幅で、30秒おきに window を設ける
    .apply(Window.<String>into(SlidingWindows.of(Duration.standardSeconds(60L))
        .every(Duration.standardMinutes(30L))));
```