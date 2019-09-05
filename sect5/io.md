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

# Pipeline I/O
基本的には外部ソースからのデータの読み込み、および外部ソースへのデータ出力、っていう形になるかと思います。
Beam SDKでは[様々なストレージ](https://beam.apache.org/documentation/io/built-in/)へのread/write変換が用意されてる。

## <span class="head">Inputデータの読み込み</span>
既出なので特に難しいことは無いかと思います...

```java
Pipeline pipeline = Pipeline.create();
PCollection<String> lines = pipeline
    .apply(TextIO.read().from("/path/to/file"));
```

`from`に渡している/path/to/fileは、ローカル上のファイルでもGCSでもAmazon S3...etc、で大体同じ。
ローカルファイルなら普通に相対パス or 絶対パス、GCSなら"gs://[バケット名]/[ファイル名]"って感じになります。

## <span class="head">ファイル出力</span>
こちらも既出なので...

```java
PCollection<String> output = ...;
output.apply(TextIO.write().to("/path/to/file"));
```

ちょくちょく書いていた、`withoutSharding()`を付けておくと出力ファイルが分割されないので便利。

## <span class="head">複数ファイルの読み込み</span>
同じくそこまで難しくないですが、複数ファイルの読み込みのためにワイルドカードが使えます。
ただ、これは読み込み元のファイルシステムに依存するので気をつけろ、とのことです。

動作は単一ファイルの読み込みと同じで、String型の`PCollection`を返します。

```java
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;

public class Main {
    public static void main(String[] args) {
        Pipeline pipeline = Pipeline.create();
        pipeline
                .apply(TextIO.read().from("./?.txt"))
                .apply(TextIO.write().to("test").withoutSharding())
        ;
        pipeline.run();
    }
}
```

もし複数のソースからデータを読み込む場合は、個別に`read`変換をしたあとで、`Flatten`をapplyしてくれとのこと。

## <span class="head">複数ファイルへの出力</span>
基本的に出力ファイルは分割される。ファイル名は

(指定したファイル名)-00000-of-00005

みたいになる。ファイル結合はしなくてもよいが、拡張子はきちんとしたい...、みたいなときは`withSuffix`で拡張子が指定できる。ファイル名は

(指定したファイル名)-00000-of-00005(指定した拡張子)

って感じです。

```java
import java.util.ArrayList;
import java.util.List;
// beam sdk
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Create;

public class Main {
    public static void main(String[] args) {
        List<String> input = new ArrayList<String>(10) {{
            for (int i = 1; i <= 10; i++) {
                add(String.valueOf(i));
            }
        }};
        //
        Pipeline pipeline = Pipeline.create();
        pipeline
                .apply(Create.of(input))
                .apply(TextIO.write().to("result").withSuffix(".txt"))
        ;
        pipeline.run();
    }
}
```
