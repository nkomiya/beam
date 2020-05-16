[戻る](../overview.md)

# Process-time triggers
Process-time triggers は処理時刻、つまりパイプラインの起動時間に基づく trigger です。Watermark が絡んでこないため、動作のイメージは掴みやすいと思います。

Process-time triggers の使い道は、処理の早期発火です。たとえば、「10分間隔で DB を準リアルタイムに更新したい」といった場合に有用です。動作としては、更新のタイミングで届いているデータに対して集計が行われます。

## Triggerを設定する
Process-time triggers は`AfterProcessingTime.pastFirstElementInPane()`で設定できます。処理発火のタイミングは、`plusDelayOf`で指定します。下のサンプルでは、30秒間隔で処理が発火されます。

```java
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.Trigger.OnceTrigger;
import org.joda.time.Duration;

OnceTrigger processTimeTrigger =
    AfterProcessingTime.pastFirstElementInPane()
        .plusDelayOf(Duration.standardSeconds(30L));
```

[Event-time triggers](./event-time.md) に載せたコード例に修正を加え、処理の早期発火が行えるようにします。もともとある Event-time triggers の`withEarlyFirings`メソッドで、上で作成した Process-time triggers を指定します。

```java
import org.apache.beam.sdk.transforms.windowing.AfterPane;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;

PCollection<T> windowed = pCollection
    .apply(Window.<T>into(...)
        .withAllowedLateness(...)
        .triggering(
            AfterWatermark.pastEndOfWindow()
                //-------------------------------------- 追加
                .withEarlyFirings(processTimeTrigger)
                //------------------------------------------
                .withLateFirings(AfterPane.elementCountAtLeast(1)))
        ).accumulatingFiredPanes())
```

動作するコードサンプルは[こちら](./codes/process-time.md)です。
