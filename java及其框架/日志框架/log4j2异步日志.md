[toc]



# 介绍

log4j2最大的特点就是异步日志，其性能的提升主要也是从异步日志中受益，我们来看看如何使用log4j2的异步日志。

Log4j2提供了两种实现日志的方式，一个是通过AsyncAppender，一个是通过AsyncLogger，分别对应前面我们说的Appender组件和Logger组件。注意这是两种不同的实现方式，在设计和源码上都是不同的体现。



# AsyncAppender方式

AsyncAppender是通过引用别的Appender来实现的，当有日志事件到达时，会开启另外一个线程来处理它们。需要注意的是，如果在Appender的时候出现异常，对应用来说是无法感知的。 AsyncAppender应该在它引用的Appender之后配置，默认使用 java.util.concurrent.ArrayBlockingQueue实现而不需要其它外部的类库。 当使用此Appender的时候，在多线程的环境下需要注意，阻塞队列容易受到锁争用的影响，这可能会对性能产生影响。这时候，我们应该考虑使用无所的异步记录器（AsyncLogger）。

例子:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="warn" name="MyApp" packages="">
  <Appenders>
    <File name="MyFile" fileName="logs/app.log">
      <PatternLayout>
        <Pattern>%d %p %c{1.} [%t] %m%n</Pattern>
      </PatternLayout>
    </File>
    <Async name="Async">
      <AppenderRef ref="MyFile"/>
    </Async>
  </Appenders>
  <Loggers>
    <Root level="error">
      <AppenderRef ref="Async"/>
    </Root>
  </Loggers>
</Configuration>
```



# AsyncLogger方式

AsyncLogger才是log4j2 的重头戏，也是官方推荐的异步方式。它可以使得调用Logger.log返回的更快。你可以有两种选择：全局异步和混合异步。

- 全局异步就是，所有的日志都异步的记录，在配置文件上不用做任何改动，只需要在jvm启动的时候增加一个参数；

- 混合异步就是，你可以在应用中同时使用同步日志和异步日志，这使得日志的配置方式更加灵活。因为Log4j文档中也说了，虽然Log4j2提供以一套异常处理机制，可以覆盖大部分的状态，但是还是会有一小部分的特殊情况是无法完全处理的，比如我们如果是记录审计日志，那么官方就推荐使用同步日志的方式，而对于其他的一些仅仅是记录一个程序日志的地方，使用异步日志将大幅提升性能，减少对应用本身的影响。混合异步的方式需要通过修改配置文件来实现，使用AsyncLogger标记配置。



例子:

**全局异步**

```xml
<?xml version="1.0" encoding="UTF-8"?>
 
<!-- Don't forget to set system property
-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector
     to make all loggers asynchronous. -->
 
<Configuration status="WARN">
  <Appenders>
    <!-- Async Loggers will auto-flush in batches, so switch off immediateFlush. -->
    <RandomAccessFile name="RandomAccessFile" fileName="async.log" immediateFlush="false" append="false">
      <PatternLayout>
        <Pattern>%d %p %c{1.} [%t] %m %ex%n</Pattern>
      </PatternLayout>
    </RandomAccessFile>
  </Appenders>
  <Loggers>
    <Root level="info" includeLocation="false">
      <AppenderRef ref="RandomAccessFile"/>
    </Root>
  </Loggers>
</Configuration>
```

加载JVM启动参数里

`java -Dog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector`



**混合异步**

```xml
<?xml version="1.0" encoding="UTF-8"?>
 
<!-- No need to set system property "log4j2.contextSelector" to any value
     when using <asyncLogger> or <asyncRoot>. -->
 
<Configuration status="WARN">
  <Appenders>
    <!-- Async Loggers will auto-flush in batches, so switch off immediateFlush. -->
    <RandomAccessFile name="RandomAccessFile" fileName="asyncWithLocation.log"
              immediateFlush="false" append="false">
      <PatternLayout>
        <Pattern>%d %p %class{1.} [%t] %location %m %ex%n</Pattern>
      </PatternLayout>
    </RandomAccessFile>
  </Appenders>
  <Loggers>
    <!-- pattern layout actually uses location, so we need to include it -->
    <AsyncLogger name="com.foo.Bar" level="trace" includeLocation="true">
      <AppenderRef ref="RandomAccessFile"/>
    </AsyncLogger>
    <Root level="info" includeLocation="true">
      <AppenderRef ref="RandomAccessFile"/>
    </Root>
  </Loggers>
</Configuration>
```

root logger就是同步的，但是com.foo.Bar的logger就是异步的。



# 总结

1、Log4j 2的异步记录日志在一定程度上提供更好的吞吐量，但是一旦队列已满，appender线程需要等待，这个时候就需要设置等待策略，AsyncAppender是依赖于消费者最序列最后的消费者，会持续等待。

2、因为AsyncLogger是采用Disruptor，通过环形队列无阻塞队列作为缓冲，多生产者多线程的竞争是通过CAS实现，无锁化实现，可以降低极端大的日志量时候的延迟尖峰，[Disruptor](https://www.cnblogs.com/lewis09/p/9974995.html) 可是号称一个线程里每秒处理600万订单的高性能队列







[Log4j2简介和异步日志梳理 - 简书 (jianshu.com)](https://www.jianshu.com/p/9f0c67facbe2)

[log4j2异步那些事(2)--AsyncLogger | BryantChang的博客](https://bryantchang.github.io/2019/01/15/log4j2-asyncLogger/)

[log4j2异步日志解读（二）AsyncLogger - lewis09 - 博客园 (cnblogs.com)](https://www.cnblogs.com/lewis09/p/10004117.html)