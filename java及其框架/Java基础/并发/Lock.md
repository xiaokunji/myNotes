[toc]

# 1. 定义

在 Lock 接口出现之前，Java 程序是靠 synchronized 关键字实现锁功能的，而 Java SE 5之后，并发包中新增了 Lock 接口（以及相关实现类）用来实现锁功能，它提供了与synchronized 关键字类似的同步功能,只是在使用时需要显式地获取和释放锁。

> 虽然它缺少了（通过 synchronized 块或者方法所提供的）隐式获取释放锁的便捷性，但是却拥有了锁获取与释放的可操作性、可中断的获取锁以及超时获取锁等多种 synchronized 关键字所不具备的同步特性。  
>
> Lock也是维护了一个锁（state），和一个等待队列(AQS)，这也是Lock在底层实现的两个核心元素。AQS队列解决了线程同步的问题，volatile定义的锁状态解决保证了线程对于临界区代码访问的互斥，并且解决了各个线程对于锁状态的可见性问题。

Lock 是一个接口，它定义了锁获取和释放的基本操作  

![image-20211120030234082](https://gitee.com/xiaokunji/my-images/raw/master/myMD/lock接口的函数描述.png)



# 2. 使用

一般使用Lock会使用它的实现类, java中常用的就是ReentrantLock  类

```java
Lock lock = new ReentrantLock();
lock.lock();
try {
    // do some thing
} finally {
	lock.unlock();
}
```

> 不要将获取锁的过程写在 try 块中，因为如果在获取锁（自定义锁的实现） 时发生了异常，异常抛出的同时，也会导致锁无故释放。  



关键字synchronized与wait()和notify()/notifyAll()方法相结合可以实现等待/通知模式，类ReentrantLock也可以实现同样的功能，但需要借助于Condition对象

condition需要获得锁后使用

```java
private Lock lock = new ReentrantLock();
private Condition condition = lock.newCondition();
```

一把锁可以生成多个condition, (变量各自控制各自线程的等待和唤起)

当调用 await()方法后，当前线程会释放锁并在此等待，而其他线程调用 Condition 对象的 signal()方法，通知当前线程后，当前线程才从 await()方法返回，并且在返回前已经获取了锁



# 3. AQS

抽象队列同步器 AbstractQueuedSynchronizer（以下简称同步器），是用来构建锁或者其他同步组件的基础框架，它使用了一个 int 成员变量表示同步状态，通过内置的 FIFO 队列来完成资源获取线程的排队工作.

使用AQS能简单且高效地构造出应用广泛的同步器，比如我们提到的ReentrantLock，Semaphore，ReentrantReadWriteLock，SynchronousQueue，FutureTask等等皆是基于AQS的。

当然，我们自己也能利用AQS非常轻松容易地构造出符合我们自己需求的同步器，只要子类实现它的几个`protected`方法就可以了，

AQS内部使用了一个volatile的变量state来作为资源的标识。同时定义了几个获取和改变state的protected方法，子类可以覆盖这些方法来实现自己的逻辑：

```java
getState()
setState()
compareAndSetState()
```

这三种叫做均是原子操作，其中compareAndSetState的实现依赖于Unsafe的compareAndSwapInt()方法。

而AQS类本身实现的是一些排队和阻塞的机制，比如具体线程等待队列的维护（如获取资源失败入队/唤醒出队等）。它内部使用了一个先进先出（FIFO）的双端队列，并使用了两个指针head和tail用于标识队列的头部和尾部。其数据结构如图：

![img](http://concurrent.redspider.group/article/02/imgs/AQS%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84.png)



> 总结: 通过一个队列来让线程们排队执行,就做到了线程安全,  然后通过一个状态来做标识(例如ReentrantLock用这个状态来表示加锁次数), 
>
> > 在添加队列时需要获得(自己的)锁才能添加队列, 加到队列后让当前线程阻塞(park()函数),每次释放锁时,就把队列中的下一个线程启动(unpark()函数)
> >
> > [Java线程中断(Interrupt)与阻塞(park)的区别 - 周二鸭 - 博客园 (cnblogs.com)](https://www.cnblogs.com/jojop/p/13957027.html)



## 3.1 资源共享模式

资源有两种共享模式，或者说两种同步方式：

- 独占模式（Exclusive）：资源是独占的，一次只能一个线程获取。如ReentrantLock。

- 共享模式（Share）：同时可以被多个线程获取，具体的资源个数可以通过参数指定。如Semaphore/CountDownLatch。

  > 就是共享锁和独占锁

一般情况下，子类只需要根据需求实现其中一种模式，当然也有同时实现两种模式的同步类，如`ReadWriteLock`。

前面说到,AQS通过队列来控制获取线程工作, 这个队列是先进先出的双向队列,  两种模式下都用了这个队列, 通过`Node`类来控制, 它下面有 前驱节点,后驱节点, 下一个等待节点等等属性.

> 注意：通过Node我们可以实现两个队列，一是通过prev和next实现CLH队列(线程同步队列,双向队列)，二是nextWaiter实现Condition条件上的等待线程队列(单向队列)，这个Condition主要用在ReentrantLock类中。



## 3.2 AQS的主要方法源码解析

AQS的设计是基于**模板方法模式**的，它有一些方法必须要子类去实现的，它们主要有：

- isHeldExclusively()：该线程是否正在独占资源。只有用到condition才需要去实现它。
- tryAcquire(int)：独占方式。尝试获取资源，成功则返回true，失败则返回false。
- tryRelease(int)：独占方式。尝试释放资源，成功则返回true，失败则返回false。
- tryAcquireShared(int)：共享方式。尝试获取资源。负数表示失败；0表示成功，但没有剩余可用资源；正数表示成功，且有剩余资源。
- tryReleaseShared(int)：共享方式。尝试释放资源，如果释放后允许唤醒后续等待结点返回true，否则返回false。

这些方法虽然都是`protected`方法，但是它们并没有在AQS具体实现，而是直接抛出异常（这里不使用抽象方法的目的是：避免强迫子类中把所有的抽象方法都实现一遍，减少无用功，这样子类只需要实现自己关心的抽象方法即可

> 比如 Semaphore 只需要实现 tryAcquire 方法而不用实现其余不需要用到的模版方法）：
>
> ```java
> protected boolean tryAcquire(int arg) {
>     throw new UnsupportedOperationException();
> }
> ```



**以下仅讲述独占锁的方式**



### 3.2.1 获取资源

获取资源的入口是acquire(int arg)方法。arg是要获取的资源的个数，在独占模式下始终为1。我们先来看看这个方法的逻辑：

```java
public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

首先调用tryAcquire(arg)尝试去获取资源。前面提到了这个方法是在子类具体实现的。

如果获取资源失败，就通过addWaiter(Node.EXCLUSIVE)方法把这个线程插入到等待队列中。其中传入的参数代表要插入的Node是独占式的。

```java
private Node addWaiter(Node mode) {
    // 生成该线程对应的Node节点
    Node node = new Node(Thread.currentThread(), mode);
    // 将Node插入队列中
    Node pred = tail;
    if (pred != null) {
        node.prev = pred;
        // 使用CAS尝试，如果成功就返回
        if (compareAndSetTail(pred, node)) {
            pred.next = node;
            return node;
        }
    }
    // 如果等待队列为空或者上述CAS失败，再自旋CAS插入
    enq(node);
    return node;
}

// 自旋CAS插入等待队列
private Node enq(final Node node) {
    for (;;) {
        Node t = tail;
        if (t == null) { // Must initialize
            // 不知道为什么要单独new一个节点,而不是用入参, 而且它们表示的资源共享模式也不一样????????
            if (compareAndSetHead(new Node()))
                tail = head;
        } else {
            node.prev = t;
            if (compareAndSetTail(t, node)) {
                t.next = node;
                return t;
            }
        }
    }
}
```

> 上面的两个函数比较好理解，就是在队列的尾部插入新的Node节点，但是需要注意的是由于AQS中会存在多个线程同时争夺资源的情况，因此肯定会出现多个线程同时插入节点的操作，在这里是通过CAS自旋的方式保证了操作的线程安全性。

OK，现在回到最开始的aquire(int arg)方法。现在通过addWaiter方法，已经把一个Node放到等待队列尾部了。而处于等待队列的结点是从头结点一个一个去获取资源的。具体的实现我们来看看acquireQueued方法

```java
final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
        boolean interrupted = false;
        // 自旋
        for (;;) {
            final Node p = node.predecessor();
            // 如果node的前驱结点p是head，表示node是第二个结点，就可以尝试去获取资源了
            if (p == head && tryAcquire(arg)) {
                // 拿到资源后，将head指向该结点。
                // 所以head所指的结点，就是当前获取到资源的那个结点或null。
                setHead(node); 
                p.next = null; // help GC
                failed = false;
                return interrupted;
            }
            // 如果自己可以休息了，就进入waiting状态，直到被unpark()
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

> 这里parkAndCheckInterrupt方法内部使用到了LockSupport.park(this)，顺便简单介绍一下park。
>
> LockSupport类是Java 6 引入的一个类，提供了基本的线程同步原语。LockSupport实际上是调用了Unsafe类里的函数，归结到Unsafe里，只有两个函数：
>
> - park(boolean isAbsolute, long time)：阻塞当前线程
> - unpark(Thread jthread)：使给定的线程停止阻塞

所以**结点进入等待队列后，是调用park使它进入阻塞状态的。只有头结点的线程是处于活跃状态的**。

当然，获取资源的方法除了acquire外，还有以下三个：

- acquireInterruptibly：申请可中断的资源（独占模式）
- acquireShared：申请共享模式的资源
- acquireSharedInterruptibly：申请可中断的资源（共享模式）

> 可中断的意思是，在线程中断时可能会抛出`InterruptedException`

### 3.2.2 释放资源

释放资源相比于获取资源来说，会简单许多。在AQS中只有一小段实现。源码：

```java
public final boolean release(int arg) {
    if (tryRelease(arg)) {
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}

private void unparkSuccessor(Node node) {
    // 如果状态是负数，尝试把它设置为0
    int ws = node.waitStatus;
    if (ws < 0)
        compareAndSetWaitStatus(node, ws, 0);
    // 得到头结点的后继结点head.next
    Node s = node.next;
    // 如果这个后继结点为空或者状态大于0
    // 通过前面的定义我们知道，大于0只有一种可能，就是这个结点已被取消
    if (s == null || s.waitStatus > 0) {
        s = null;
        // 等待队列中所有还有用的结点，都向前移动
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t;
    }
    // 如果后继结点不为空，
    if (s != null)
        LockSupport.unpark(s.thread);
}
```













































































《Java并发编程艺术》

[11 AQS · 深入浅出Java多线程 (redspider.group)](http://concurrent.redspider.group/article/02/11.html)

[并发工具（锁）：深入Lock+Condition - 知乎 (zhihu.com)](https://zhuanlan.zhihu.com/p/333969353)