### 一、Java引用介绍

 众所周知，Java中是JVM负责内存的分配和回收，这是它的优点（使用方便，程序不用再像使用c那样操心内存），但同时也是它的缺点（不够灵活）。为了解决内存操作不灵活这个问题，可以采用软引用等方法。

在JDK1.2以前的版本中，当一个对象不被任何变量引用，那么程序就无法再使用这个对象。也就是说，只有对象处于可触及状态，程序才能使用它。这 就像在日常生活中，从商店购买了某样物品后，如果有用，就一直保留它，否则就把它扔到垃圾箱，由清洁工人收走。一般说来，如果物品已经被扔到垃圾箱，想再 把它捡回来使用就不可能了。

但有时候情况并不这么简单，你可能会遇到类似鸡肋一样的物品，食之无味，弃之可惜。这种物品现在已经无用了，保留它会占空间，但是立刻扔掉它也不划算，因 为也许将来还会派用场。对于这样的可有可无的物品，一种折衷的处理办法是：如果家里空间足够，就先把它保留在家里，如果家里空间不够，即使把家里所有的垃 圾清除，还是无法容纳那些必不可少的生活用品，那么再扔掉这些可有可无的物品。

从JDK1.2版本开始，把对象的引用分为四种级别，从而使程序能更加灵活的控制对象的生命周期。

>Java从1.2版本开始引入这4种引用的级别由高到低依次为： 强引用  >  软引用  >  弱引用  >  虚引用
   
#### 1、强引用（StrongReference）

强引用是使用最普遍的引用。如果一个对象具有强引用，那垃圾回收器绝不会回收它。当内存空间不足，Java虚拟机宁愿抛出OutOfMemoryError错误，使程序异常终止，也不会靠随意回收具有强引用的对象来解决内存不足的问题。

#### 2、软引用（SoftReference）

如果一个对象只具有软引用，则内存空间足够，垃圾回收器就不会回收它；如果内存空间不足了，就会回收这些对象的内存。只要垃圾回收器没有回收它，该对象就可以被程序使用。软引用可用来实现内存敏感的高速缓存。

软引用可以和一个引用队列（ReferenceQueue）联合使用，如果软引用所引用的对象被垃圾回收器回收，Java虚拟机就会把这个软引用加入到与之关联的引用队列中。

#### 3、弱引用（WeakReference）

弱引用与软引用的区别在于：只具有弱引用的对象拥有更短暂的生命周期。在垃圾回收器线程扫描它所管辖的内存区域的过程中，一旦发现了只具有弱引用的对象，不管当前内存空间足够与否，都会回收它的内存。不过，由于垃圾回收器是一个优先级很低的线程，因此不一定会很快发现那些只具有弱引用的对象。

弱引用可以和一个引用队列（ReferenceQueue）联合使用，如果弱引用所引用的对象被垃圾回收，Java虚拟机就会把这个弱引用加入到与之关联的引用队列中。

#### 4、虚引用（PhantomReference）

“虚引用”顾名思义，就是形同虚设，与其他几种引用都不同，虚引用并不会决定对象的生命周期。如果一个对象仅持有虚引用，那么它就和没有任何引用一样，在任何时候都可能被垃圾回收器回收。

虚引用主要用来跟踪对象被垃圾回收器回收的活动。虚引用与软引用和弱引用的一个区别在于：虚引用必须和引用队列 （ReferenceQueue）联合使用。当垃圾回收器准备回收一个对象时，如果发现它还有虚引用，就会在回收对象的内存之前，把这个虚引用加入到与之 关联的引用队列中。

由于引用和内存回收关系紧密。下面，先通过实例对内存回收有个认识；然后，进一步通过引用实例加深对引用的了解。

### 二、内存回收

创建公共类MyDate，它的作用是覆盖finalize()函数：在finalize()中输出打印信息，方便追踪。

说明：finalize()函数是在JVM回收内存时执行的，但JVM并不保证在回收内存时一定会调用finalize()。

MyDate代码如下：

```
public class MyDate extends Date { 

    /** Creates a new instance of MyDate */
    public MyDate() {
    }
    // 覆盖finalize()方法
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("obj [Date: " + this.getTime() + "] 被 gc 执行了");
    }   

    public String toString() {
        return "Date: " + this.getTime();
    }
}

```

在这个类中，对java.util.Date类进行了扩展，并重写了finalize()和toString()方法。

创建公共类ReferenceTest，它的作用是定义一个方法drainMemory()：消耗大量内存，以此来引发JVM回收内存。

ReferenceTest代码如下：
```
public class ReferenceTest {
    /** Creates a new instance of ReferenceTest */
    public ReferenceTest() {
    }   
    
    // 消耗大量内存
    public static void drainMemory() {
        String[] array = new String[1024 * 10];
        for(int i = 0; i < 1024 * 10; i++) {
            for(int j = 'a'; j <= 'z'; j++) {
                array[i] += (char)j;
            }           
        }
    }
} 
```

在这个类中定义了一个静态方法drainMemory()，此方法旨在消耗大量的内存，促使JVM运行垃圾回收。

有了上面两个公共类之后，我们即可测试JVM什么时候进行垃圾回收。下面分3种情况进行测试：

#### 情况1：清除对象

实现代码：

```
    /**
     * 情况1：清除对象
     */
    private static void test01() {
        MyDate date = new MyDate();
        date = null;
    }
```

运行结果：

><无任何输出>

结果分析：date虽然设为null，但由于JVM没有执行垃圾回收操作，MyDate的finalize()方法没有被运行。

 

#### 情况2：显式调用垃圾回收

实现代码： 

```
    public void test02() {
        MyDate date02 = new MyDate();
        date02 = null;
        System.gc();
    }
```

运行结果：

>obj [Date: 1500975617709] 被 gc 执行了

结果分析：调用了System.gc()，使JVM运行垃圾回收，MyDate的finalize()方法被运行。

 

#### 情况3：隐式调用垃圾回收

实现代码： 

```
    public void test03() {
        MyDate date = new MyDate();
        date = null;
        ReferenceTest.drainMemory();
    }
```

运行结果：

>obj [Date: 1500975676028] 被 gc 执行了

结果分析：虽然没有显式调用垃圾回收方法System.gc()，但是由于运行了耗费大量内存的方法，触发JVM进行垃圾回收。

 
总结：JVM的垃圾回收机制，在内存充足的情况下，除非你显式调用System.gc()，否则它不会进行垃圾回收；在内存不足的情况下，垃圾回收将自动运行

### Java对引用的分类

#### 1、强引用（StrongReference）

强引用是使用最普遍的引用。如果一个对象具有强引用，那垃圾回收器绝不会回收它。如下：

```
Object object = new Object();   //  强引用  
```
  
当内存空间不足，Java虚拟机宁愿抛出OutOfMemoryError错误，使程序异常终止，也不会靠随意回收具有强引用的对象来解决内存不足的问题。如果不使用时，要通过如下方式来弱化引用，如下：

```
object = null;     // 帮助垃圾收集器回收此对象 
```

 显式地设置o为null，或超出对象的生命周期范围，则gc认为该对象不存在引用，这时就可以回收这个对象。具体什么时候收集这要取决于gc的算法。
举例：

```
public void test(){  
    Object o=new Object();  
    // 省略其他操作  
}
```

在一个方法的内部有一个强引用，这个引用保存在栈中，而真正的引用内容（Object）保存在堆中。当这个方法运行完成后就会退出方法栈，则引用内容的引用不存在，这个Object会被回收。
但是如果这个o是全局的变量时，就需要在不用这个对象时赋值为null，因为强引用不会被垃圾回收。
强引用在实际中有非常重要的用处，举个ArrayList的实现源代码：

```
private transient Object[] elementData;  
public void clear() {  
        modCount++;  
        // Let gc do its work  
        for (int i = 0; i < size; i++)  
            elementData[i] = null;  
        size = 0;  
} 
```

在ArrayList类中定义了一个私有的变量elementData数组，在调用方法清空数组时可以看到为每个数组内容赋值为null。
不同于elementData=null，强引用仍然存在，避免在后续调用 add()等方法添加元素时进行重新的内存分配。
使用如clear()方法中释放内存的方法对数组中存放的引用类型特别适用，这样就可以及时释放内存。

#### 2、软引用（SoftReference）

 如果一个对象只具有软引用，则内存空间足够，垃圾回收器就不会回收它；如果内存空间不足了，就会回收这些对象的内存。只要垃圾回收器没有回收它，该对象就可以被程序使用。软引用可用来实现内存敏感的高速缓存。
 
 ```
 String str=new String("abc");                                     // 强引用  
 SoftReference<String> softRef=new SoftReference<String>(str);     // 软引用   
 ```

   当内存不足时，等价于：
   
```
   If(JVM.内存不足()) {  
      str = null;  // 转换为软引用  
      System.gc(); // 垃圾回收器进行回收  
   }  
```

虚引用在实际中有重要的应用，例如浏览器的后退按钮。按后退时，这个后退时显示的网页内容是重新进行请求还是从缓存中取出呢？这就要看具体的实现策略了。

（1）如果一个网页在浏览结束时就进行内容的回收，则按后退查看前面浏览过的页面时，需要重新构建

（2）如果将浏览过的网页存储到内存中会造成内存的大量浪费，甚至会造成内存溢出

这时候就可以使用软引用

```
Browser prev = new Browser();               // 获取页面进行浏览  
SoftReference sr = new SoftReference(prev); // 浏览完毕后置为软引用         
if(sr.get()!=null){   
    rev = (Browser) sr.get();           // 还没有被回收器回收，直接获取  
}else{  
    prev = new Browser();               // 由于内存吃紧，所以对软引用的对象回收了  
    sr = new SoftReference(prev);       // 重新构建  
}  
```

这样就很好的解决了实际的问题。

       软引用可以和一个引用队列（ReferenceQueue）联合使用，如果软引用所引用的对象被垃圾回收器回收，Java虚拟机就会把这个软引用加入到与之关联的引用队列中。

#### 3、弱引用（WeakReference）

弱引用与软引用的区别在于：只具有弱引用的对象拥有更短暂的生命周期。在垃圾回收器线程扫描它所管辖的内存区域的过程中，一旦发现了只具有弱引用的对象，不管当前内存空间足够与否，都会回收它的内存。不过，由于垃圾回收器是一个优先级很低的线程，因此不一定会很快发现那些只具有弱引用的对象。
  
```
tring str=new String("abc");      
WeakReference<String> abcWeakRef = new WeakReference<String>(str);  
str=null;   

```  

当垃圾回收器进行扫描回收时等价于：

```
str = null;  
System.gc();  
```

如果这个对象是偶尔的使用，并且希望在使用时随时就能获取到，但又不想影响此对象的垃圾收集，那么你应该用 Weak Reference 来记住此对象。

下面的代码会让str再次变为一个强引用：

```
String  abc = abcWeakRef.get();  
```

弱引用可以和一个引用队列（ReferenceQueue）联合使用，如果弱引用所引用的对象被垃圾回收，Java虚拟机就会把这个弱引用加入到与之关联的引用队列中。
当你想引用一个对象，但是这个对象有自己的生命周期，你不想介入这个对象的生命周期，这时候你就是用弱引用。

这个引用不会在对象的垃圾回收判断中产生任何附加的影响。

```
public class ReferenceTest {  
  
    private static ReferenceQueue<VeryBig> rq = new ReferenceQueue<VeryBig>();  
  
    public static void checkQueue() {  
        Reference<? extends VeryBig> ref = null;  
        while ((ref = rq.poll()) != null) {  
            if (ref != null) {  
                System.out.println("In queue: " + ((VeryBigWeakReference) (ref)).id);  
            }  
        }  
    }  
  
    public static void main(String args[]) {  
        int size = 3;  
        LinkedList<WeakReference<VeryBig>> weakList = new LinkedList<WeakReference<VeryBig>>();  
        for (int i = 0; i < size; i++) {  
            weakList.add(new VeryBigWeakReference(new VeryBig("Weak " + i), rq));  
            System.out.println("Just created weak: " + weakList.getLast());  
  
        }  
  
        System.gc();   
        try { // 下面休息几分钟，让上面的垃圾回收线程运行完成  
            Thread.currentThread().sleep(6000);  
        } catch (InterruptedException e) {  
            e.printStackTrace();  
        }  
        checkQueue();  
    }  
}  
  
class VeryBig {  
    public String id;  
    // 占用空间,让线程进行回收  
    byte[] b = new byte[2 * 1024];  
  
    public VeryBig(String id) {  
        this.id = id;  
    }  
  
    protected void finalize() {  
        System.out.println("Finalizing VeryBig " + id);  
    }  
}  
  
class VeryBigWeakReference extends WeakReference<VeryBig> {  
    public String id;  
  
    public VeryBigWeakReference(VeryBig big, ReferenceQueue<VeryBig> rq) {  
        super(big, rq);  
        this.id = big.id;  
    }  
  
    protected void finalize() {  
        System.out.println("Finalizing VeryBigWeakReference " + id);  
    }  
}  
```

最后的输出结果为：

```
Just created weak: com.javabase.reference.VeryBigWeakReference@1641c0  
Just created weak: com.javabase.reference.VeryBigWeakReference@136ab79  
Just created weak: com.javabase.reference.VeryBigWeakReference@33c1aa  
Finalizing VeryBig Weak 2  
Finalizing VeryBig Weak 1  
Finalizing VeryBig Weak 0  
In queue: Weak 1  
In queue: Weak 2  
In queue: Weak 0  
```

#### 4、虚引用（PhantomReference）

“虚引用”顾名思义，就是形同虚设，与其他几种引用都不同，虚引用并不会决定对象的生命周期。如果一个对象仅持有虚引用，那么它就和没有任何引用一样，在任何时候都可能被垃圾回收器回收。

虚引用主要用来跟踪对象被垃圾回收器回收的活动。虚引用与软引用和弱引用的一个区别在于：虚引用必须和引用队列 （ReferenceQueue）联合使用。当垃圾回收器准备回收一个对象时，如果发现它还有虚引用，就会在回收对象的内存之前，把这个虚引用加入到与之 关联的引用队列中。

|引用类型|被垃圾回收时间|用途|生存时间               |
|:-------------:|:-------------:| :-----:|:------:|
| 强引用 | 从来不会| 对象的一般状态 | JVM停止运行时终止|
|软引用 | 在内存不足时| 对象缓存 | 内存不足时终止|
| 弱引用 | 在垃圾回收时| 对象缓存| gc运行后终止|
| 虚引用 | Unknown| Unknown| Unknown|

