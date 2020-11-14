[topへ](../index.md)

# Pipeline

<!-- TOC -->

- [Pipeline](#pipeline)
    - [インスタンス作成](#インスタンス作成)
        - [最小構成](#最小構成)
            - [サンプル](#サンプル)
                - [補足: コード実行とオプション値の参照](#補足-コード実行とオプション値の参照)
        - [ユーザ定義 オプション](#ユーザ定義-オプション)
            - [オプションクラスの定義](#オプションクラスの定義)
                - [オプションの必須化](#オプションの必須化)
                - [デフォルト値](#デフォルト値)
                - [ヘルプテキスト](#ヘルプテキスト)
            - [オプションクラスのインスタンス作成](#オプションクラスのインスタンス作成)
            - [サンプル](#サンプル-1)

<!-- /TOC -->

## インスタンス作成

まずは `Pipeline` インスタンスを作る方法について。

### 最小構成

基本的に、以下の流れで `Pipeline` インスタンスを作ります。

1. パイプラインにオプションを渡すため、`PipelineOptions` インスタンスを作成
1. `PipelineOptions` を紐付けた、`Pipeline` インスタンスを作成

`PipelineOptions` を使う理由は、主に下記二点です。

- パース済みのオプション値を Beam コード内で参照する
- Beam SDK にオプション値を渡す

Beam では runner (コードの実行環境) に関する構成もオプション経由で指定します。`PipelineOptions` を経由して Beam SDK に runner の情報等を渡すと、SDK が裏側で制御してくれます。

たとえば Cloud Dataflow で実行する際、オプション経由で GCP プロジェクト渡すのみで、パイプラインを走らせる GCP プロジェクトが指定できます。


#### サンプル

以下コードは定型文として覚えて良いと思います。

```java
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;

public class MinimumPipeline {
  public static void main(String[] args) {
    // 実行時オプションをコマンドライン引数から取得
    PipelineOptions opt = PipelineOptionsFactory
        .fromArgs(args)
        .withValidation()
        .create();

    // オプション付き Pipeline インスタンスの作成
    Pipeline pipeline = Pipeline.create(opt);
  }
}
```

`PipelineOptions` インスタンスは、 `PipelineOptionsFactory` により作成します。`fromArgs` メソッドにコマンドライン引数 (args) を渡し、`create` メソッドでインスタンス化します。  
インスタンス化の前に `withValidation` を呼び出すと、必須オプションが指定されているか、値が期待するフォーマットになっているか、などの検証してくれます。

`Pipeline` インスタンスは、`Pipeline.create` により作成します。`create` メソッドの引数に `PipelineOptions` を渡すと、Beam SDK がオプション値を参照できるようになります。

##### 補足: コード実行とオプション値の参照

- コード実行

`PipelineOptionsFactory.fromArgs` は、以下形式の要素を持つ String 型配列を想定しています。

```java
"--${オプション名}=${値}"
```

そのため、コード実行は、以下形式のコマンドで行います。

```bash
$ mvn compile exec:java \
    -D exec.mainClass=path.to.class \
    -D exec.args="\
--option1=hoge \
--option2=fuga"
```

- オプション値の参照

`PipelineOptions` 型インスタンスの `get<オプション名>` メソッドから、コード内でオプション値を参照できます (オプション名は先頭が大文字です)。  
以下は、String 型のオプション option1 を取得する例です。

```java
PipelineOptions opt = ...;
String val = opt.getOption1();
```

### ユーザ定義 オプション

ユーザ定義のオプションを定義すると、コマンドライン引数経由でパイプライン実行時にパラメータを渡すことができます。

#### オプションクラスの定義

ユーザ定義のオプションの定義には、`PipelineOptions` を継承したインターフェース作ります。定義したいオプションごとに、setter と getter を定義する必要があります。

基本的には、以下のような形式となります。

```java
public interface CustomOptions extends PipelineOptions {
  /** getter */
  String getMyOption();

  /** setter */
  void setMyOption(String v);
}
```

各オプションに対する setter / getter のメソッド名は、オプション名で決まります。それぞれ、`set<オプション名>`, `get<オプション名>` の形にします。  
オプション値の型は setter の仮引数、および getter の戻り値の型から決まり、両者の型は一致する必要があります。  
上のサンプルでは、String 型のオプション myOption が定義されます。

getter にアノテーション付けることで、下記項目の設定が可能です。

- オプションの必須化
- デフォルト値
- ヘルプテキスト

##### オプションの必須化

`@Validation.Required` を付けたオプションが実行時に指定されない場合、エラーとなります。

```java
@Validation.Required
String getMyOption();
```

##### デフォルト値

`@Default.<型>(<デフォルト値>)` で、オプションのデフォルト値を設定できます。

```java
@Default.String("default_value")
String getMyOption();
```

##### ヘルプテキスト

`@Description("<ヘルプテキスト>")` で、オプションのヘルプテキストを設定できます。

```java
@Description("This is a help text for option myOption")
String getMyOption();
```

コマンドラインでヘルプテキストを表示できるようにするには、`PipelineOptionsFactory` へオプションを登録しておく必要があります。

```java
PipelineOptionsFactory.register(CustomOptions.class);
```

上記設定により、以下コマンドでヘルプを表示することができます。

```bash
$ mvn -q compile exec:java \
    -D exec.mainClass=path.to.main.class \
    -D exec.args='--help=path.to.option.class'

# 出力例
com.examples.beam.chapter3.CustomOptionsPipeline$CustomOptions:

  --option1=<String>
    Description for option 1
  --option2=<String>
    Default: default_value
    Description for option 2
```

#### オプションクラスのインスタンス作成

ユーザ定義 オプションを使う場合も、`PipelineOptionsFactory` を使います。

ただインスタンス作成の際、`create` ではなく `as` を使います。

```java
CustomOptions opt = PipelineOptionsFactory
    .fromArgs(args)
    .withValidation()
    .as(CustomOptions.class);
```

#### サンプル

```java
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation;

public class CustomOptionsPipeline {

  /** ユーザ定義 オプション */
  public interface CustomOptions extends PipelineOptions {
    /** option 1 */
    @Description("Description for option 1")
    @Validation.Required
    String getOption1();

    void setOption1(String s);

    /** option2 */
    @Default.String("default_value")
    @Description("Description for option 2")
    String getOption2();

    void setOption2(String s);
  }

  /** Pipeline 作成 */
  public static void main(String[] args) {
    PipelineOptionsFactory.register(CustomOptions.class);
    CustomOptions opt = PipelineOptionsFactory
        .fromArgs(args)
        .withValidation()
        .as(CustomOptions.class);

    Pipeline pipeline = Pipeline.create(opt);
    System.out.printf("option1=%s\n", opt.getOption1());
    System.out.printf("option2=%s\n", opt.getOption2());
  }
}
```
