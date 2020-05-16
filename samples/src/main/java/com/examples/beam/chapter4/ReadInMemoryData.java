package com.examples.beam.chapter4;

import java.util.*;
// pipeline
import org.apache.beam.sdk.Pipeline;
// for passing the in-memory data to pipeline
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.coders.BigEndianIntegerCoder;

public class ReadInMemoryData {
  public void main(String[] args) {
    // create pipeline
    Pipeline pipeline = Pipeline.create();

    // .............................. 単一のデータを読み込む
    pipeline.apply("ReadSingleValue",
        Create.of(1).withCoder(BigEndianIntegerCoder.of()));

    // .............................. 複数のデータを読み込む
    // 読み込むデータ。複数の場合、リストにする必要がある
    final List<String> input = Arrays.asList(
        "To be, or not to be: that is the question: ",
        "Whether 'tis nobler in the mind to suffer ",
        "The slings and arrows of outrageous fortune, ",
        "Or to take arms against a sea of troubles, ");

    pipeline.apply("ReadMultipleValues",
        Create.of(input).withCoder(StringUtf8Coder.of()));
  }
}
