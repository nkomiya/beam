## <span class="head">Coderのdefaultを指定する</span>
`Pipeline`オブジェクトは`CoderRegistry`を持っていて、これを使うとJavaの型に対してdefaultの`Coder`を指定することができます。

`CoderRegistry`は`Pipeline`オブジェクトの`getCoderRegistry`メソッドを使って取得可能です。