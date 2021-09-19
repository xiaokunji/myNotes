[toc]

# 1. 前言

 MySQL的二进制日志可以说是MySQL最重要的日志了，它记录了所有的DDL和DML(除了数据查询语句)语句，以事件形式记录，还包含语句所执行的消耗的时间，MySQL的二进制日志是事务安全型的。一般来说开启二进制日志大概会有1%的性能损耗(参见MySQL官方中文手册 5.1.24版)。二进制有两个最重要的使用场景: 
   其一：MySQL Replication在Master端开启binlog，Mster把它的二进制日志传递给slaves来达到master-slave数据一致的目的。
   其二：自然就是数据恢复了，通过使用mysqlbinlog工具来使恢复数据。



二进制日志包括两类文件：

- 二进制日志索引文件（文件名后缀为.index）用于记录所有的二进制文件，

- 二进制日志文件（文件名后缀为.00000*）记录数据库所有的DDL和DML(除了数据查询语句)语句事件。

binlog是一个二进制文件集合，每个binlog文件以一个4字节的魔数开头，接着是一组Events:

魔数：0xfe62696e对应的是0xfebin；

Event：每个Event包含header和data两个部分；header提供了Event的创建时间，哪个服务器等信息，data部分提供的是针对该Event的具体信息，如具体数据的修改；

第一个Event用于描述binlog文件的格式版本，这个格式就是event写入binlog文件的格式；

其余的Event按照第一个Event的格式版本写入；

最后一个Event用于说明下一个binlog文件；

binlog的索引文件是一个文本文件，其中内容为当前的binlog文件列表

当遇到以下3种情况时，MySQL会重新生成一个新的日志文件，文件序号递增：

- MySQL服务器停止或重启时

- 使用 flush logs 命令；

- 当 binlog 文件大小超过 max_binlog_size 变量的值时；

`max_binlog_size` 的最小值是4096字节，最大值和默认值是 1GB (1073741824字节)。事务被写入到binlog的一个块中，所以它不会在几个二进制日志之间被拆分。

因此，如果你有很大的事务，为了保证事务的完整性，不可能做切换日志的动作，只能将该事务的日志都记录到当前日志文件中，直到事务结束，你可能会看到binlog文件大于 `max_binlog_size` 的情况。



**写 Binlog 的时机**

对支持事务的引擎如**InnoDB而言，是在prepare完commit前写的**。binlog 什么时候刷新到磁盘跟参数 `sync_binlog` 相关。

如果设置为0，则表示MySQL不控制binlog的刷新，由文件系统去控制它缓存的刷新；

如果设置为不为0的值，则表示每 sync_binlog 次事务，MySQL调用文件系统的刷新操作刷新binlog到磁盘中。

设为1是最安全的，在系统故障时最多丢失一个事务的更新，但是会对性能有所影响。

如果 sync_binlog=0 或 sync_binlog大于1，当发生电源故障或操作系统崩溃时，可能有一部分已提交但其binlog未被同步到磁盘的事务会被丢失，恢复程序将无法恢复这部分事务。

在MySQL 5.7.7之前，默认值 sync_binlog 是0，<u>MySQL 5.7.7和更高版本使用默认值1</u>，这是最安全的选择。一般情况下会设置为100或者0，牺牲一定的一致性来获取更好的性能。

> 这涉及几个知识点
>
> - redolog
>
> - 二段式提交
>
>   
>
>   当我们更新数据时，两阶段提交的具体流程：
>
>   - 更新操作先写入redolog，这时候这条log的状态是prepared状态
>   - 再将逻辑日志写入binlog
>   - 最后在binlog写好之后，把redolog里的这条日志的状态改为commit
>
> **redolog和undolog**：innodb事务日志包括redo log和undo log。redo log是重做日志，提供前滚操作，undo log是回滚日志，提供回滚操作。详见：https://www.cnblogs.com/f-ck-need-u/p/9010872.html

# 2. 三种模式

在 `MySQL 5.7.7` 之前，默认的格式是 `STATEMENT`，在 `MySQL 5.7.7` 及更高版本中，默认值是 `ROW`

## 2.1 row level (默认)

记录的方式是行，即如果批量修改数据，记录的不是批量修改的SQL语句事件，而是每条记录被更改的SQL语句，因此，ROW模式的binlog日志文件会变得很“重”。

![image](https://images2018.cnblogs.com/blog/163758/201809/163758-20180905223630873-1667858615.png)

**优点**：row level的binlog日志内容会非常清楚的记录下每一行数据被修改的细节。而且不会出现某些特定情况下存储过程或function，以及trigger的调用和触发器无法被正确复制的问题。

**缺点**：row level下，所有执行的语句当记录到日志中的时候，都以每行记录的修改来记录，这样可能会产生大量的日志内容，产生的binlog日志量是惊人的。批量修改几百万条数据，那么记录几百万行……

> 存的是具体的修改语句

## 2.2 Statement level

记录每一条修改数据的SQL语句（批量修改时，记录的不是单条SQL语句，而是批量修改的SQL语句事件）。看上面的图解可以很好的理解row level和statement level两种模式的区别。

> 存的是sql 语句

**优点**：statement模式记录的更改的SQ语句事件，并非每条更改记录，所以大大减少了binlog日志量，节约磁盘IO，提高性能。

**缺点**：statement level下对一些特殊功能的复制效果不是很好，比如：函数、存储过程的复制。由于row level是基于每一行的变化来记录的，所以不会出现类似问题



>  行模式和语句模式的区别
>
> 1. 语句模式：
>
>    ​	100万条记录,	只需1条delete * from test；就可以删除100万条记录
>
> 2. row模式
>
>    100万条记录 , 记录100万条删除命令

## 2.3 Mixed

实际上就是前两种模式的结合。在Mixed模式下，MySQL会根据执行的每一条具体的sql语句来区分对待记录的日志形式，也就是在Statement和Row之间选择一种。

> 为什么不推荐使用`mixed`模式，理由如下
>
> 假设master有两条记录，而slave只有一条记录。
>
> master的数据为
>
> ```
> +----+------------------------------------------------------+
> | id | n |
> +----+------------------------------------------------------+
> | 1 | d24c2c7e-430b-11e7-bf1b-00155d016710 |
> | 2 | ddd |
> +----+------------------------------------------------------+
> ```
>
> slave的数据为
>
> ```
> +----+-------------------------------------------------------+
> | id | n |
> +----+-------------------------------------------------------+
> | 1 | d24c2c7e-430b-11e7-bf1b-00155d016710 |
> +----+-------------------------------------------------------+
> ```
>
> 当在`master`上更新一条从库不存在的记录时，也就是`id=2`的记录，你会发现`master`是可以执行成功的。而`slave`拿到这个SQL后，也会照常执行，不报任何异常，只是更新操作不影响行数而已。并且你执行命令`show slave status`，查看输出，你会发现没有异常。但是，如果你是`row`模式，由于这行根本不存在，是会报1062错误的。



# 3 查看与配置



配置:

```properties
[mysqld]
#设置日志路径，注意路经需要mysql用户有权限写,这里可以写绝对路径,也可以直接写mysql-bin(后者默认就是在/var/lib/mysql目录下)
log-bin = /data/mysql/logs/mysql-bin.log
#配置serverid
server-id=1
#设置日志三种格式：STATEMENT、ROW、MIXED 。
binlog_format = mixed
#设置binlog清理时间
expire_logs_days = 7
#binlog每个日志文件大小
max_binlog_size = 100m
#binlog缓存大小
binlog_cache_size = 4m
#最大binlog缓存大小
max_binlog_cache_size = 512m
// 配置前两个亦可
```





查看biglog是否开启

`SHOW VARIABLES LIKE 'log_bin';`

查看binlog模式

`show global variables like "binlog%";`

```mysql
show binlog events;   #只查看第一个binlog文件的内容
show binlog events in 'mysql-bin.000002';#查看指定binlog文件的内容
show binary logs;  #获取binlog文件列表
show master status； #查看当前正在写入的binlog文件
show  variables like '%binlog_format%'; # 查看日志格式

```

**mysqlbinlog工具**

`/usr/local/mysql/bin/mysqlbinlog --start-datetime="2013-03-01 00:00:00" --stop-datetime="2014-03-21 23:59:59" /usr/local/mysql/var/mysql-bin.000007 -r test2.sql`

> -r  输出到文件
>
> -v / -vv  查看binlog是row格式  -vv 展示更详细的sql信息
>
> -d 指定库名
>
> -h 指定ip
>
> -P 端口
>
> -u 指定用户名
>
> -p 指定密码
>
> --server-id= 指定sever-id
>
> 当前查询binlog时一个事务日志被截断了,使用 `--include-gtids`  指定事务标识
>
> gtid（Global Transaction ID） :  全局事务id; 组成:  `server_uuid：transaction_id`



输出案例

```log
/*!50530 SET @@SESSION.PSEUDO_SLAVE_MODE=1*/;
/*!50003 SET @OLD_COMPLETION_TYPE=@@COMPLETION_TYPE,COMPLETION_TYPE=0*/;
DELIMITER /*!*/;
# at 4
#190308 10:05:03 server id 1  end_log_pos 123 CRC32 0xff02e23d     Start: binlog v 4, server v 5.7.22-log created 190308 10:05:03
# Warning: this binlog is either in use or was not closed properly.
# at 123
#190308 10:05:03 server id 1  end_log_pos 154 CRC32 0xb81da4c5     Previous-GTIDs
# [empty]
# at 154
#190308 10:05:09 server id 1  end_log_pos 219 CRC32 0xfb30d42c     Anonymous_GTID  last_committed=0    sequence_number=1   rbr_only=yes
/*!50718 SET TRANSACTION ISOLATION LEVEL READ COMMITTED*//*!*/;
SET @@SESSION.GTID_NEXT= 'ANONYMOUS'/*!*/;
# at 219
...
...
# at 21019
#190308 10:10:09 server id 1  end_log_pos 21094 CRC32 0x7a405abc     Query   thread_id=113   exec_time=0 error_code=0
SET TIMESTAMP=1552011009/*!*/;
BEGIN
/*!*/;
# at 21094
#190308 10:10:09 server id 1  end_log_pos 21161 CRC32 0xdb7a2b35     Table_map: `maxwell`.`positions` mapped to number 110
# at 21161
#190308 10:10:09 server id 1  end_log_pos 21275 CRC32 0xec3be372     Update_rows: table id 110 flags: STMT_END_F
### UPDATE `maxwell`.`positions`
### WHERE
###   @1=1
###   @2='master.000003'
###   @3=20262
###   @4=NULL
###   @5='maxwell'
###   @6=NULL
###   @7=1552011005707
### SET
###   @1=1
###   @2='master.000003'
###   @3=20923
###   @4=NULL
###   @5='maxwell'
###   @6=NULL
###   @7=1552011009790
# at 21275
#190308 10:10:09 server id 1  end_log_pos 21306 CRC32 0xe6c4346d     Xid = 13088
COMMIT/*!*/;
SET @@SESSION.GTID_NEXT= 'AUTOMATIC' /* added by mysqlbinlog */ /*!*/;
DELIMITER ;
# End of log file
/*!50003 SET COMPLETION_TYPE=@OLD_COMPLETION_TYPE*/;
/*!50530 SET @@SESSION.PSEUDO_SLAVE_MODE=0*/;
```

比较关注的就是里面的修改类语句

截取其中的一段进行分析：

```log
# at 21019
#190308 10:10:09 server id 1  end_log_pos 21094 CRC32 0x7a405abc     Query   thread_id=113   exec_time=0 error_code=0
SET TIMESTAMP=1552011009/*!*/;
BEGIN
/*!*/;
```

上面输出包括信息：

- position: 位于文件中的位置，即第一行的（# at 21019）,说明该事件记录从文件第21019个字节开始

- timestamp: 事件发生的时间戳，即第二行的（#190308 10:10:09）

- server id: 服务器标识（1）

- end_log_pos 表示下一个事件开始的位置（即当前事件的结束位置+1）

- thread_id: 执行该事件的线程id （thread_id=113）

- exec_time: 事件执行的花费时间

- error_code: 错误码，0意味着没有发生错误

- type:事件类型Query

  
> 一些常见的事件类型
> 
> | 事件类型          | 说明                                                         |
> | ----------------- | ------------------------------------------------------------ |
> | QUERY_EVENT       | 执行更新语句时会生成此事件，包括：create，insert，update，delete； |
> | STOP_EVENT        | 当mysqld停止时生成此事件                                     |
> | ROTATE_EVENT      | 当mysqld切换到新的binlog文件生成此事件，切换到新的binlog文件可以通过执行flush logs命令或者binlog文件大于 `max_binlog_size` 参数配置的大小； |
> | INTVAR_EVENT      | 当sql语句中使用了AUTO_INCREMENT的字段或者LAST_INSERT_ID()函数；此事件没有被用在binlog_format为ROW模式的情况下 |
> | XID_EVENT         | 支持XA的存储引擎才有，本地测试的数据库存储引擎是innodb，所有上面出现了XID_EVENT；innodb事务提交产生了QUERY_EVENT的BEGIN声明，QUERY_EVENT以及COMMIT声明，如果是myIsam存储引擎也会有BEGIN和COMMIT声明，只是COMMIT类型不是XID_EVENT |
> | TABLE_MAP_EVENT   | 用在binlog_format为ROW模式下，将表的定义映射到一个数字，在行操作事件之前记录（包括：WRITE_ROWS_EVENT，UPDATE_ROWS_EVENT，DELETE_ROWS_EVENT） |
> | WRITE_ROWS_EVENT  | 用在binlog_format为ROW模式下，对应 insert 操作               |
> | UPDATE_ROWS_EVENT | 用在binlog_format为ROW模式下，对应 update 操作               |
> | DELETE_ROWS_EVENT | 用在binlog_format为ROW模式下，对应 delete 操作               |










参考链接

[binlog配置](https://blog.csdn.net/weixin_43944305/article/details/108620849)

[binlog三种模式-博客园](https://www.cnblogs.com/rinack/p/9595370.html)

[查询binlog日志](https://www.cnblogs.com/softidea/p/12624778.html)

[mysql-binlog](https://blog.csdn.net/chushoufengli/article/details/106748672)

[binlog和redolog](https://www.jianshu.com/p/ac7bcfc656eb)



