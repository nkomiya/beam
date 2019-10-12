[戻る](../core.md)

# Flatten
`Flatten`は同じ型の`PCollection`をひとまとめにするときに使います。動作としては、単純にJoinするだけです。  
複数のデータソースがあると、`PCollection`も複数できます。ひとまとめにしたいのですが...、というときに使います。

複数の`PCollection`を取るのは`CoGroupByKey`と似てますが、`Flatten`はkeyでひとまとめにしないですし、`KV`型でなくても大丈夫です。

動作イメージは以下の通りです。

**Input 1**

```
cat
dog
```

**Input 2**  

```
lion
dolphin
```

**Output**

```
cat
dog
lion
dolphin
```

手順としては、

1. `PCollection`のlistを作成
2. `Flatten`をapply

です。一つ目の手順は初登場ですが、`PCollectionList.of`でリスト作成、`and`で`PCollection`を追加していきます。

```java
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;

// 複数のPCollection
PCollection<T> input1 = ...;
PCollection<T> input2 = ...;
// PCollectionのlistにする
PCollectionList<T> pCollectionList = PCollectionList.of(input1).and(input2);
```

あとは、`Flatten.pCollections`をapplyするだけです。

```java
PCollection<T> flattened = pCollectionList.apply(Flatten.pCollections());
```

[こちら](./codes/flatten.md)、動作するコードサンプルです。