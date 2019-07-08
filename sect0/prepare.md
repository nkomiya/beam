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

# 準備編
目次
+ [Open JDKのinstall](#jdk)
+ [Apache Mavenのinstall](#maven)
+ [Jenvのinstall](#jenv)
+ [IntelliJのinstall](#intellij)

## <span class="lhead" id="jdk">Open JDKのinstall</span>
Java8を使いたければ、Oracleの無償のJDKはもう利用不可。代替策として[AdoptOpenJDK](https://adoptopenjdk.net)を利用する。欲しいversionを選んでダウンロードしておしまい。

## <span class="lhead" id="maven">Mavenのinstall</span>
brewでinstall、もしくは[公式サイト](https://maven.apache.org/download.cgi)からバイナリファイルをダウンロード。

## <span class="lhead" id="jenv">Jenvのinstall</span>
pyenvのJava版。Git、もしくはhomebrewでinstallする。

```bash=
$ git clone https://github.com/gcuisinier/jenv.git ~/.jenv
```

.bash_profileにjenvの設定を追加。

```bash=
$ echo 'export PATH="$HOME/.jenv/bin:$PATH"' >> ~/.bash_profile
$ echo 'eval "$(jenv init -)"' >> ~/.bash_profile
```

Jenvは割と優秀で、環境変数の自動設定、mavenの有効化ができる。

```bash=
$ jenv enable-plugin export
$ jenv enable-plugin maven
```

インストールが終わったら、

```bash=
$ java -version
$ mvn -version
```

を行い、javaのversionが両者で一致してることとかをcheckしておしまい。

## <span class="lhead" id="intellij">IntelliJのinstall</span>
JDKのバージョン指定でかなりはまったのでメモ。とりあえずビルドが通るように、最低限の設定のみ触れておきます。 

### DefaultのJDKのバージョンの指定
特にMaven projectを作る場合、JDKのバージョンは1.5にされる模様です。なので、IntelliJにdefaultのJDKバージョンを教えてあげる必要があります。

Settings > Build,Execution,Development > Compiler > Java Compiler

を選択。Project bytecode versionを"8"にしてください。
All modules will be compiled with project bytecode version, となってますが、実はこれだけでは不十分です...

### defaultのpom.xmlの編集
Mavenプロジェクトの設定ファイルであるpom.xmlでもJDKのバージョン指定が必要です。毎回設定を変更するのは面倒なので、defaultのpom.xmlを編集しておきます。 
再び、settingsから

settings > Editor > File and code template

を選択。Otherタブを選ぶとMavenが見つかるはずです。その中にMaven project.xmlがあると思うので、ファイル末尾にちょい足しします。

```xml=
... 中略 ...

    <groupId>${GROUP_ID}</groupId>
    <artifactId>${ARTIFACT_ID}</artifactId>
    <version>${VERSION}</version>
    
    <!-- ここから -->
    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>
    <!-- ここまでを追加 -->

    ${END}
</project>
```

これで、新規プロジェクトを開く時に自動でJDKをうまいことしてくれると思います。