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

# Pipelineの作成
まず`Pipeline`objectを作らなければ何も始まらないので、とりあえず作り方を...。

## <span class="head">オプション無しのPipeline</span>
Javaは型付けが静的だからBeamを使う上ではわりと安心できるが、各Objectの定義のためにimportが必要になるから、import文がごつくなるかもです。

```java=
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;

public class Main {
    public static void main(String[] args){
	Pipeline p = Pipeline.create( opt );
    }
}
```

JavaのBeam SDKで新たにインスタンスを作るときは、\*\*\*.create()の形が多いかもです。

## <span class="head">実行時オプションの設定</span>
`pipeline`のソースを変更したり、GCPのプロジェクトIDを変更したりするたびに、いちいちbuildし直すのは面倒。なので、`pipeline`が実行時オプションを受け取れるようにしておくと便利です。

`pipeline`への実行時オプション渡し方は二通り。
1. 前もってプログラム内で定義
2. command lineで実行時に指定

１つめについてですが、Beam SDKには`GcpOptions`とかいうクラスがあります。プロジェクトIDとかを受け付けますが、一つのGCPプロジェクトでしか使うつもりがないならハードコードで指定しておいた方が楽かもです。

### PipelineOptionsインスタンスの作成
コマンドライン引数（mainの引数のargs）を`PipelineOptions`インスタンスに渡すには、こんな感じのコードになります。

```java
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptions.PipelineOptionsFactory;

public class Main {
    public static void main(String[] args ) {
        PipelineOptions opt = PipelineOptionsFactory
                .fromArgs(args)
                .withValidation()
                .create();
    }
}
```

`create`の前にメソッドを少し足してる。`withValidation()`は、コマンドライン引数のチェックをしてくれる。型とか正規表現でのパターンマッチとか。
ただ、`create`メソッドで作られる`PipelineOptions`インスタンスが受け取れるオプションはかなり少ないうえ、あまり有り難みがないです...

### カスタムオプションの設定
PipelineOptionsを継承したインターフェースを定義します。Dataflowで使うならGcpOptionsの方が便利かもです。

作り方の違いとしては、`PipelineOptions`のインスタンスを作るのに`create`でなく、`as`を使うことです。  
インターフェースの中では、設定したいオプションごとにgetterとsetterの型宣言をすればよいです。オプションargAについてのメソッド名はそれぞれ、getArgA, setArgA、みたいにします。  
コマンドラインオプションでアクセスするときはargA。

アノテーションを使って、default値、およびhelpメッセージも定義できるが、各アノテーションのimportが必要。

```java=
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
        // .classはJava本来の機能です
        MyOptions opt = PipelineOptionsFactory
            .fromArgs(args)
            .withValidation()
            .as(MyOptions.class);
        // 実行時にargAを指定すればprintできます
        System.out.println( opt.getArgA() );
    }
}
```

補足として、上のcodeでは`pipeline`を実行しないし、作ってすらいないです。