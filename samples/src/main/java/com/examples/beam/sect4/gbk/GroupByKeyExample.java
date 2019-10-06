package com.examples.beam.sect4.gbk;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.BigEndianIntegerCoder;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

import java.util.Arrays;
import java.util.List;

public class GroupByKeyExample {
  public static void main(String[] args) {
    // final修飾子をつけてImmutableにする
    final List<KV<String, Integer>> input =
        Arrays.asList(
            KV.of("cat", 1),
            KV.of("dog", 5),
            KV.of("and", 1),
            KV.of("jump", 3),
            KV.of("tree", 2),
            KV.of("cat", 5),
            KV.of("dog", 2),
            KV.of("and", 2),
            KV.of("cat", 9),
            KV.of("and", 6)
        );

    // graph構築
    Pipeline pipeline = Pipeline.create();
    PCollection<KV<String, Integer>> col = pipeline.apply("CreateInput",
        Create.of(input).withCoder(KvCoder.of(StringUtf8Coder.of(), BigEndianIntegerCoder.of())));

    col
        .apply("ApplyGroupByKey", GroupByKey.create())
        .apply(ParDo.of(
            new DoFn<KV<String, Iterable<Integer>>, String>() {
              @ProcessElement
              public void mymethod(@Element KV<String, Iterable<Integer>> e, OutputReceiver<String> o) {
                StringBuilder builder = new StringBuilder();
                builder.append(e.getKey()).append(": [");
                builder.append(Joiner.on(", ").join(
                    Iterables.<Integer, String>transform(
                        e.getValue(),
                        x -> String.format("%d", x))));
                builder.append("]");
                o.output(builder.toString());
              }
            }))
        .apply("WriteToFile", TextIO.write().to("result.txt").withoutSharding());

    // 実行
    pipeline.run();
  }
}
