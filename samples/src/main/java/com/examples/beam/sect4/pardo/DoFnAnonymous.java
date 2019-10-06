package com.examples.beam.sect4.pardo;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;

import java.util.Arrays;
import java.util.List;

public class DoFnAnonymous {
  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();

    // input
    List<String> input = Arrays.asList("hoge", "fuga");

    // graph
    pipeline
        .apply(Create.of(input))
        .apply(ParDo.of(
            new DoFn<String, Integer>() {
              @ProcessElement
              public void method(@Element String e, OutputReceiver<Integer> o) {
                o.output(e.length());
              }
            }));

    // execute
    pipeline.run();
  }
}