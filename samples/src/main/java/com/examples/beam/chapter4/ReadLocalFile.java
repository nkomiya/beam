package com.examples.beam.chapter4;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.io.TextIO;

public class ReadLocalFile {
  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();
    //
    // file読み込み
    PCollection<String> col = pipeline
        .apply("ReadFromFile", TextIO.read().from("input.txt"));
  }
}
