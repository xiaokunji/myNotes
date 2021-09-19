

[toc]

# 1. 集群规划

集群中有两个主要角色，Master Node和Data Node，其它如Tribe Node等节点可根据业务需要另行设立

所以区分master和data节点,职责单一化(通过配置即可完成)

> Master Node，整个集群的管理者，负有对index的管理、shards的分配，以及整个集群拓扑信息的管理等功能，众所周知，Master Node可以通过Data Node兼任
>
> Data Node是数据的承载者，对索引的数据存储、查询、聚合等操作提供支持，这些操作严重消耗系统的CPU、内存、IO等资源

# 2. 分片指定

因为es有分片的概念,即数据会分布到不同的分片上,存储到分片上时是根据id分配的,若我们能自定义这个存放规则,取值时就能精确查找了(默认情况下会全部遍历)(有点像自定义HBASE Rowkey)

```htt
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