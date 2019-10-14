[topへ](../index.md)

# Pipeline I/O
基本的には外部ソースからのデータの読み込み、および外部ソースへのデータ出力、っていう形になるかと思います。
Beam SDKでは[様々なストレージ](https://beam.apache.org/documentation/io/built-in/)へのread/write変換が用意されています。

ここでは、`TextIO`について詳細に説明します。

## <span class="head">Inputデータの読み込み</span>
既出なので特に難しいことは無いかと思います...

```java
Pipeline pipeline = Pipeline.create();
PCollection<String> lines = pipeline
    .apply(TextIO.read().from("/path/to/file"));
```

`from`に渡している/path/to/fileは、ローカル上のファイルでもGCSでもAmazon S3...etc、で大体同じです。(GCS, S3では、Credentialを渡す必要がありますが)

ローカルファイルの読み込みでは相対パス or 絶対パスでの指定、GCSからの読み込みならGCSパスを渡せば良いです。

## <span class="head">ファイル出力</span>
こちらも既出なので...

```java
PCollection<String> output = ...;
output.apply(TextIO.write().to("/path/to/file").withoutSharding());
```

`withoutSharding()`を付けておくと出力ファイルが分割されないので便利です。

## <span class="head">複数ファイルの読み込み</span>
同じくそこまで難しくないですが、複数ファイルの読み込みのためにワイルドカードが使えます。
ただ、これは読み込み元のファイルシステムに依存します。

動作は条件にマッチするファイルを個別に読み込み、`Flatten`をapplyした場合と同じです。単一のString型の`PCollection`を返します。

```java
pipeline.apply(TextIO.read().from("*.txt"))
```

ワイルドカードでマッチさせられない場合や複数のソースから読み込む場合は、個別読み込みの後に`Flatten`をapplyしてください。

[コードサンプル](./codes/wildcard.md)

## <span class="head">複数ファイルへの出力</span>
基本的に出力ファイルは分割されます。ファイル名は

(指定したファイル名)-00000-of-00005

のように、拡張子がつきません。拡張子を指定したい場合は、`withSuffix`を使います。

```java
PCollection<String> pCollection = ...;
pCollection.apply(TextIO.write().to("result").withSuffix(".txt");
```

この例だと、ファイル名は`result-00000-of-00005.txt`のようになります。

[コードサンプル](./codes/suffix.md)
