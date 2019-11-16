[topへ](../index.md)  
[Triggerの概要へ](./overview.md)

# 集積モード
Window に trigger を設定する場合、window の**集積モード**を指定する必要があります。

Process-time triggers などのように、trigger は一つの window に対して複数回発火されることがあります。この際、処理が行われたデータの取り扱いを決めるのが集積モードです。

1. <u>AccumulatingFiredPane</u>  
処理を行ったデータを「window に保持させる」
2. <u>DiscardingFiredPane</u>  
処理が行われたデータは「捨てる」

例を使って、二つの集積モードを説明します。10分間隔の Fixed time windows を設け、データが 3つ届いた段階で処理の早期発火する Data-driven triggers を設定しているとします。

> <img src="./figs/trigger-accumulation.png" width=600>  
> [https://beam.apache.org/images/trigger-accumulation.png](https://beam.apache.org/images/trigger-accumulation.png)

Window 0 にはデータが 9つ入るので、計3回処理が早期発火されます。

### AccumulatingFiredPane
この集積モードでは window が閉じるまで、処理を行ったデータを保持します。こまめに集計結果のアップデートをかけたい場合に便利です。動作のイメージとしては、以下の通りです。

```
1st trigger    : [5, 8, 3]
2nd trigger    : [5, 8, 3, 15, 19, 23]
3rd trigger    : [5, 8, 3, 15, 19, 23, 9, 13, 10]

After watermark: [5, 8, 3, 15, 19, 23, 9, 13, 10]
```

### DiscardingFiredPane
この集積モードでは、処理が行われたデータを捨てます。各データに対して、一回だけ処理を行いたい場合に便利です。動作のイメージとしては、以下の通りです。

```
1st trigger    : [5, 8, 3]
2nd trigger    :          [15, 19, 23]
3rd trigger    :                      [9, 13, 10]

After watermark: []
```

## 集積モードを指定する
既出なので、細かくは説明しません。

### AccumulatingFiredPane
```java
PCollection<T> pCollection = ...;
PCollection<T> windowed = pCollection
    .apply(Window.<T>into(...)
        .withAllowedLateness(...)
        .triggering(...).accumulatingFiredPane())
```

[コードサンプル](./codes/accumulating-mode.md)

### DiscardingFiredPane
```java
PCollection<T> pCollection = ...;
PCollection<T> windowed = pCollection
    .apply(Window.<T>into(...)
        .withAllowedLateness(...)
        .triggering(...).discardingFiredPane())
```

[コードサンプル](./codes/discarding-mode.md)