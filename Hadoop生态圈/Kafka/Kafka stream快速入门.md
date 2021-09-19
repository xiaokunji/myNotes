[toc]

## 1.1.  目标
本快速入门指南的目标是提供与KafkaStreams的第一个应用程序示例。我们将演示在你的第一个示例程序中，如果使用Kafka Streams库和演示一个简单的端到端的数据流。
值得注意的是，这种快速入门只涵盖了KafkaStreams的表面，这篇文档的剩余部分将会提供更多的细节，我们将在快速入门指南中为你指明方向。

## 1.2.  我们想做什么
在这个快速入门中，我们将运行包含Apachekafka的一个wordcount演示应用程序。下面代码的关键在于使用Java8的lambda表达式，易于阅读。(摘自WordCountLambdaExample):
```
[java] view plain copy
//序列化/反序列化Sting和Long类型  
final Serde<String> stringSerde = Serdes.String();  
final Serde<Long> longSerde = Serdes.Long();  
```
//通过指定输入topic “mystream”来构造KStream实例，  
//输入数据就以文本的形式保存在topic “mystream” 中。  
//(在本示例中，我们忽略所有消息的key.)  
`KStream<String, String> textLines = builder.stream(stringSerde, stringSerde, "mystream");  `
   
`KStream<String, Long> wordCounts = textLines`  
//以空格为分隔符，将每行文本数据拆分成多个单词。  
//这些文本行就是从输入topic中读到的每行消息的Value。  
//我们使用flatMapValues方法来处理每个消息Value，而不是更通用的`flatMap  
.flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))`  
//我们随后将调用countByKey来计算每个单词出现的次数  
//所以我们将每个单词作为map的key。  
`.map((key, value) -> new KeyValue<>(value, value))  `
//通过key来统计每个单词的次数  
//  
//这会将流类型从KStream<String,String>转为KTable<String,Long> (word-count).  
//因此我们必须提供String和long的序列化反序列化方法。  
    //  
`.countByKey(stringSerde, "Counts") ` 
//转化KTable<String,Long>到KStream<String,Long>  
    .toStream();  
   
//将KStream<String,Long>写入到输出topic中。  
`wordCounts.to(stringSerde, longSerde, "streams-wordcount-output");  `


在上面的代码执行过程中，我们将执行如下步骤：
- 1、  启动一台kafka集群
- 2、  使用Kafkaconsole producer命令行生产者客户端往Kafka Topic中写入示例输入数据
- 3、  在Java应用程序中使用kafkaStream库来处理输入数据。这里，我们使用了一个包含kafka的WordCount示例程序。
- 4、  使用Kafkaconsole consumer命令行消费者客户端检查应用程序的输出。
- 5、  停止Kafka集群

## 1.3.  启动Kafka 集群
在本章节中，我们会在一台机器上安装并启动Kafka集群。该集群有一个单节点Kafka(只有一个Broker)外加一个单节点Zookeeper构成。在wordcount演示程序中，这种集群依赖是必须的。我们假定kafka broker运行地址为localhost:9092, Zookeeper本地地址为localhost:2181。
首先，安装Oracle JRE或JDK 1.7及以上版本
然后，下载和安装包含Kafka Streams的新版本Apache Kafka. 为此，我们使用Confluent Platform 3.0.0版本。
(下面操作比较简单，所以不翻译了。)
```
[plain] view plain copy
# Download and install Confluent Platform 3.0.0 from ZIP archive  
$ wget http://packages.confluent.io/archive/3.0/confluent-3.0.0-2.11.zip  
$ unzip confluent-3.0.0-2.11.zip  
   
# *** IMPORTANT STEP ****  
# The subsequent paths and commands used throughout this quickstart assume that  
# your are in the following working directory:  
$ cd confluent-3.0.0/  
   
# Note: If you want to uninstall the Confluent Platform at the end of this quickstart,  
# run the following commands.  
#  
#   $ rm -rf confluent-3.0.0/  
#   $ rm -rf /var/lib/kafka          # Data files of Kafka  
#   $ rm -rf /var/lib/kafka-streams  # Data files of Kafka Streams  
#   $ rm -rf /var/lib/zookeeper      # Data files of ZooKeeper  
```
提示：可以通过Installationvia ZIP and TAR archives 和ConfluentPlatform Quickstart 获取更进一步信息。
我们首先启动ZooKeeper实例。该实例将监听本地2181端口。由于这是一个长期运行的服务，你应该在自己的终端中运行。
```
[plain] view plain copy
# Start ZooKeeper.  Run this command in its own terminal.  
$ ./bin/zookeeper-server-start ./etc/kafka/zookeeper.properties  
```
接下来，我们启动Kakfa的Broker，这将监听本地9092端口，然后连接到我们刚刚启动的Zookeeper实例。这也是一个长期运行的服务，也应该在终端中运行它。
```
[plain] view plain copy
# Start Kafka.  Run this command in its own terminal  
$ ./bin/kafka-server-start ./etc/kafka/server.properties  
```
现在，我们的单节点kafka集群已经完全运转起来了，我们就可以着手准备输入数据，运行我们的第一个kafka Streams示例程序。

## 1.4.  准备输入数据
提示：在本章节中，我们将使用内置的命令行工具来输入kakfa数据。在实际使用中，你应该通过其他方式将数据写入Kafka中，比如通过你自己应用程序中的Kafka客户端。
现在，我们将一些输入数据发送到Kafka的topic中，然后由Kafka Streams的应用程序做后续处理。
 首先，我们要创建名称为mystream的topic：
 ```
[plain] view plain copy
$ ./bin/kafka-topics.sh --create --zookeeper slave01:2181,slave02:2181,slave03:2181 --replication-factor 1 --partitions 1 --topic mystream  
```
下一步，我们生成一些输入数据并保存在本地文件/tmp/file-input.txt中。
```
[plain] view plain copy
$ echo -e "all streams lead to kafka\nhello kafka streams\njoin kafka summit" > /tmp/file-input.txt
```

生成的文件将包含如下内容：
```
[plain] view plain copy
all streams lead to kafka  
hello kafka streams  
join kafka summit  
```
最后，我们发送这些数据到input topic
```
[plain] view plain copy
$ cat /tmp/file-input.txt | ./bin/kafka-console-producer --broker-list master:9092,slave01:9092,slave02:9092,slave03:9092 --topic mystream  
```
Kafka console-producer从stdin中读取数据，并将每一行作为单独的消息发送到kafka的输入流中。该消息的key是null，消息是每行内容，使用字符串编码。   
注意: 你可能想知道这样一步步的快速启动和真实流处理系统的差异，在大型的实时的流处理系统中，数据总是在移动的，快速入门的目的仅仅是做功能证明。简单来说，一个端到端的数据管道建立在Kafka和Kafka Streams的各个方面。出于说教的原因，我们故意将快速入门清楚地拆分成一系列分开连续的步骤。   
 但在实践中，这些步骤通常会看起来有些不同并且会有并发的存在。比如输入数据可能不会来源于本地文件，而是直接从分布式系统中发送的，并且数据将被连续的写入Kafka。类似的，流处理应用程序可能在第一行数据发送之前就已经启动并运行。
 
## 1.5.  在KafkaStreams中处理输入数据
现在，我们已经生成了一些输入数据，我们可以运行我们的第一个基于Kafka Streams的java应用程序。   
我们将运行WordCount演示应用程序，它使用了ApacheKafka。它实现了WordCount算法，从输入文本来计算直方图。然而和其他你之前见过的操作被绑定在数据上的WordCount实例程序不同的是，这个示例程序是数据无界，无限流动的。和有界算法的变体类似，他是一个有状态的算法，跟踪并更新word的计数器。然后因为它必须接受无界的输入数据，它会周期性的输出其当前状态和计算结果，同时继续处理更多的数据，因为它不知道是否已经处理了所有的数据。这就是他和Hadoop 的Mapreduce算法之间的典型差异。一旦我们了解这种差异，检查了实际的输出数据之后，会更容易接受它。    
 由于wordCount示例程序与kafka打包在一起，已经在Kafka的Broker中集成，这就意味着我们不需要做额外的事情就可以运行它，无需编译任何Java源代码。
 ```
[plain] view plain copy
# Run the WordCount demo application.  There won't be any STDOUT output.  
# You can safely ignore any WARN log messages.  
$ ./bin/kafka-run-class org.apache.kafka.streams.examples.wordcount.WordCountDemo  
```
注意，这里没有魔术式的部署，实际上，使用kafkaStreams库中的任何应用程序，就像启动任何普通的Java应用程序，该脚本kafka-run-class也只是一个简单的java -cp命令的包装。   
该WordCount示例程序将从输入topic中读取数据，然后计算wordCount，将计算结果不断进行输出。演示将运行几秒钟，然后和其他典型流处理应用程序不同的是，它将会自动终止。

## 1.6.  检查输出结果
在本章节中，我们将使用内置的命令行工具从kafka中手工读取数据。在实际使用中，你可以通过其他方式，通过Kakfa客户端从Kafka中读取数据。比如，如果你可以在自己的应用程序中使用Kafka客户端将数据从Kakfa中迁移到其它数据系统。    
现在，我们可以从kafka输出topic中读取数据并检查wordcount实例运行结果。
```
[plain] view plain copy
./bin/kafka-console-consumer --zookeeper slave01:2181 \  
          --topic streams-wordcount \  
          --from-beginning \  
          --formatter kafka.tools.DefaultMessageFormatter \  
          --property print.key=true\  
          --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \  
          --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer  

 kafka-console-consumer.sh --zookeeper slave01:2181 --topic streams-wordcount --property print.key=true --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer   --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer  
```
Wordcount的数据将会被打印在如下的控制台中：
```
[plain] view plain copy
all     1  
streams 1  
lead    1  
to      1  
kafka   1  
hello   1  
kafka   2  
streams 2  
join    1  
kafka   3  
summit  1  
```
这里，第一列是Kafka消息的key的字符串格式，第二列是消息的值，long类型。你可以通过Ctrl+c命令来终止控制台输出。
但是等一下，输出看起来是不是很奇怪？为什么会出现重复的条目？比如streams出现了两次：
```
[plain] view plain copy
# Why not this, you may ask?  
all     1  
lead    1  
to      1  
hello   1  
streams 2  
join    1  
kafka   3  
summit  1  
```
对于上面的输出的解释是，wordCount应用程序的输出实际上是持续更新的流，其中每行记录是一个单一的word(即Message Key，比如Kafka)的计数。对于同一个Key的多个记录，每个记录之后是前一个的更新。
 

当第二个文本航的hello kafkastreams被处理的时候，我们观察到，相对第一次，已经存在的条目KTable被更新了(Kafak和Streams这两个单词). 修改后的记录被在此发送到了KStream。
这就解释了上述KStream第二列中显示的信息，为什么输出的topic上显示的内容，因为它是包含了变化的完整内容
```
[plain] view plain copy
all     1  
streams 1  
lead    1  
to      1  
kafka   1  
hello   1  
kafka   2  
streams 2  
join    1  
kafka   3  
summit  1  
```
>更多参看官网:http://kafka.apache.org/documentation/streams/
