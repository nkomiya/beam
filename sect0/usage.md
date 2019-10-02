[topへ](../index.md)

# Mavenの使い方
## Beam用にMaven projectを作成する
Mavenはリポジトリ管理機能つきのbuildツールです。Mavenリポジトリ上のパッケージを自分のプロジェクトで使ったり、自作のパッケージを公開したりできます（たぶん）。
設定ファイルがxmlなのは面倒ですが設定ファイルは幸い一つなので、テンプレ的なやつを作ればとりあえずはokです。

Beamを使う上で簡単なのは、[Dataflowのクイックスタート](https://cloud.google.com/dataflow/docs/quickstarts/quickstart-java-maven#wordcount-)に載っているサンプルコードを落とす方法です。これをMaven Projectを作ると、使わない依存関係も入ると思いますが...

```bash
$ BEAM_VERSION=2.15.0
$ GROUP_ID=[MavenプロジェクトのグループID]
$ ARTIFACT_ID=[ビルドで作成される成果物の名前]
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

Group ID, Artifact IDはMavenで使う概念っぽいです。Groupは組織とか (パッケージがGroup配下にまとめられる) 、Artifactがビルドされたパッケージ的な感じです。適当な値を入れて問題ないです。

外部パッケージはたいてい[Mave Central Repository](https://mvnrepository.com)から持ってくることになると思いますが、https://mvnrepository.com/artifact/**GROUP\_ID**/**ARTIFACT\_ID** みたいな感じでパッケージがまとめられます。Beamだとこんな感じです。

```
org.apach.beam
  ├── beam-sdks-java-core
  ├── beam-runners-direct-java
  └── ...
```

コマンドを実行すると、カレントディレクトリに`DartifactId`で指定したディレクトリができるはずです。欲しいのはpom.xmlのみなので、その他もろもろは全て消してOKです。

```bash
$ rm -r ${ARTIFACT_ID}/src/main/java/*; \
  rm -r ${ARTIFACT_ID}/src/test/java/*; \
  cd ${ARTIFACT_ID}/src; \
  mkdir -p main/java/$(echo $GROUP_ID | sed "s/\./\//g") && \
  echo "package ${GROUP_ID};" > main/java/$(echo $GROUP_ID | sed "s/\./\//g")/Main.java; \
  mkdir -p test/java/$(echo $GROUP_ID | sed "s/\./\//g"); \
  cd ../..; \
  unset BEAM_VERSION GROUP_ID ARTIFACT_ID VERSION
```

### Build
Buildするだけならシンプル。Mavenプロジェクトのルートに移動し、次のコマンドを実行します。

```bash
$ mvn compile
```

初回はリポジトリからBeam SDKを落とすので、Buildに時間が掛かるかもです。

### ローカルでの実行
実行したいクラスをDexec.mainClassで、コマンドライン引数をDexec.argsに渡します。

```bash
$ mvn exec:java -Dexec.mainClass=path.to.class \
  -Dexec.args="--arg1=hoge --arg2=fuga"
```

みたいな。依存パッケージのパス指定はMavenが上手いことしてくれます (プラグインの追加が必要ですが、落としたpomファイルには記載ありです)。

### Dataflowでの実行
Mavenとbeamの両方でRunnerの指定が必要。

```bash
$ mvn -Pdataflow-runner exec:java -Dexec.mainClass=path.to.class \
  -Dexec.args="--runner=DataflowRunner \
    --tempLocation=gs://bucket名 \
    --arg1=hoge --arg2=fuga ..."
```

tempLocationは一時ファイル置き場で、Cloud Storageのバケットを指定します。

> #### メモ
> Dataflowでの処理は、Compute Engineのデフォルトサービスアカウントが使われます。プロジェクト編集者の権限がついてるので、一つのプロジェクトで閉じているなら権限設定はたいていの場合必要ないです。  
> クロスプロジェクトで走らせたりする場合には、Compute Engineのデフォルトサービスアカウントに適切な権限を付与する必要があります（実行時のサービスアカウントは変更可です）。