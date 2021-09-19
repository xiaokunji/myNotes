[toc]
# 1.少用count（distinct）
- count（distinct）是由一个reduce task来完成的，这一个reduce需要处理的数据量太大，就会导致整个job很难完成。
- count（distinct）可以使用先group by再count的方式来替换

# 2. 合理使用MapJoin

# 3. 合理使用动态分区
动态分区可以优化诸如需要往指定分区插入数据的这种操作。  
配置参数 

```
hive.exec.dynamic.partition：是否开启动态分区，默认为false，设置成true
hive.exec.dynamic.partition.mode：默认值表示必须指定至少一个静态分区，默认为strict，设置成nonstrict
hive.exec.max.dynamic.partitions.pernode：在每个执行MR的节点上，最大可以创建多少个动态分区，默认100，按实际情况来定
hive.exec.max.created.files：整个MR Job中，最大可以创建多少个HDFS文件，默认值：100000，一般默认值足够了，除非你的数据量非常大，需要创建的文件数大于100000，可根据实际情况加以调整。
hive.error.on.empty.partition：当有空分区生成时，是否抛出异常，默认值：false，一般不需要设置。
```

# 4. 可以替代 in/exists 语句
hive1.2.1 也支持 in/exists 操作，但还是推荐使用 hive 的一个高效替代方案：left semi join

`select a.id, a.name from a left semi join b on a.id = b.id;`

# 5. 合理设置map/reduce数量

# 6. 避免数据倾斜

> https://www.cnblogs.com/qingyunzong/p/8847775.html#_label4
https://www.cnblogs.com/duanxingxing/p/6874318.html
https://blog.csdn.net/qq_32038679/article/details/80557286


