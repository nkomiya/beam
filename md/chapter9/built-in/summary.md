[戻る](../built-in.md)

# この節のまとめ
この節では Built-in のトリガーとして、**Event-time triggers**, **Process-time triggers**, **Data-driven triggers** の 3つを説明しました。

Trigger は window 変換の際に設定しました。

```java
PCollection<T> pCollection = ...;
PCollection<T> windowed = pCollection
    .apply(Window.<T>into( [Window変換] )
        .withAllowedLateness( [許容する遅延] )
        .triggering( [トリガー] ).accumulatingFiredPanes());
```

`triggering` の中では、メインとなるトリガーに補助的なトリガーを付け加えて、動作を調整します。

### メインの trigger を指定
これまで扱ったものは、window に watermark が渡された際に処理を発火する trigger のみです。この trigger を指定するには、`AfterWatermark.pastEndOfWindow()`を使います。

```java
.triggering(
    AfterWatermark.pastEndOfWindow()
        .withEarlyFirings( [早期発火用の trigger] )
        .withLateFirings( [遅延データ用の trigger] )
).accumulatingFiredPanes());
```

この trigger は、`withEarlyFirings`, および`withLateFirings`で、watermark が渡される前後に処理を発火させることができます。どちらも trigger インスタンスを引数にとります。

### 補助的な trigger を指定
`withEarlyFirings`などで指定する trigger を、メインの trigger の動作を調整する、という意味で "補助的な trigger" とします。

`withEarlyFirings`などは`OnceTrigger`を引数に取りますが、これまでに説明した "補助的な trigger" は全て`OnceTrigger`の派生クラスです。

```java
// Data-driven trigger
OnceTrigger subTrigger = AfterPane.elementCountAtLeast(1);
```