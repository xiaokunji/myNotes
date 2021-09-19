[toc]

>注:**kafka依赖zookeeper,所以启动kafka前需开启zookeeper,kafka依赖zookeeper来分发消息,并会在zookeeper中存储分区和broker的信息**


# 1、启动服务

#从后台启动Kafka集群（3台都需要启动）   
`cd /mysoftware/kafka_2.11-0.10.1.0//bin`   
#进入到kafka的bin目录    
`./kafka-server-start.sh -daemon ../config/server.properties`

(每台都)启动:    
`kafka-server-start.sh config/server.properties >/dev/null & 
停止: kafka-server-stop.sh   >/dev/null &  `    
如果启动报错,必须先停止,才能启动


启动后会有kafka进程

# 2. 创建Topic
`./kafka-topics.sh --create --zookeeper slave02:2181 --replication-factor 2 --partitions 1 --topic mystream`

> 解释
>- --replication-factor 2   #复制两份
>- --partitions 1 #创建1个分区
>- --topic #主题为shuaige

# 3. 在一台服务器上创建一个发布者(相当于生产者)
#创建一个broker，发布者   
`./kafka-console-producer.sh --broker-list master:9092 --topic shuaige`

# 4 在一台服务器上创建一个订阅者(相当于消费者)
	>--bootstrap-server  新的kafka,可以用这个参数取代--zookeeper   端口号要变成9092
- old: `kafka-console-consumer.sh --zookeeper localhost:2181 --topic shuaige --from-beginning`   
- new: `kafka-console-consumer.sh --bootstrap-server master:9092 --topic shuaige --from-beginning`

# 5 、查看topic

`./kafka-topics.sh --list --zookeeper localhost:2181`

# 6 、查看topic状态

`./kafka-topics.sh --describe --zookeeper localhost:2181 --topic shuaige`
> #下面是显示信息
Topic:ssports    PartitionCount:1    ReplicationFactor:2    Configs:
    Topic: shuaige    Partition: 0    Leader: 1    Replicas: 0,1    Isr: 1   
#分区为为1  复制因子为2   他的  shuaige的分区为0 
#Replicas: 0,1   复制的为0，1

