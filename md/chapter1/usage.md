[top へ](../index.md)

# Maven の使い方

<!-- TOC -->

- [クイックスタート](#クイックスタート)
    - [Beam のクイックスタートを試す](#beam-のクイックスタートを試す)
        - [コード取得](#コード取得)
            - [pom.xml についての補足](#pomxml-についての補足)
        - [Build](#build)
        - [実行](#実行)

<!-- /TOC -->

## Beam のクイックスタートを試す

Beam のクイックスタートをローカル実行する流れを通して、Maven の使い方を説明します。

### 準備: コード取得

Maven のコマンドを使って、Beam のクイックスタートのコードを取得します。

- ダウンロード
    - 現状 2.24.0 以降では得られる Java ファイルが正しくありません...

```bash
# Beam SDK バージョン指定
$ BEAM_VERSION=2.23.0

# 雛形取得
$ mvn archetype:generate \
    -D interactiveMode=false \
    -D archetypeGroupId=org.apache.beam \
    -D archetypeArtifactId=beam-sdks-java-maven-archetypes-starter \
    -D archetypeVersion="${BEAM_VERSION}" \
    -D targetPlatform=1.8 \
    -D artifactId=check-pipeline-dependencies \
    -D groupId=org.apache.beam.samples

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

#### pom.xml についての補足

Maven では pom.xml を使って、依存ライブラリの管理などを行います。

Maven で Beam プログラミングを行うにあたり、新規で pom.xml を作成しても良いですが、Beam 公式のサンプルを取得して pom.xml のみを流用するのが簡単です。

最小構成の pom.xml を使いたければ[こちら](https://beam.apache.org/documentation/sdks/java-dependencies/)、頻繁に使いそうなライブラリ (GCP用のライブラリなど) が入ったものを使いたければ [こちら](https://beam.apache.org/get-started/quickstart-java/) を使うのが良いかと思います。

### ビルド

先程の Maven のコマンドで作成されたディレクトリのルート (pom.xml があるディレクトリ) にて、次のコマンドを実行すると Java コードがコンパイルできます。

```bash
$ mvn compile
```

上記コマンドの実行するとディレクトリ target が作成され、ここに class ファイルなどが作成されます。

```bash
$ find target -type f
target/classes/org/apache/beam/samples/StarterPipeline.class
target/classes/org/apache/beam/samples/StarterPipeline$1.class
target/classes/org/apache/beam/samples/StarterPipeline$2.class
target/maven-status/maven-compiler-plugin/compile/default-compile/inputFiles.lst
target/maven-status/maven-compiler-plugin/compile/default-compile/createdFiles.lst
```

初回はリポジトリから Beam SDK を落とすため、Build にやや時間が掛かかります。

### 実行

サンプルでは、maven-exec-plugin が pom.xml に記載されているため、Maven コマンドによりプログラムを実行できます ([参考](https://qiita.com/hide/items/0c8795054219d04e5e98))。

maven-exec-plugin では、実行するクラスはオプション exec.mainClass で指定可能です。

```bash
$ mvn -q exec:java \
    -D exec.mainClass=org.apache.beam.samples.StarterPipeline
May 14, 2020 12:12:18 AM org.apache.beam.samples.StarterPipeline$2 processElement
INFO: HELLO
May 14, 2020 12:12:18 AM org.apache.beam.samples.StarterPipeline$2 processElement
INFO: WORLD
```
