[topへ](../index.md)  
[PTransformの概要へ](./ptransform.md)

# 基本的なPTransform
公式guideではCore Beam transformと呼ばれてる、Built-inの簡単な`PTransform`たちです。

全て覚えるというよりは、コーディングの流れの理解ができれば十分なはずです。ただ、分散処理のややこしさが出てくる、`Combine`は目を通しても損はないと思います。

+ [ParDo](./core/pardo.md)  
`PCollection`の要素ごとの変換処理
+ [GroupByKey](./core/groupbykey.md)  
`PCollection`の要素をグループ化
+ [CoGroupByKey](./core/cogroupbykey.md)  
複数の`PCollection`に関するグループ化
+ [Combine](./core/combine.md)  
`PCollection`に対して集計処理を行う
+ [Flatten](./core/flatten.md)  
複数の`PCollection`の単純な結合
+ [Partition](./core/partition.md)  
`PCollection`を分割する