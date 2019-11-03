[topへ](../index.md)  
[Windowに戻る](overview.md)


# Build-inのwindow変換
[公式Guide](https://beam.apache.org/documentation/programming-guide/#provided-windowing-functions)で紹介されているBeamのbuilt-inのwindow変換を紹介します。

Sliding time windowを見ると分かりますが、要素は**少なくとも**一つのwindowに属し、かつ複数個のwindowに属することもあり得ます。

1. [Fixed time windows](./built-in/fixed.md)  
固定幅で重なりを**持たない** window
2. [Sliding time windows](./built-in/sliding.md)  
固定幅で重なりを**持たせた** window
