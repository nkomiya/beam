[戻る](../overview.md)

# Data-driven triggers
Data-driven triggers はパイプラインに到着したデータ数に基づく trigger です。

使い道としては、遅延データの取り扱いの指定です。

## Triggerを設定する
Data-driven triggers は`AfterPane`で設定できます。処理発火のタイミングは、`elementCountAtLeast`で指定します。下のコードでは、データが 3つ届いた段階で処理が発火されます。

```java
import org.apache.beam.sdk.transforms.windowing.AfterPane;
import org.apache.beam.sdk.transforms.windowing.Trigger.OnceTrigger;

OnceTrigger dataDrivenTrigger = AfterPane.elementCountAtLeast(3);
```

動作するコードサンプルは[こちら](./codes/data-driven.md)です。このサンプルでは Data-driven triggers を使って、処理の早期発火のタイミングを指定しています。
