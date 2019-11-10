[戻る](../built-in.md)

# Session windows
## Overview
Session windows は名前の通り"セッション"ベースの window 変換で、"セッション"が生きている限り`PCollecion`の要素は同じ window に割り当てられます。

<img src="./figs/session-windows.png" width=600>

各"セッション"は、一定時間新しいデータが到着しない場合に終了します。言い換えると、到着時間が近いデータがひとまとめにされます。

Session windows は**時間的にばらつきのあるデータ**に対して有効です。  
例としては、Web ページのクリック情報などです。Web サイトを離れればクリックされなくなりますし、Web ページを読んでいる間はクリック回数が少なくなるはずです。  
こういった場合に Session windows を使うことで、「ユーザが Web ページに留まっている間」などでデータをまとめることができます。

## Windowの設定
Session windows では、window 間の最小間隔を指定します。ここで指定した期間の間データが届かないと、次に到着するデータは別の window に入ります。

```java
import org.apache.beam.sdk.transforms.windowing.Sessions;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;

PCollection<T> pCollection = ...;
PCollection<T> windowed = pCollection
    .apply(Window.<T>into(Sessions.withGapDuration(Duration.standardMinutes(10L))));
```

コードサンプルは[こちら](./codes/session.md)です。`PCollection`の要素に擬似的なタイムスタンプを付与し、window の設定をしています。