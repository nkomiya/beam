<style type="text/css">
  .head { 
    border-left:5px solid #00f;
    padding:3px 0 3px 10px;
    font-weight: bold;
  }
  .lhead { 
    border-left:5px solid #00f;
    padding:3px 0 3px 10px;
    font-size:14pt;
    font-weight: bold;
  }
</style>
[topへ](../index.html)

# Mavenの使い方
## Maven projectの作成
Mavenはリポジトリ管理機能つきのbuildツールです。Mavenリポジトリ上のパッケージを自分のプロジェクトで使ったり、自作のパッケージを公開したりできます（たぶん）。

ですが、設定ファイルがxmlなのが嫌なところですかね...。設定ファイルは幸い一つなので、テンプレ的なやつを作ればとりあえずはokなはずです。
Beamを使う上で簡単なのは、Dataflowの公式ドキュメントに載っているサンプルをdownloadする方法です。無駄はあるかと思いますが...。

```bash=
$ BEAM_VERSION=2.13.0
$ GROUP_ID=[Javaのパッケージ名]
$ ARTIFACT_ID=[出力フォルダ名]
$ VERSION=[プロジェクトのバージョン]
$ mvn archetype:generate \
      -DarchetypeGroupId=org.apache.beam \
      -DarchetypeArtifactId=beam-sdks-java-maven-archetypes-examples \
      -DarchetypeVersion=$BEAM_VERSION \
      -DgroupId=$GROUP_ID \
      -DartifactId=$ARTIFACT_ID \
      -Dversion=$VERSION \
      -Dpackage=org.apache.beam.examples \
      -DinteractiveMode=false
```

コマンドを実行すると、カレントディレクトリにDversionで指定したディレクトリができるはずです。
欲しいのはpom.xmlのみなので、その他もろもろは全て消します。

```bash=
$ rm -r ${ARTIFACT_ID}/src/main/java/*; \
  rm -r ${ARTIFACT_ID}/src/test/java/*; \
  cd ${ARTIFACT_ID}/src; \
  mkdir -p main/java/$(echo $GROUP_ID | sed "s/\./\//g") && \
  echo "package ${GROUP_ID};" > main/java/$(echo $GROUP_ID | sed "s/\./\//g")/Main.java; \
  mkdir -p test/java/$(echo $GROUP_ID | sed "s/\./\//g"); \
  cd ../..; \
  unset BEAM_VERSION GROUP_ID ARTIFACT_ID VERSION
```

### build
buildするだけならシンプル。Mavenプロジェクトのルートに移動し、次のコマンドを実行します。

```bash=
$ mvn compile
```

初回はリポジトリからbeam SDKを落とすので、buildに時間が掛かるかもです。

### ローカルでの実行
実行したいクラスをDexec.mainClassで、コマンドライン引数をDexec.argsに渡す。

```bash=
$ mvn exec:java -Dexec.mainClass=path.to.class \
  -Dexec.args="--arg1=hoge --arg2=fuga"
```

みたいな。実行時にbeamのSDKの指定もいるので、`java -cp=...`みたいにやるのは面倒くさい。

### Dataflowでの実行
Mavenとbeamの両方でRunnerの指定が必要。

```bash=
$ mvn -Pdataflow-runner exec:java -Dexec.mainClass=path.to.class \
  -Dexec.args="--runner=DataflowRunner \
    --tempLocation=gs://bucket名 \
    --arg1=hoge --arg2=fuga ..."
```

tempLocationは一時ファイル置き場で、cloud storageのバケットを指定する。たぶん必要？