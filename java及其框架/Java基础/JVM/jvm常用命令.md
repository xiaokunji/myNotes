[toc]

#  jps 

jps 命令类似与 linux 的 ps 命令，但是它只列出系统中所有的 Java 应用程序。 通过 jps 命令可以方便地查看 Java 进程的启动类、传入参数和 Java 虚拟机参数等信息。

> -q 只显示pid，不显示class名称,jar文件名和传递给main 方法的参数
>
> -m 输出传递给main 方法的参数，在嵌入式jvm上可能是null
>
> -l 输出应用程序main class的完整package名 或者 应用程序的jar文件完整路径名
>
> -v 输出传递给JVM的参数

# jinfo 

jinfo 是 JDK 自带的命令，可以用来查看正在运行的 java 应用程序的扩展参数，包括Java System属性和JVM命令行参数；也可以动态的修改正在运行的 JVM 一些参数。当系统崩溃时，jinfo可以从core文件里面知道崩溃的Java应用程序的配置信息

`jinfo Pid` 输出进程的基本信息。

# jstat 

jstack是jdk自带的线程堆栈分析工具，使用该命令可以查看或导出 Java 应用程序中线程堆栈信息。

`-gcutil Pid 5s`：每5秒输出一次GC情况。



# jstack

Jstat是JDK自带的一个轻量级小工具。全称“Java Virtual Machine statistics monitoring tool”，它位于java的bin目录下，主要利用JVM内建的指令对Java应用程序的资源和性能进行实时的命令行的监控，包括了对Heap size和垃圾回收状况的监控。

` -l Pid > /data/jstack.txt`：将指定进行的线程情况进行输出到指定文件中。

# jmap 

命令jmap是一个多功能的命令。它可以生成 java 程序的 dump 文件， 也可以查看堆内对象示例的统计信息、查看 ClassLoader 的信息以及 finalizer 队列。

`-histo Pid > /data/histo.txt`：将堆中对象统计信息输出到指定文件中。

`jmap -heap Pid > /data/jmap_heap.txt`：输出堆内存信息到指定文件中。

`jmap -J-d64 -dump:format=b,file=/data/heap_dump.bin Pid`：输出JVM的堆内容到指定文件中。



# jhat：Java堆分析工具

jhat（Java堆分析工具），是一个用来分析java的堆情况的命令。使用[jmap](http://www.hollischuang.com/archives/303)可以生成Java堆的Dump文件。生成转储文件之后就可以用jhat命令，将转储文件转成html的形式，然后通过http访问可以查看堆情况。

jhat命令解析会Java堆dump并启动一个web服务器，然后就可以在浏览器中查看堆的dump文件了。





[JVM常用命令 - 简书 (jianshu.com)](https://www.jianshu.com/p/d8f6c92ad438)

[JVM命令 - acehm - 博客园 (cnblogs.com)](https://www.cnblogs.com/laurarararararara/p/12465339.html)