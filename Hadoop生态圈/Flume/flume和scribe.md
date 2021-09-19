Scribe是Facebook开源的分布式日志搜集系统，架构简单，日志格式灵活，且支持异步发送消息和队列

| 对比项            | Flume-NG                                                     | Scribe                                             |
| ----------------- | ------------------------------------------------------------ | -------------------------------------------------- |
| **使用语言**      | Java                                                         | c/c++                                              |
| **容错性**        | Agent和Collector间，Collector和Store间都有容错性，且提供三种级别的可靠性保证； | Agent和Collector间, Collector和Store之间有容错性； |
| **负载均衡**      | Agent和Collector间，Collector和Store间有LoadBalance和Failover两种模式 | 无                                                 |
| **可扩展性**      | 好                                                           | 好                                                 |
| **Agent丰富程度** | 提供丰富的Agent，包括avro/thrift socket, text, tail等        | 主要是thrift端口                                   |
| **Store丰富程度** | 可以直接写hdfs, text, console, tcp；写hdfs时支持对text和sequence的压缩； | 提供buffer, network, file(hdfs, text)等            |
| **代码结构**      | 系统框架好，模块分明，易于开发                               | 代码简单                                           |

 

> 来自* *<*[*http://www.aboutyun.com/thread-8317-1-1.html*](http://www.aboutyun.com/thread-8317-1-1.html)*>*