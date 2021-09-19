# hive on spark/yarn使用与区别

**使用**:

默认是使用yarn,通过设置 配置文件或者启动时设置可以指定spark, 但使用spark计算引擎很麻烦,要对应版本的spark,且不含hive jar包

> 由于官方的spark都自带hive jar包,所以需要自己编码
>
> <font style='color:red'>是不是cdh不需要?待检验</font>
>
> https://www.jianshu.com/p/339da2b6d480

**区别:**

其本质是替换了计算引擎,一个基于MapReduce,一个基于spark,于此他们计算/资源等等方式都有所不同

# 小文件的场景,危害,怎么处理

**场景**:

1. 接收的就是小文件,需要自行处理
2. 自己的代码造成了很多小文件

**危害**:

占用过多空间(一个块存不满也占用那么多)

文件越多,map也多,划不来

**处理**:

**总得来说合并小文件**

1. 通过参数方式,再写入一次做合并

2. 存储时指定可以压缩的文本格式(sequenceFile),还可以指定压缩格式

3. 对于不常用的数据,可以使用hadoop的archive归档

   > 详情见hive的<<小文件>>



# Hive中order by，sort by，distribute by，cluster by的区别

**一：order by**

order by会对输入做全局排序，因此只有一个Reducer(多个Reducer无法保证全局有序)，然而只有一个Reducer，会导致当输入规模较大时，消耗较长的计算时间。关于order by的详细介绍请参考这篇文章：[Hive Order by操作](http://blog.csdn.net/lzm1340458776/article/details/43230517)。

**二：sort by**

sort by不是全局排序，其在数据进入reducer前完成排序，因此，如果用sort by进行排序，并且设置mapred.reduce.tasks>1，则sort by只会保证每个reducer的输出有序，并不保证全局有序。sort by不同于order by，它不受hive.mapred.mode属性的影响，sort by的数据只能保证在同一个reduce中的数据可以按指定字段排序。使用sort by你可以指定执行的reduce个数(通过set mapred.reduce.tasks=n来指定)，对输出的数据再执行归并排序，即可得到全部结果。

**三：distribute by**

distribute by是控制在map端如何拆分数据给reduce端的。hive会根据distribute by后面列，对应reduce的个数进行分发，默认是采用hash算法。sort by为每个reduce产生一个排序文件。在有些情况下，你需要控制某个特定行应该到哪个reducer，这通常是为了进行后续的聚集操作。distribute by刚好可以做这件事。因此，distribute by经常和sort by配合使用。

> 注：Distribute by和sort by的使用场景
>
> 1.Map输出的文件大小不均。
>
> 2.Reduce输出文件大小不均。
>
> 3.小文件过多。
>
> 4.文件超大。



**四：cluster by**

cluster by除了具有distribute by的功能外还兼具sort by的功能。但是排序只能是倒叙排序，不能指定排序规则为ASC或者DESC。