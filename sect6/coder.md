[topへ](../index.md)

# データのエンコードと型安全性
Beamでは全ての`PCollection`は`Coder`の指定が必須です。ワーカーに渡されるデータは、中間データも含めてバイト列と相互に変換できる必要がありますが、この変換を担うのが`Coder`です。  
ちなみに、バイト列への変換がencode、バイト列からの復元がdecodeです。

JavaのBeam SDKでは、様々な型（Integer, Long, String, ... etc.）に対して`Coder`サブクラスが事前定義されています。型推定が上手く動作する場合は、暗黙的に`Coder`サブクラスが指定されるため、`Coder`を意識する必要はありません。

> #### Memo
> 一つの型に対する`Coder`は一意である必要はありません。型変換をしない`PTransform`の前後で`Coder`を変更することもできます。  
> 例えば、`PTransform<Integer, Integer>`において、Inputは`BigEndianIntegerCoder`にしておき、Outputでは`VarIntCoder`を使う、のような具合です。  
> `Coder`を変更することで、シリアル化するデータ量を（おそらく）減らせることもあるので、`Coder`を活用できれば処理性能を上げられるかもしれません。


## <span class="head">Coderの指定</span>
`PCollection`の型宣言や、applyする`PTransform`の型引数から型推定ができるので、基本的には自動的に`Coder`を決まる場合が多いです。ですが、カスタムクラスを使う場合など、`Coder`の推定が出来ない場合はユーザ自身が明示的に`Coder`の指定をする必要があります。

`PCollection`の`Coder`を指定するには、`setCoder`メソッドを使います。たとえば、次のような感じです。

```java
PCollection<String> pCollection = ...;
pCollection.setCoder(StringUtf8Coder.of());
```

`Coder`がfinalizeされるのは、`PCollection`にtransformをapplyする段階です。

```java
PCollection<String> pCollection = ...;
pCollection.apply(...);

// transformのapply後にCoder指定をすると、graph構築の段階でビルドに失敗する
pCollection.setCoder(StringUtf8Coder.of());
```

Pipelineの各ステップは並列に実行され得ます。特定の型ではなく、特定の`PCollection`に注目した場合だと、encode / decodeの方法は統一されなければいけないのかと思います。

`PCollection`の`Coder`を調べるには、`getCoder`メソッドを用います。  

```java
pCollection<String> pCollection = ...;
System.out.pritnln(pcol.getCoder());    // (stdout) StringUtf8Coder
```

`Coder`を指定しておらず、`Coder`の自動決定ができない場合、`getCoder`メソッドはエラーを返します。

`Create`を使って`PCollection`を作る場合は、`withCoder`メソッドで`Coder`の指定します。

```java
Pipeline pipeline = Pipeline.create();
pipeline.apply(Create.of(...).withCoder(...));
```

`Create`は引数の型情報を参照しないらしいので、型推定を信頼せずに`withCoder`で明示的に`Coder`を指定した方が良いようです。

[コードサンプル](./codes/setCoder.md)

## <span class="head">デフォルトのCoderを指定する</span>
`Pipeline`オブジェクトは`CoderRegistry`を持っていて、これを使うとdefaultの`Coder`を取得したり、defaultの`Coder`を登録したりすることができます。

`CoderRegistry`は、`getCoderRegistry`を使って取得できます。

```java
import org.apache.beam.sdk.coders.CoderRegistry;

Pipeline pipeline = ...
CoderRegistry cr = pipeline.getCoderRegistry();
```

手抜きのカスタムクラスですが、これにdefaultの`Coder`を登録することを考えます。

```java
class MyCustomDataType implements Serializable {
  public int a;
  public int b;
}
```

`registerCoderForClass`を使うと、クラスに対するdefaultのCoderが指定できます。

```java
cr.registerCoderForClass(
    MyCustomDataType.class,
    // SerializableCoderは、java.io.Serializableを実装してるクラスに
    // 使用可能で、Beamが上手いことencode/decodeしてくれます。
    SerializableCoder.of(MyCustomDataType.class));
```


`CoderRegistry`から`Coder`を取得するには、`getCoder`を使います。実引数にはClassオブジェクトを渡します。このメソッドを使うには、例外の捕捉が必要です。

```java
import org.apache.beam.sdk.coders.CannotProvideCoderException;

try {
  cr.getCoder(MyCustomDataType.class);
} catch (CannotProvideCoderException e) {}
```

[コードサンプル](./codes/coderRegistry.md)

## <span class="head">カスタムクラスのCoderを指定する</span>
上のように`CoderRegistry`を使っても良いのですが、特に使い回しをするカスタムクラスに対して`Coder`の指定を何度も行うのはやや面倒です。

カスタムクラスを定義する際、`@DefaultCoder`アノテーションをつけることでも、defaultの`Coder`を指定することができます。

```java
@DefaultCoder(SerializableCoder.class)
public class MyCustomDataType implements Serializable { ... }
```

これをやっておくと`Coder`の登録なしに、複数のPipelineでカスタムクラスを使い回すことができます。

[コードサンプル](./codes/defaultCoder.md)

## <span class="head">カスタムCoderの作成 - 手抜き編</span>
`SerializableCoder`に丸投げにするので十分かと思いますが、カスタムCoderを作成する方法をまとめておきます。`Coder`サブクラスを作るのですが、最低限必要になるのは、

+ Coderサブクラスがシングルトンになるようにする (必須かは不明...)  
&rarr; Beamのコードを読む限りは必要そうです
+ encodeメソッドのOverride
+ decodeメソッドのOverride

です。例として、

```java
class MyCustomData implements Serializable {
  public int a;
  public int b;
  
  // ... 中略
}
```

に対するCoderを作ってみます。メンバ変数のencode, decodeはBuilt-inの`Coder`である、`BigEndianIntegerCoder`を使うのが簡単です。

シングルトンにするのはそれほど難しくなく、Constructorへのアクセスを制限してやれば良いです。のちの都合上、メンバ変数用のCoderをここで作っておきます。

```java
class MyCoder extends Coder<T> {
  private static final MyCoder INSTANCE = new MyCoder();
  // Beam SDKのCoderを使う
  private final Coder<Integer> aCoder;
  private final Coder<Integer> bCoder;

  // コンストラクタ
  private TinyIntegerCoder() {
    this.aCoder = BigEndianIntegerCoder.of();
    this.bCoder = BigEndianIntegerCoder.of();
  }
  
  // Coderインスタンスを返すメソッド
  public static MyCoder of() {
    return INSTANCE;
  }
  
  // ... 中略
}
```

encodeではバイト列への変換、decodeではバイト列から元の型への復元を行います。実際のシリアル化、デシリアル化はBuilt-inのCoderに丸投げにするわけです。  
一つ注意点として、全データを直列に並べるため、encode / decodeの処理順序を変えると、当然ながらデータがおかしくなります。

```java
@Override
public void encode(MyCustomData value, OutputStream outStream)
    throws CoderException, IOException {
  // decodeする順序と合わせる必要がある
  aCoder.encode(value.a, outStream);
  bCoder.encode(value.b, outStream);
}

@Override
public MyCustomData decode(InputStream inStream)
    throws CoderException, IOException {
  // encodeの順序と合わせる必要がある
  int a = aCoder.decode(inStream);
  int b = bCoder.decode(inStream);
  return new MyCustomData(a, b);
}
```

追加で、後二つメソッドのOverrideが必須なのですが、手を抜いて差し支えないです。詳しくは、[コードサンプル](./codes/simpleCustomCoder.md)を確認してください...。

```java
@Override
public List<? extends Coder<?>> getCoderArguments() {
  return Collections.emptyList();
}

@Override
public void verifyDeterministic() throws NonDeterministicException {
}
```

## <span class="head">カスタムCoderの作成 - 本格編</span>
手抜き編でも面倒なので、本格編は真面目に説明しません。  
大雑把な流れとしては、`java.io.DataInputStream`と`java.io.DataOutputStream`を使って、自分で元データとバイト列の間の相互変換を実装します。

[コードサンプル](./codes/customCoder.md)では、IntegerのCoderを作っています。ただ、下位1バイトしかencode, decodeしないようにしています。最終結果も、あえておかしくなるようにしています。