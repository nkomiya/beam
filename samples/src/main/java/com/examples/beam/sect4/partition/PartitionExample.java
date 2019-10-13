package com.examples.beam.sect4.partition;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.BigEndianIntegerCoder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.Partition;
import org.apache.beam.sdk.transforms.Partition.PartitionFn;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.TypeDescriptors;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Partitionのコードサンプル
 * <p>
 * 整数型のPCollectionを 80%:20% に分割する
 * </p>
 */
public class PartitionExample {
  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();
    List<Integer> lst = new ArrayList<Integer>(100);
    for (int i = 1; i <= 100; i++) lst.add(i);

    // inputのPCollection
    PCollection<Integer> input = pipeline
        .apply("CreateInput", Create.of(lst).withCoder(BigEndianIntegerCoder.of()));

    // 分割
    int numPartition = 2;
    PCollectionList<Integer> partitioned = input
        .apply("ApplyPartition", Partition.of(numPartition, new MyPartitionFn()));


    // Listのように、getメソッドでPCollectionを抽出する
    partitioned.get(0)
        .apply("ConvertToString-1",
            MapElements.into(TypeDescriptors.strings()).via((Integer x) -> String.format("%d", x)))
        .apply("WriteToText-1",
            TextIO.write().to("lower").withoutSharding());
    // 元の20%
    partitioned.get(1)
        .apply("ConvertToString-2",
            MapElements.into(TypeDescriptors.strings()).via((Integer x) -> String.format("%d", x)))
        .apply("WriteToText-2",
            TextIO.write().to("upper").withoutSharding());

    // 実行
    pipeline.run();
  }

  /**
   * InputのPCollectionを 80%:20% に分割するPartitionFn
   */
  private static class MyPartitionFn implements PartitionFn<Integer> {
    /**
     * Partitionの仕方を決めるメソッド
     * <p>
     * 5回に一度1を返し、残りは0を返す
     * </p>
     *
     * @param item PCollectionの要素
     * @param ndiv 分割数
     * @return 分割先を指定する整数
     */
    @Override
    public int partitionFor(Integer item, int ndiv) {
      Random r = new Random();
      return r.nextInt(5) / 4;
    }
  }
}
