[戻る](../built-in.md)

# Event-time triggers
Beam のデフォルトの trigger になります。名前的にはイベント時刻に基づく trigger ですが、実態としては watermark に基づく trigger です。

デフォルトの挙動は、window に watermark が渡った段階で一度のみ処理が発火され、遅延データ（Watermarkが渡った後に届くもの）は許されません。


## Triggerを設定する
初回であるため、まず Beam における trigger の設定方法について説明します。Trigger は window を設定するときに指定します。

```java
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;

PCollection<T> pCollection = ...;
PCollection<T> windowed = pCollection
    .apply(Window.<T>into(...)
        .withAllowedLateness(...)
        // ここで trigger の設定をする
        .triggering(...).accumulatingFiredPanes());
```

さらに、デフォルトでない Trigger を設定する場合、以下二つを明示的に指定する必要があります。

* <u>遅延データの取り扱い</u>  
`withAllowedLateness`で指定できる、watermark が渡った後にどの程度待つか。  
* <u>window の集積モード</u>  
この説明は後に回します。上のコードでは、`accumulatingFiredPanes`にあたります。

### Event-time triggersの設定方法
上のコードでの `.triggering()`の中身について説明します。

Watermark ベースの trigger は`AfterWatermark.pastEndOfWindow()`で作成します。これは watermark が渡った段階で発火される trigger ですが、`withLateFirings`で遅延データが届いた際の挙動を設定できます。

下の例では遅延データが届いた場合、すぐに処理を発火するような trigger の設定になります。

```java
import org.apache.beam.sdk.transforms.windowing.AfterPane;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;

PCollection<T> windowed = pCollection
    .apply(Window.<T>into(...)
        .withAllowedLateness(...)
        // trigger の設定
        .triggering(
            AfterWatermark.pastEndOfWindow()
                // 遅延データが届いたら、即発火
                .withLateFirings(AfterPane.elementCountAtLeast(1)))
        ).accumulatingFiredPanes())
```

コード全体のサンプルは[こちら](./codes/event-time.md)です。ローカル実行の場合 Watermark が渡るのが遅いせいか、処理が発火されないです...。