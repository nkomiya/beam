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
# Apache beam programming guide for Java SDK

## 目次
1. 準備
    + [各種ツールのセットアップ](./sect0/prepare.html)
    + [Apache Mavenの使い方](./sect0/usage.html)
2. [Overview](./sect1/overview.html)
3. [Pipeline](./sect2/pipeline.html)
4. [PCollection](./sect3/pcollection.html)
5. PTransform
    + [概要](./sect4/overview.html)
    + 基本的なPTransform
      + [ParDo](./sect4/pardo.html)
      + [GroupByKey](./sect4/groupbykey.html)
      + [CoGroupByKey](./sect4/cogbk.html)
      + [Combine](./sect4/combine.html)
      + [Flatten](./sect4/flatten.html)
      + [Partition](./sect4/partition.html)
    + [注意点](./sect4/warnings.html)
    + [Side Input](./sect4/sideInput.html)
    + [ParDo発展編](./sect4/pardo2.html)
    + [Composite transform](./sect4/composite.html)
6. [Pipeline I/O](./sect5/io.md)
7. [エンコードと型安全性](./sect6/coder.md)
8. window
9. trigger