[toc]
# 1. Mesos:
一个资源统一管理和调度平台,类似YARN,个人感觉不如YARN好   
出自:`http://dongxicheng.org/mapreduce-nextgen/mesos_vs_yarn/`

# 2. DGA:
在spark里每一个操作生成一个RDD，RDD之间连一条边，最后这些RDD和他们之间的边组成一个有向无环图，这个就是DAG。(一般来说,生成新的RDD后,旧的不会使用,会等着被JVM回收)
	
> 来自 `<http://blog.csdn.net/sinat_31726559/article/details/51738155> `

# 3. yarn和spark: 
spark可以运行在yarn上,做资源管理,但是spark有自己的资源管理平台(可以深究,TaskScheduler)
	
# 4. Spark core:
Spark Core实现了Spark的基本功能，包含任务调度、内存管理、错误恢复、与存储系统交互等模块。Spark Core中还包含了对弹性分布式数据集（resilient distributed dataset，简称RDD）的API定义。 Spark Core提供了创建和操作这些集合的多个API。
> 来自 `<https://www.douban.com/note/536766108/?from=tag> `
	
	
# 5. RDD类型
# 6. PageRank算法
# 7. Spark Shuffle
以reduceByKey为例,涉及按key对于RDD成员进行重组。将具有相同key但分布在不同节点上的成员聚会在一个节点上,以便对他们的value进行操作,这个重组过程就是shuffle操作。因为shuffle操作会涉及数据的传输，所以成本特别高，而且过程复杂。
> 简单的说就是需要操作所有数据时,整合数据的过程叫shuffle

# 8. RDD不可变,那RDD会不会存在回收?
RDD 使用的是java内存,每生成一个RDD,就生成了一个对象(即使不接收),当RDD不再使用时(可能是标记法吧),又JVM回收

# 9. 为什么Flink比spark快?

1.  Flink所用事物类型更底层,在运行时就经过了优化
2.  flink提供了基于每个事件的流式处理机制,而spark是用小批量来模拟流式，也就是多个事件的集合
> 以上只是简述,并非完全.来自: https://www.jianshu.com/p/905ca3a7edb9

# 10.checkpoint
checkpoint在spark中主要有两块应用：   
一块是在spark core中对RDD做checkpoint，可以切断做checkpoint RDD的依赖关系，将RDD数据保存到可靠存储（如HDFS）以便数据恢复；   
另外一块是应用在spark streaming中，使用checkpoint用来保存DStreamGraph以及相关配置信息，以便在Driver崩溃重启的时候能够接着之前进度继续进行处理（如之前waiting batch的job会在重启后继续处理）。
> 注: **checkpoint着重的是DAG图,会保存当前RDD,比如:RDD经过很多转换,最后才触发action;   
cache着重的是数据,比如:需要重复用到该数据**
> https://www.cnblogs.com/superhedantou/p/9004820.html
https://blog.csdn.net/j904538808/article/details/80104525

# 11.SparkSQL 和 impala
`SparkSQL`优势是能做业务处理,因为是RDD,能和其他RDD无缝连接,   
`impala`优势是查询速度够快,(感觉能用web系统的操作)

# 12.springStream中的 updatestateByKey函数和Mapwithstate函数
SparkStreaming 7*24 小时不间断的运行，有时需要管理一些状态，比如wordCount，每个batch的数据不是独立的而是需要累加的，这时就需要sparkStreaming来维护一些状态(这些状态可以是任意类型)，目前有两种方案updateStateByKey和mapWithState，(后者性能比前者好)   

**updateStateByKey**
```
    ssc.checkpoint(".") // 需打开checkpiont
    def updateFunction(currValues:Seq[Int],preValue:Option[Int]): Option[Int] = {
       val currValueSum = currValues.sum
        //上面的Int类型都可以用对象类型替换
        Some(currValueSum + preValue.getOrElse(0)) //当前值的和加上历史值
    }
    kafkaStream.map(r => (r._2,1)).updateStateByKey(updateFunction _)// vaule值是state(任意类型),更加key对state做处理(stream来维护状态)
```

**mapWithState**
```
可以使用initialState(RDD)来初始化key的值。   
另外，还可以指定timeout函数，该函数的作用是，如果一个key超过timeout设定的时间没有更新值，那么这个key将会失效。这个控制需要在func中实现，必须使用state.isTimingOut()来判断失效的key值。如果在失效时间之后，这个key又有新的值了，则会重新计算。如果没有使用isTimingOut，则会报错。

val initialRDD = ssc.sparkContext.parallelize(List[(String, Int)]())
words.map((_, 1)).mapWithState(StateSpec.function(func).timeout(Seconds(30)).initialState(initialRDD)).print()

/**
   * 定义一个函数，该函数有三个类型word: String, option: Option[Int], state: State[Int]
   * 其中word代表统计的单词，
   * option代表的是历史数据（使用option是因为历史数据可能有，也可能没有，如第一次进来的数据就没有历史记录），
   * state代表的是返回的状态
   */
  val func: (String, Option[Int], State[Int]) => Any = (word: String, option: Option[Int], state: State[Int]) => {
    if(state.isTimingOut()){
      //如果key超过时间没有更新
      println(word + "is timeout")
    }else{
      val sum = option.getOrElse(0) + state.getOption().getOrElse(0)
      // 单词和该单词出现的频率/ 获取历史数据，当前值加上上一个批次的该状态的值
      // 更新状态
      state.update(sum)
      (word, sum)
    }
  }

```
> 链接：https://www.jianshu.com/p/9f743301f589
https://my.oschina.net/u/3875806/blog/2986549
http://spark.apache.org/docs/2.2.1/streaming-programming-guide.html
