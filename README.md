# AlginProject
自定义左右对齐的TextView

>左右对齐的文字效果，很常见，在大多数文章上都可以看到，这种效果能给人带来一种界面整洁的效果，但是如果只是左对齐的话，页面显示就会参差不齐；而app的页面大多数都是图形+文字的显示效果，左右对齐的文字效果需求还是非常大的；而反观Android的官方控件TextView并没有提供这种左右对齐的显示效果，那么就只能自己来造轮子了；

这里会涉及到实现原理的解析，如果只是需要使用轮子，请拉到最底下；


### 1. TextView 效果
首先，让我们先来看一下TextView的显示效果：
![textView.png](https://user-gold-cdn.xitu.io/2020/2/5/1701387d2fc410cd?w=608&h=846&f=png&s=99717)


中文情况下还好，但是英文情况下的显示效果就不是很好看了，右边会留了很长的空白，对于追求用户体验的，这样的显示效果肯定是不能满足的；但是TextView内部也没有提供API给我们，那么就只能自己来实现了；


### 2. 对齐原理
原理：将一行剩余的宽度平分给当前单词的间距，这样来达到左右对齐的效果；
那么我们就有两种实现方案：  

（1）TextView绘制一行的计算原理简单粗暴，就是计算这一行显示不下一个单词的时候，就进行回车换行；TextView已经手动给我们计算出了一行能显示多少的字符，那么我们只需要通过计算剩余的宽度再进行绘制即可；  

会存在问题：如果一行存在单词较少的情况，就会出现间隔过大的问题；

比如：

![效果1.png](https://user-gold-cdn.xitu.io/2020/2/5/1701387d30db70eb?w=636&h=96&f=png&s=7377)

（2）通过手动计算一行能显示多少个字符，然后再计算剩余的宽度进行绘制；
这样只是比第一步多了个自己计算一行能显示多少个字符的操作；  
但是这样也会存在问题：如果单词存在中英文混合，或者非中文的情况，会很大概率出现换行时单词被截断的问题；
比如：

![效果2.png](https://user-gold-cdn.xitu.io/2020/2/5/1701387d310a8b89?w=632&h=408&f=png&s=57003)

### 3. 最终方案
以上两种方案都会存在缺陷，那这样的话我们就得对出现的问题提出解决方案；  
当前市场上有成熟的阅读软件，最常用的就是左右对齐的排版效果，来看看当前的阅读软件是怎么解决这些问题的；
先看一下微信读书app的显示效果：

![微信读书.png](https://upload-images.jianshu.io/upload_images/5274018-288ab08d7803b961?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

再看一下掌阅app的显示效果：

![掌阅.png](https://user-gold-cdn.xitu.io/2020/2/5/1701387d342e5e3e?w=650&h=1374&f=png&s=738572)

微信读书和掌阅都是对这个过长的单词进行截断处理，然后使用“-”符号将这两个截断的单词连接起来，再进行绘制时就不会出现上述两种方案的问题；

那么最终方案就是：

通过手动计算一行能显示多少个单词，如果一行最后一个单词显示不下，则进行截断处理，中文则不不存在该问题，这里针对非中文的处理；然后再根据剩余空间进行绘制；
那么有了方案之后，接下来看看具体要怎么实现；

### 4. 最终实现  
（1）先遍历当前页面的字符，将中英文截取为一个个单词，具体实现在getWordList(String text)方法里面；通过遍历当前的字符，判断如果为中文时，则为一个单词，非中文时则通过遍历该英文单词进行拼接，最后拼成一个非中文单词；

（2）通过遍历当前的单词，计算每一行要显示的单词集合，具体实现在getLineList(List<String> frontList)这个方法里面；遍历单词时，当添加最后一个单词时，宽度已经超过一行显示的距离，那么就判断最后一个单词是否为中文，是的话则添加到下一行，否则的话则进行截断处理；

（3）通过第二步计算出来的每一行的单词，计算剩余的距离进行绘制；

![具体流程.png](https://user-gold-cdn.xitu.io/2020/2/5/1701387d3488742f?w=618&h=1070&f=png&s=19091)

### 5. 优化点

（1）代码里修改了绘制的逻辑后，那么关于TextView的一些基础属性也要进行适配；比如布局的方向，可以使用TextView自带的属性来进行设置；通过android:gravity=""和android:textAlignment=""属性来定义布局的文字方向，是居左还是居右边，这两个属性都可以进行设置，textAlignment属性的优先级比较高，如果同时设置的话，那么则以textAlignment属性为准；


```
<com.example.testdemo1.XQJustifyTextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="10dp"
        android:textSize="16sp"
        android:gravity="start"
        android:textAlignment="textStart"/>
```

（2）对于实现的英文单词截断的效果，还有优化的地方，经过一些小伙伴的提醒，发现还可以通过音节的进行拆分，再去研究了一波微信读书和掌阅的截断效果，发现确实是使用了音节来进行截断，这样展示效果就更加完美了；

后面研究了一波资料后，发现可以通过元音来进行截断，英语单词的元音有5个，分别是a,e,i,o,u; 那么就可以通过元音加一个辅音的规则来进行截断；比如an,en之类的；这里只实现了一种规则来进行截断，其他的规则过于复杂，暂时没有引入；

看一下优化后的效果：


![](https://user-gold-cdn.xitu.io/2020/2/11/1703023f90ba51cb?w=630&h=1056&f=png&s=261233)

## 备注

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
