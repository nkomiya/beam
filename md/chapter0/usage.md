[top へ](../index.md)

# クイックスタート

<!-- TOC -->

- [クイックスタート](#クイックスタート)
  - [Maven プロジェクトの雛形取得](#maven-プロジェクトの雛形取得)
  - [Build](#build)
  - [サンプルコード実行](#サンプルコード実行)

<!-- /TOC -->

## Maven プロジェクトの雛形取得

依存ライブラリの取得にあたり、pom.xml の編集が必要。

[こちら](https://beam.apache.org/documentation/sdks/java-dependencies/)、もしくは[こちらの](https://beam.apache.org/get-started/quickstart-java/) Beam のサンプルプロジェクトをダウンロードし、pom.xml の雛形を取得しておくと良いかと思われます。

- ダウンロード

```bash
# Beam SDK バージョン指定
$ BEAM_VERSION=2.20.0

# 雛形取得
$ mvn archetype:generate \
   -DinteractiveMode=false \
   -DarchetypeGroupId=org.apache.beam \
   -DarchetypeArtifactId=beam-sdks-java-maven-archetypes-starter \
   -DarchetypeVersion="${BEAM_VERSION}" \
   -DtargetPlatform=1.8 \
   -DartifactId=check-pipeline-dependencies \
   -DgroupId=org.apache.beam.samples

# 変数開放
$ unset BEAM_VERSION
```

- 確認

```bash
$ cd check-pipeline-dependencies

$ find . -type f
./pom.xml
./src/main/java/org/apache/beam/samples/StarterPipeline.java
```

## Build

Maven プロジェクトのルートに移動し、次のコマンドを実行します。

```bash
# ビルド
$ mvn compile

# 確認
$ find target -type f
target/classes/org/apache/beam/samples/StarterPipeline.class
target/classes/org/apache/beam/samples/StarterPipeline$1.class
target/classes/org/apache/beam/samples/StarterPipeline$2.class
target/maven-status/maven-compiler-plugin/compile/default-compile/inputFiles.lst
target/maven-status/maven-compiler-plugin/compile/default-compile/createdFiles.lst
```

初回はリポジトリから Beam SDK を落とすため、Build にやや時間が掛かかります。

## サンプルコード実行

サンプルコードの実行は`-D exec.mainClass`で、クラスパス `org.apache.beam.samples.StarterPipeline` を指定します。

```bash
$ mvn -q exec:java -D exec.mainClass=org.apache.beam.samples.StarterPipeline
May 14, 2020 12:12:18 AM org.apache.beam.samples.StarterPipeline$2 processElement
INFO: HELLO
May 14, 2020 12:12:18 AM org.apache.beam.samples.StarterPipeline$2 processElement
INFO: WORLD
```
