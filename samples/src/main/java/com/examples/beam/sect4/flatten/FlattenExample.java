package com.examples.beam.sect4.flatten;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;

import java.util.Arrays;
import java.util.List;

public class FlattenExample {
  public static void main(String[] args) {
    // Inputの作成
    List<String> inputList1 = Arrays.asList("dog", "cat");
    List<String> inputList2 = Arrays.asList("lion", "dolphin");

    // InputのPCollection
    Pipeline pipeline = Pipeline.create();
    PCollection<String> input1 = pipeline
        .apply("CreateInput1", Create.of(inputList1));
    PCollection<String> input2 = pipeline
        .apply("CreateInput2", Create.of(inputList2));

    // PCollectionのリストを作成
    PCollectionList<String> pCollectionList = PCollectionList.of(input1).and(input2);

    // 結合
    PCollection<String> flattened = pCollectionList
        .apply("ApplyFlatten", Flatten.pCollections());

    // 出力
    flattened
        .apply("WriteToText",
            TextIO.write().to("result").withoutSharding());

    // 実行
    pipeline.run();
  }
}
