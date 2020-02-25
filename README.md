# AlginProject
自定义左右对齐的TextView

### 1.非中文单词不够一行会自动截断，用符号“-”连接起来；

### 2.适配布局的方向，使用原生TextView的属性：android:gravity=""和android:textAlignment=""，gravity的优先级较高，如果同时设置这两个属性则以textAlignment的属性为准；
```
<com.example.testdemo1.XQJustifyTextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="10dp"
        android:textSize="16sp"
        android:gravity="start"
        android:textAlignment="textStart"/>
```

### 3.英文情况下使用元音字母进行截断，如果没有找到元音字母则使用默认规则截断；

### 4.依赖Library
在主项目app的build.gradle中依赖
```
dependencies {
    ...
    implementation 'com.text:alginlib:1.0.1'
}
```
