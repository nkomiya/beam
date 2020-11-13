[top へ](../index.md)

<!-- TOC -->

- [準備編](#準備編)
    - [JDK](#jdk)
    - [Maven](#maven)
    - [jEnv](#jenv)

<!-- /TOC -->

# 準備編

## JDK

Beam の安定動作には Java 8 が必要で、Java 11 対応は進行中[^jdk11]。無償のものを使いたければ、[こちら](https://adoptopenjdk.net/)などから Open JDK を落とす。

[^jdk11]: [https://beam.apache.org/roadmap/java-sdk/](https://beam.apache.org/roadmap/java-sdk/)

## Maven

Beam の公式ガイドや、Google が提供している多くの Beam のプロジェクトで、[Maven](https://maven.apache.org/) が使用されているため、ビルドツールは
 Maven がおすすめです。

## jEnv

[jEnv](https://www.jenv.be/) は、Java 用のバージョン管理ツール。Git 経由でインストールする方法をまとめます。

- ダウンロード

```bash
$ git clone https://github.com/gcuisinier/jenv.git ~/.jenv
```

- .bash_profile に jEnv の設定追加

```bash
$ cat << '_EOF_' >> ${HOME}/.bash_profile
export JENV_ROOT="${HOME}/.jenv"
export PATH="${JENV_ROOT}/bin:$PATH"
eval "$(jenv init -)"
. "${HOME}/jenv/completions/jenv.bash"
_EOF_
```

- プラグイン設定

```bash
# 環境変数 (JAVA_HOME) の自動設定
$ jenv enable-plugin export

# Maven 用の設定
$ jenv enable-plugin maven
```

- Java バージョンの一致確認
    - 両コマンドで Java の version が一致してれば OK です

```bash
# Java バージョン (抜粋)
$ java -version
openjdk version "1.8.0_222"

# Maven バージョン (抜粋)
$ mvn -version
Apache Maven 3.6.3 (cecedd343002696d0abb50b32b541b8a6ba2883f)
Java version: 1.8.0_222
```
