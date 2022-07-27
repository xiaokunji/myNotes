

[toc]

# 1. 集群规划

集群中有两个主要角色，Master Node和Data Node，其它如Tribe Node等节点可根据业务需要另行设立

所以区分master和data节点,职责单一化(通过配置即可完成)

> Master Node，整个集群的管理者，负有对index的管理、shards的分配，以及整个集群拓扑信息的管理等功能，众所周知，Master Node可以通过Data Node兼任
>
> Data Node是数据的承载者，对索引的数据存储、查询、聚合等操作提供支持，这些操作严重消耗系统的CPU、内存、IO等资源



更多集群知识见  [集群模式.md](集群模式.md) 

# 2. 分片指定

因为es有分片的概念,即数据会分布到不同的分片上,存储到分片上时是根据id分配的,若我们能自定义这个存放规则,取值时就能精确查找了(默认情况下会全部遍历)(有点像自定义HBASE Rowkey)

```http
例如: 
PUT my_index/my_type/1?routing=user1
{
  "title": "This is a document"
}

GET my_index/my_type/1?routing=user1
```



在自定义routing的情况下,难免会出现量大的index:

一种解决办法是单独为这些数据量大的渠道创建独立的index，

另一种办法是指定index参数index.routing_partition_size，来解决最终可能产生群集不均衡的问题

> 索引中的mappings必须指定_routing为"required": true，另外mappings不支持parent-child父子关系。



# 3. 索引拆分

当数据越来越多,则分片上的数据也越来越多,查询和存储的速度也越来越慢，更重要的是一个index其实是有存储上限的，如官方声明单个shard的文档数不能超过20亿, 可以借助 Rollover Api 和 Index Template,以时间维度拆分

> 大致如下: 存储数据的时候,存到具有类似名称的索引上,这些索引类属于同一个别名,则查询的时间可以根据这个规则范围查询
>
> 例如:  logs-2020.04.12 , logs-2020.05.12 都类比于 logs-*
>
> 那如果你想查询3天内的数据可以通过日期规则来匹配索引名，
>
> `GET /<logs-{now/d}-*>,<logs-{now/d-1d}-*>,<logs-{now/d-2d}-*>/_search`



# 4. Hot-Warm架构

冷热架构，为了保证大规模时序索引实时数据分析的时效性，可以根据资源配置不同将Data Nodes进行分类形成分层或分组架构，一部分支持新数据的读写，另一部分仅支持历史数据的存储，存放一些查询发生机率较低的数据，即Hot-Warm架构

思路: 

1. 将Data Node根据不同的资源配比打上标签，如：host、warm
2. 定义2个时序索引的index template，包括hot template和warm template，hot template可以多分配一些shard和拥有更好资源的Hot Node
3. 用hot template创建一个active index名为active-logs-1，别名active-logs，支持索引切割，插入一定数据后，通过roller over api将active-logs切割，并将切割前的index移动到warm nodes上，如active-logs-1，并阻止写入
4. 通过Shrinking API收缩索引active-logs-1为inactive-logs-1，原shard为5，适当收缩到2或3，可以在warm template中指定，减少检索的shard，使查询更快
5. 通过force-merging api合并inactive-logs-1索引每个shard的segment，节省存储空间
6. 删除active-logs-1



> 来自 : https://www.cnblogs.com/xguo/p/10558828.html?utm_source=tuicool&utm_medium=referral





# 5. 集群配置优化

## jvm配置 

- xms和xmx设置成一样，避免heap resize时卡顿

- xmx不要超过物理内存的50%

  因为es的lucene写入强依赖于操作系统缓存，需要预留加多的空间给操作系统

- 最大内存不超过32G，但是也不要太小

  堆太小会导致频繁的小延迟峰值，并因不断的垃圾收集暂停而降低吞吐量；

  如果堆太大，应用程序将容易出现来自全堆垃圾回收的罕见长延迟峰值；

  将堆限制为略小于32GB可以使用jvm的指针压缩技术增强性能；

- jvm使用server模式

- 推荐采用g1垃圾回收器

- 关闭jvm swapping



## 集群节点数

es的节点提供查询的时候使用较多的内存来存储查询缓存，es的lucene写入到磁盘也会先缓存在内存中，我们开启设计这个es节点时需要根据每个节点的存储数据量来进行判断。这里有一个流行的推荐比例配置：

- 搜索类比例：1:16(内存:磁盘)
- 日志类比例：1:48  ~ 1:96(内存:磁盘)



示例：

有一个业务的数据量预估实际有1T，我们把副本设置1个，那么es中总数据量为2T。

- 如果业务偏向于搜索

  每个节点31*16=496G。在加上其它的预留空间，每个节点有400G的存储空间。2T/400G，则需要5个es存储节点。

- 如果业务偏向于写入日志型

  每个节点31*50=1550G，就只需要2个节点即可

这里31G表示的是jvm设置不超过32g否则不会使用java的指针压缩优化了。



# 6. 写入和查询优化

## 写入优化

写入的目标在于增大写入的吞吐量，这里主要从两个方面进行优化：

**客户端**：

- 写入数据不指定_id，让ES自动产生

  > 当用户显示指定id写入数据时，ES会先发起查询来确定index中是否已经有相同id的doc存在，若有则先删除原有doc再写入新doc。
  >
  > 这样每次写入时，ES都会耗费一定的资源做查询。
  >
  > 如果用户写入数据时不指定doc，ES则通过内部算法产生一个随机的id，并且保证id的唯一性，这样就可以跳过前面查询id的步骤，提高写入效率。

- 进行多线程写入，最好的情况时动态调整，如果http429，此时可以少写入点，不是可以多写点



**server**：

- 可靠性要求不高时，可以副本设置为0。

- 减少不必要的分词，从而降低cpu和磁盘的开销。

- 不要对字符串使用默认的dynmic mapping。会自动分词产生不必要的开销。

- 设置30s refresh，降低lucene生成频次，资源占用降低提升写入性能，但是损耗实时性。

- translong落盘异步化，提升性能，损耗灾备能力。

- merge并发控制。

  > ES的一个index由多个shard组成，而一个shard其实就是一个Lucene的index，它又由多个segment组成，且Lucene会不断地把一些小的segment合并成一个大的segment，这个过程被称为merge。

这里可以针对myindex索引优化的示例：

```http
PUT myindex {
    "settings": {
        "index" :{
            "refresh_interval" : "30s","number_of_shards" :"2"
        },
        "routing": {
            "allocation": {
                "total_shards_per_node" :"3"
            }
        },
        "translog" :{
            "sync_interval" : "30s",
            "durability" : "async"
        },
        number_of_replicas" : 0
    }
    "mappings": {
        "dynamic" : false,
        "properties" :{}
    }
}
```



## 查询优化



首先有几个原则我们需要清楚：

- ElasticSearch不是关系型数据库，即使ElasticSearch支持嵌套、父子查询，但是会严重损耗ElasticSearch的性能，速度也很慢。

- 尽量先将数据计算出来放到索引字段中，不要查询的时候再通过es的脚本来进行计算。

- 尽量利用filter的缓存来查询

- 设计上不要深度分页查询，否则可能会使得jvm内存爆满。

- 可以通过profile、explain工具来分析慢查询的原因。

- 严禁*号通配符为开头的关键字查询，我们可以利用不同的分词器进行模糊查询。

- 分片数优化，避免每次查询访问每一个分片，可以借助路由字段进行查询。

- 需要控制单个分片的大小：

  > 这个上面有提到：查询类：20GB以内；日志类：50G以内。

- 读但是不写入文档的索引进行lucene段进行强制合并。

- 优化数据模型、数据规模、查询语句。

- 尽量少对text类型字段做聚合操作, 因为效果不佳,性能低下



# 7. 问题诊断

## 监控状态

- 绿色代表集群的索引的所有分片（主分片和副本分片）正常分配了。
- 红色代表至少一个主分片没有分配。
- 黄色代表至少一个副本没有分配。



## 慢查询

### profile api

在查询条件中设置profile为true的参数，将会显示查询经历的细节。

```http
GET trace_segment_record_202204291430/_search
{
  "query": {
     "match": {
       "serviceIp" : "xxxxx"
     }
  },
  "profile": true
}
```

其结果

![image-20220727104009422](D:\格力资料\个人\笔记\myNotes\综合\ELK\ElasticSearch\es优化.assets\image-20220727104009422.png)

这里会返回一个shards列表。其中：

- id

  【nodeId】【shardId】

- query

  主要包含了如下信息：

  - query_type

    展示了哪种类型的查询被触发。

  - lucene

    显示启动的lucene方法

  - time

    执行lucene查询小号的时间

  - breakdown

    里面包含了lucene查询的一些细节参数

  - rewrite_time

    多个关键字会分解创建个别查询，这个分解过程花费的时间。将重写一个或者多个组合查询的时间被称为”重写时间“

  - collector

    在Lucene中，收集器负责收集原始结果，并对它们进行组合、过滤、排序等处理。这里我们可以根据这个查看收集器里面花费的时间及一些参数。



Profile API让我们清楚地看到查询耗时。提供了有关子查询的详细信息，我们可以清楚地知道在哪个环节查询慢，另外返回的结果中，关于Lucene的详细信息也让我们深入了解到ES是如何执行查询的。



## 节点cpu过高

如果出现节点占用CPU很高，我们需要知道CPU在运行什么任务，一般通过线程堆栈来查看。

这里有两种方式可以查看哪些线程CPU占用率比较高：

1. 使用ElasticSearch提供的hot_threads api查看；
2. 使用jstack和top命令查看，针对java应用cpu过高问题的这个是通用做法。

这里推荐使用hot_threads api

```http
GET /_nodes/hot_threads
GET /_nodes/<node_id>/hot_threads
```

更多详见:  [深入解读 Elasticsearch 热点线程 hot_threads_铭毅天下的博客-CSDN博客_es hot_thread](https://blog.csdn.net/laoyang360/article/details/109567666)





[ElasticSearch部署架构和容量规划 (qq.com)](https://mp.weixin.qq.com/s?__biz=MzIzNzgyMjYxOQ==&mid=2247490210&idx=1&sn=b6581976f56c577b51ad9f667f97df58)



