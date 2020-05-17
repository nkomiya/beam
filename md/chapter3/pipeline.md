[topへ](../index.md)

# Pipeline

<!-- TOC -->

- [Pipeline](#pipeline)
    - [インスタンス作成](#インスタンス作成)
        - [最小構成](#最小構成)
            - [サンプル](#サンプル)
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

1. 実行時オプション定義のため、`PipelineOptions` インスタンスの作成
2. `Pipeline` インスタンスの作成

Beam では runner (コードの実行環境) といった、パイプラインの構成に関するパラメータもオプション経由で指定します。

`PipelineOptions` 無しでも `Pipeline` インスタンスを作れますが、デフォルトの構成でしか実行できなくなります。  
※ Direct runner (ローカルPC) でしか実行できなくなったりします

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

まず、コマンドライン引数 (args) 経由で `Pipeline` の実行時オプションを指定するため、`PipelineOptionsFactory` を使います。  
`withValidation` を付けると、必須のオプションの指定有無や、値の検証が行えます。

`Pipeline` インスタンスの作成は、`Pipeline.create` へ `PipelineOptions` インスタンスを渡すのみです。

- 補足: 実行方法

`fromArgs` は以下フォーマットの String 配列を引数に取ります。

```java
{"--${オプション名}=${値}", ...}
```

そのため、コード実行は、以下形式のコマンドで行います。

```bash
$ mvn compile exec:java \
    -D exec.mainClass=path.to.class \
    -D exec.args="\
--option1=hoge \
--option2=fuga"
```

### ユーザ定義 オプション

ユーザ定義のオプションを、コマンドライン引数経由で渡すこともできます。

#### オプションクラスの定義

`PipelineOptions` を継承したインターフェースを定義します。オプションごとに、setter と getter を定義する必要があります。

下記サンプルでは、型 `T` のオプション `myOption` が定義されます。

```java
public interface CustomOptions extends PipelineOptions {
  T getMyOption();
  void setMyOption(T t);
}
```

オプション名は、setter, getter のメソッド名から決まります。それぞれ、`set{{オプション名}}`, `get{{オプション名}}` の形にします。  
型は setter の仮引数、および getter の戻り値の型から決まり、両者は一致する必要があります。

getter にアノテーション付けることで、下記項目の設定が可能です。

- オプションの必須化
- デフォルト値
- ヘルプテキスト

##### オプションの必須化

`@Validation.Required` を付けたオプションが実行時に指定されない場合、エラーとなります。

```java
@Validation.Required
T getMyOption();
```

##### デフォルト値

`@Default.{{型}}({{デフォルト値}})` で、オプションのデフォルト値を設定できます。

```java
@Default.String("default_value")
String getMyOption();
```

##### ヘルプテキスト

`@Description("{{ヘルプテキスト}}")` で、オプションのヘルプテキストを設定できます。

```java
@Description("This is a help text")
T getMyOption();
```

コマンドラインでヘルプテキストを表示できるようにするには、`PipelineOptionsFactory` へオプションを登録しておく必要があります。

```java
PipelineOptionsFactory.register(CustomOptions.class);
```

上記設定により、以下コマンドでヘルプを表示することができます。

```bash
$ mvn -q compile exec:java \
    -D exec.mainClass=path.to.class \
    -D exec.args="--help=${オプション クラス名}"
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
