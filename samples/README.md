# Beam コードサンプル

## ビルドと実行

基本的には、実行時引数は取りません。

```bash
$ mvn compile exec:java \
    -D exec.mainClass=package.name.and.className
```

実行時を取る場合は、こちらです。

```bash
$ mvn compile exec:java \
    -D exec.mainClass=package.name.and.className \
    -D exec.args="\
--myOption1=hoge \
--myOption1=fuga \
..."
```

## クラス名一覧の取得

実行できるファイルが多いので...

```bash
$ cd $(git rev-parse --show-toplevel)/samples
$ find src -type f -name "*.java" \
    | sed -e "s/src\/main\/java\///" -e "s/\.java$//" \
    | tr "/" "." \
    | sort -t "." -k 4
```
