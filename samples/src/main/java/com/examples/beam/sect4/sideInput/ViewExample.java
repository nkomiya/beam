package com.examples.beam.sect4.sideInput;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.View;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;

public class ViewExample {
  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();

    PCollection<String> pCollection = pipeline
        .apply("CreateInput",Create.of("test").withCoder(StringUtf8Coder.of()));

    PCollectionView<String> pCollectionView = pCollection
        .apply("CreateView",View.asSingleton());

    pipeline.run();
  }
}
