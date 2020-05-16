[Triggerの概要へ](./overview.md)

# Composite triggers
今までは、メインの trigger として`AfterWatermark.pastEndOfWindow`のみを使っていました。ですが Built-in の trigger には、これ以外にもいくつか trigger が用意されています。

`AfterWatermark.pastEndOfWindow`と同じく、動作を調整するには補助的な trigger を指定する必要があります。複数の trigger を組み合わせて処理の発火タイミングをコントロールする、という意味で`Composite trigger`と呼ばれていると思われます。

## AfterWatermark.pastEndOfWindow
Watermark が渡されたタイミングで、処理が発火されます。

以下二つの補助的な trigger を使い、挙動を調整できます。

* <u>withEarlyFiring</u>  
処理の早期発火
* <u>withLateFiring</u>  
遅延データのための trigger

### サンプル
次のサンプルコードの変数`trigger`は、window 変換の `triggering`に渡せます。

挙動としては、30秒おきに早期発火、また、遅延データが届く度に処理を発火します。

```java
import org.apache.beam.sdk.transforms.windowing.AfterPane;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.Trigger;
import org.joda.time.Duration;

Trigger trigger = AfterWatermark.pastEndOfWindow()
    .withEarlyFirings(AfterProcessingTime.pastFirstElementInPane()
        .plusDelayOf(Duration.standardSeconds(30L)))
    .withLateFirings(AfterPane.elementCountAtLeast(1));
```

## Repeatedly.forever
名前の通り、条件が満たされたタイミングで処理を発火し続けます。

以下二つの条件を設定することで、trigger の挙動を調整します。

* 繰り返しの条件  
  * 例) 30秒おきに処理を発火
* 繰り返しをやめる条件 (optional)  
  * 例) データが 5つ届いたら発火しない、など。

### サンプル
挙動としては、30秒おきに処理を発火し、データが 5つ届いた段階で処理が発火されなくなります。Pub/Sub などでこの trigger を使うと、**message は消費される**ので気をつけてください...

```java
import org.apache.beam.sdk.transforms.windowing.AfterPane;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.Repeatedly;
import org.apache.beam.sdk.transforms.windowing.Trigger;

Trigger trigger = Repeatedly.forever(
    AfterProcessingTime.pastFirstElementInPane()
        .plusDelayOf(Duration.standardSeconds(30L))
).orFinally(AfterPane.elementCountAtLeast(5));
```

動作するサンプルは、[こちら](./codes/repeatedlyForever.md)から。

## AfterEach.inOrder
この trigger は複数の trigger を、指定された順番で発火します。

```java
import org.apache.beam.sdk.transforms.windowing.AfterEach;
import org.apache.beam.sdk.transforms.windowing.AfterPane;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.Trigger;

Trigger trigger = AfterEach.inOrder(
    // 1. 3つ集まったら発火
    AfterPane.elementCountAtLeast(3),
    // 2. 5つ集まったら発火
    AfterPane.elementCountAtLeast(5),
    // 3. Watermark が渡されたタイミングで発火
    AfterWatermark.pastEndOfWindow()
);
```

動作するサンプルは、[こちら](./codes/afterEachInOrder.md)から。

## AfterFirst.of
この trigger は、指定した複数の trigger のいずれか一つが満たされたタイミングで処理を発火します。

```java
import org.apache.beam.sdk.transforms.windowing.AfterFirst;
import org.apache.beam.sdk.transforms.windowing.AfterPane;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.Trigger;

Trigger trigger = AfterFirst.of(
    // 30秒おきに発火
    AfterProcessingTime.pastFirstElementInPane()
        .plusDelayOf(Duration.standardSeconds(30L)),
    // 30秒おきに発火
    AfterPane.elementCountAtLeast(5)
);
```

動作するサンプルは、[こちら](./codes/afterFirst.md)から。

## AfterAll.of
この trigger は、指定した複数の trigger のいずれか一つが満たされたタイミングで処理を発火します。

```java
import org.apache.beam.sdk.transforms.windowing.AfterAll;
import org.apache.beam.sdk.transforms.windowing.AfterPane;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.Trigger;

Trigger trigger = AfterAll.of(
    // 30秒おきに発火
    AfterProcessingTime.pastFirstElementInPane()
        .plusDelayOf(Duration.standardSeconds(30L)),
    // 30秒おきに発火
    AfterPane.elementCountAtLeast(2)
);
```

動作するサンプルは、[こちら](./codes/afterAll.md)から。