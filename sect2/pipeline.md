[topへ](../index.md)

# Pipelineの作成
まず`Pipeline`オブジェクトを作らなければ何も始まらないので、とりあえず作り方を...。

## <span class="head">オプション無しのPipeline</span>
JavaのBeam SDKで新たにインスタンスを作るときは、\*\*\*.create()の形が多いかもです。

```java
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;

public class Main {
    public static void main(String[] args){
	Pipeline p = Pipeline.create();
    }
}
```

## <span class="head">実行時オプションの設定</span>
Pipelineの入力元や、Dataflowを実行するGCPのプロジェクトIDを変更したりするたびに、いちいちBuildし直すのは面倒です。Pipelineの実行時にオプションを受け取れるようにしておくと便利です。

Pipelineの実行時オプションは、

1. Beam SDKのオプションクラス
2. カスタムオプションを作成

を経由して指定できます。SDKのオプションで指定できるのはGCPのプロジェクトIDとか、Beamが作成する中間ファイルの保存場所とかです。

### PipelineOptionsインスタンスの作成
コマンドライン引数（main関数の引数）を`PipelineOptions`インスタンスに渡すには、こんな感じのコードになります。

```java
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptions.PipelineOptionsFactory;

public class Main {
    public static void main(String[] args ) {
        PipelineOptions opt = PipelineOptionsFactory
                .fromArgs(args)
                .withValidation()
                .create();
        Pipeline p = Pipeline.create();
    }
}
```

`create`の前にメソッドを少し足してる。`withValidation()`は、コマンドライン引数のチェックをしてくれる。必須パラメータの指定があるか、とか整数にキャスト可能か、とかです。

### カスタムオプションの設定
PipelineOptionsを継承したインターフェースを定義します。Dataflowで使うならGcpOptionsの方が便利かもです。

作り方の違いとしては、`PipelineOptions`のインスタンスを作るのに`create`でなく、`as`を使うことです。  
やるべきは、設定したいオプションごとにgetterとsetterの型宣言をするのみです。オプションargAについてのメソッド名はそれぞれ、getArgA, setArgA、みたいにします。  
コマンドラインオプションでアクセスするときはargAです。

アノテーションを使って、default値、およびhelpメッセージも定義できます。

```java
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;

public class Main {
    public interface MyOptions extends PipelineOptions {
    	@Description("Help message")
    	@Default.String("Some default value")
        String getArgA();        // getter for option `argA`
    	void setArgA(String s);  // setter for option `argA`
    }
    
    public static void main(String[] args){
        // as()にはClass objectを渡す
        MyOptions opt = PipelineOptionsFactory
            .fromArgs(args)
            .withValidation()
            .as(MyOptions.class);
        // 実行時にargAを指定すればprintできます
        System.out.println( opt.getArgA() );
    }
}
```