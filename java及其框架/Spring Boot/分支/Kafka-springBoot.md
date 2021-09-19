> kafka 的 server.properties 中加入 `host.name=10.1.21.37`, 不然springBoot连不上kafka

1.pom.xml
```
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

2.application.properties
```
#============== kafka ===================
# 指定kafka 代理地址，可以多个
spring.kafka.bootstrap-servers=10.1.21.37:9092

#=============== provider  =======================

spring.kafka.producer.retries=2
# 每次批量发送消息的数量
spring.kafka.producer.batch-size=16384
spring.kafka.producer.buffer-memory=33554432
# 指定消息key和消息体的编解码方式
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer

#=============== consumer  =======================
# 指定默认消费者group id
spring.kafka.consumer.group-id=test

spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.enable-auto-commit=true
spring.kafka.consumer.auto-commit-interval=100


# 指定消息key和消息体的编解码方式
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer



topic=msg.topic

```

3. 代码
```

@Autowired
private KafkaTemplate<String, Object> kafka;

// 生产者
@RequestMapping("/sendMsg")
public LocalDateTime sendMsg(String helpNo) throws Exception {
	HelpInfo helpInfo=new HelpInfo();
	LocalDateTime now = LocalDateTime.now();
	helpInfo.setContent("content");
	helpInfo.setHelpNo(helpNo);
	helpInfo.setTitle("title");
	helpInfo.setUpdateName(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
	// 指定主题, 计算分区, json类型的内容
	SendResult<String,Object> sendResult = kafka.send(topic,Math.abs(helpNo.hashCode())%3,helpInfo.getHelpNo(), JSON.toJSONString(helpInfo)).get();
	long hasOffset = sendResult.getRecordMetadata().offset();// 得到偏移量  , 捷顺是通过偏移量不等于-1来判断是否发送成功
	System.out.println("生产数据偏移量:"+hasOffset);
	System.out.println(sendResult);
	return now;
}
	
// 消费这个主题下所有分区数据
@KafkaListener(topics= "${topic}" )
public Object consumeMsg(String msg) {
    // msg就是生产者的数据
	System.out.println("消费的数据:"+msg);
	return msg;
	
}
	
// 在消费群组topicPartition 下 消费 ${topic} 主题的 2,0 分区,其中0分区从偏移量为2开始消费
@KafkaListener(id="topicPartition",topicPartitions= {
                                 @TopicPartition(topic = "${topic}", // 指定主题
                                 partitions= {"2"}, // 指定分区消费
                                 partitionOffsets= @PartitionOffset(initialOffset = "2", partition = "0"))})// 指定分区并指定偏移量
public Object consumeMsgOfPart(String msg) {
    System.out.println("指定消费的数据:"+msg);
	// 一般情况会直接消费整个主题,毕竟分区是用来加快速度的,配上批量消费(得自己写消费工厂),简直棒棒哒
	return msg;

// 用ConsumerRecord接收,能接收都更多数据,比如分区,偏移量等等
@KafkaListener(id="consumeMsgGetMorn",topics="${topic}")
	public Object consumeMultMsg(ConsumerRecord<String, Object> record) {
		System.out.println("消费的数据,更多返回值:"+record);

		return record;
	}
	

```
// 使用Ack机制确认消费
//使用Kafka的Ack机制比较简单，只需简单的三步即可：

1.  设置ENABLE_AUTO_COMMIT_CONFIG=false，禁止自动提交
2.  设置AckMode=MANUAL_IMMEDIATE
3.  监听方法加入Acknowledgment ack 参数

```
配置文件加入如下:
spring.kafka.consumer.enable-auto-commit=false
spring.kafka.listener.ack-mode=MANUAL_IMMEDIATE


@KafkaListener(id="consumeMsgGetMorn",topics="${topic}")
	public Object consumeMultMsg(ConsumerRecord<String, Object> record, Acknowledgment ack) {
		System.out.println("ack消费的数据值:"+record);
		// 如果不手动确认,则偏移量会不改变
		ack.acknowledge();
		
		return record;
	}
```

来自: https://www.jianshu.com/p/a64defb44a23