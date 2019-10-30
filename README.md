# Apache Beam Programming Guide

## Overview
[Apache Beam Programming Guide](https://beam.apache.org/documentation/programming-guide/)の日本語訳とサンプルコード集。

### ディレクトリ構成
```
/gitroot
   ├── README.md
   ├── build.sh
   ├── doc/
   ├── md/
   └── samples/
```

+ build.sh  
Markdownをhtmlに変換するスクリプト
+ doc/  
htmlファイル
+ md/  
Markdownファイル
+ samples/  
Beamのコードサンプル


## Prerequistes
いずれも必須ではないですが、対応があると望ましいです。

### ドキュメント関連
+ Markdown Viewer
+ [Pandoc](https://pandoc.org/installing.html)
+ [GNU core utilities](https://www.gnu.org/software/coreutils)

下二つは、Markdownファイルをhtmlファイルに変換するスクリプト (build.sh) を実行するには必要になります。

### 実行環境
サンプルコードを実行するために必要な環境です。

+ JDK 8 
+ [Apache Maven](https://maven.apache.org/download.cgi)

## Convert Markdown files
```bash
$ bash build.sh
```

## References
+ CSSファイル  
[https://gist.github.com/andyferra/2554919](https://gist.github.com/andyferra/2554919)
+ シンタックスハイライト  
[https://prismjs.com/](https://prismjs.com/)


