<style type="text/css">
  .head { 
    border-left:5px solid #00f;
    padding:3px 0 3px 10px;
    font-weight: bold;
  }
  .lhead { 
    border-left:5px solid #00f;
    padding:3px 0 3px 10px;
    font-size:14pt;
    font-weight: bold;
  }
</style>
[topへ](../index.html)

# Partition
`Partition`は一つの`PCollection`を複数の`PCollection`に分割するためのメソッド。使用例としてはMLでdatasetの80%を訓練データに、20%を評価データに分割する、みたいな。

`Partition`を使うには、datasetの分割の仕方を決める関数と分割数を定義する必要がある。分割数はcommind lineのオプションとして渡すことはできるものの、pipeline graphの構築時には決定されている必要がある。つまり、pipeline実行時に動的に分割数を決めることはできない。
また、`Partition`に渡す関数は、0以上、分割数未満の整数値を返す関数である。この戻り値が同じ組にdatasetが分割される。

```java=
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
// beam sdk
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.Partition;
import org.apache.beam.sdk.transforms.Partition.PartitionFn;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.TypeDescriptors;

public class Main {
    public static void main(String[] args) {
        Pipeline pipeline = Pipeline.create();
        List<Integer> nums = new ArrayList<Integer>(100) {{
            for (int i = 1; i <= 100; i++) {
                add(i);
            }
        }};
        // Partitionをapplyすると、
        // PCollectionのリストが返る
        PCollectionList<Integer> col = pipeline
                .apply(Create.of(nums))
                .apply(Partition.of(2, new MySplitFn()))
        ;
        // 元の80%、普通のlistみたいに
        // getメソッドでPCollectionを抽出可
        col.get(0)
                .apply("ToString1",MapElements.into(TypeDescriptors.strings()).via((Integer x) -> x.toString()))
                .apply("output1", TextIO.write().to("lower").withoutSharding())
        ;
        // 元の20%
        col.get(1)
                .apply("ToString2",MapElements.into(TypeDescriptors.strings()).via((Integer x) -> x.toString()))
                .apply("Output2", TextIO.write().to("upper").withoutSharding())
        ;
        pipeline.run();
    }

    // 型引数で指定するのは、inputのPCollectionの型
    static class MySplitFn implements PartitionFn<Integer> {
        // 第一引数：PCollectionの要素、
        // 第二引数：Partitionでの分割数
        @Override
        public int partitionFor(Integer item, int ndiv) {
            Random r = new Random();
            return r.nextInt(9) / 8;
        }
    }
}
```