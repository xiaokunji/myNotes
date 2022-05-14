

# PostgreSQL与MySQL 的区别



| 特性                   | MySQL                                                        | PostgreSQL                                                   |
| ---------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 实例                   | 通过执行 MySQL 命令（mysqld）启动实例。一个实例可以管理一个或多个数据库。一台服务器可以运行多个 mysqld 实例。一个实例管理器可以监视 mysqld 的各个实例。 | 通过执行 Postmaster 进程（pg_ctl）启动实例。一个实例可以管理一个或多个数据库，这些数据库组成一个集群。集群是磁盘上的一个区域，这个区域在安装时初始化并由一个目录组成，所有数据都存储在这个目录中。使用 initdb 创建第一个数据库。一台机器上可以启动多个实例。 |
| 数据库                 | 数据库是命名的对象集合，是与实例中的其他数据库分离的实体。一个 MySQL 实例中的所有数据库共享同一个系统编目。 | 数据库是命名的对象集合，每个数据库是与其他数据库分离的实体。每个数据库有自己的系统编目，但是所有数据库共享 pg_databases。 |
| 数据缓冲区             | 通过 **innodb_buffer_pool_size** 配置参数设置数据缓冲区。这个参数是内存缓冲区的字节数，InnoDB 使用这个缓冲区来缓存表的数据和索引。在专用的数据库服务器上，这个参数最高可以设置为机器物理内存量的 80%。 | **Shared_buffers** 缓存。在默认情况下分配 64 个缓冲区。默认的块大小是 8K。可以通过设置 postgresql.conf 文件中的 shared_buffers 参数来更新缓冲区缓存。 |
| 数据库连接             | 客户机使用 CONNECT 或 USE 语句连接数据库，这时要指定数据库名，还可以指定用户 id 和密码。使用角色管理数据库中的用户和用户组。 | 客户机使用 connect 语句连接数据库，这时要指定数据库名，还可以指定用户 id 和密码。使用角色管理数据库中的用户和用户组。 |
| 身份验证               | MySQL 在数据库级管理身份验证。 基本只支持密码认证。          | PostgreSQL 支持丰富的认证方法：信任认证、口令认证、Kerberos 认证、基于 Ident 的认证、LDAP 认证、PAM 认证 |
| 加密                   | 可以在表级指定密码来对数据进行加密。还可以使用 AES_ENCRYPT 和 AES_DECRYPT 函数对列数据进行加密和解密。可以通过 SSL 连接实现网络加密。 | 可以使用 pgcrypto 库中的函数对列进行加密/解密。可以通过 SSL 连接实现网络加密。 |
| 审计                   | 可以对 querylog 执行 grep。                                  | 可以在表上使用 PL/pgSQL 触发器来进行审计。                   |
| 查询解释               | 使用 EXPLAIN 命令查看查询的解释计划。                        | 使用 EXPLAIN 命令查看查询的解释计划。                        |
| 备份、恢复和日志       | InnoDB 使用写前（write-ahead）日志记录。支持在线和离线完全备份以及崩溃和事务恢复。需要第三方软件才能支持热备份。 | 在数据目录的一个子目录中维护写前日志。支持在线和离线完全备份以及崩溃、时间点和事务恢复。 可以支持热备份。 |
| JDBC 驱动程序          | 可以从 [参考资料](http://www.ibm.com/developerworks/cn/data/library/techarticles/dm-0606khatri/index.html#Resources) 下载 JDBC 驱动程序。 | 可以从 [参考资料](http://www.ibm.com/developerworks/cn/data/library/techarticles/dm-0606khatri/index.html#Resources) 下载 JDBC 驱动程序。 |
| 表类型                 | 取决于存储引擎。例如，NDB 存储引擎支持分区表，内存引擎支持内存表。 | 支持临时表、常规表以及范围和列表类型的分区表。不支持哈希分区表。 由于PostgreSQL的表分区是通过表继承和规则系统完成了，所以可以实现更复杂的分区方式。 |
| 索引类型               | 取决于存储引擎。MyISAM：BTREE，InnoDB：BTREE。               | 支持 B-树、哈希、R-树和 Gist 索引。                          |
| 约束                   | 支持主键、外键、惟一和非空约束。对检查约束进行解析，但是不强制实施。 | 支持主键、外键、惟一、非空和检查约束。                       |
| 存储过程和用户定义函数 | 支持 CREATE PROCEDURE 和 CREATE FUNCTION 语句。存储过程可以用 SQL 和 C++ 编写。用户定义函数可以用 SQL、C 和 C++ 编写。 | 没有单独的存储过程，都是通过函数实现的。用户定义函数可以用 PL/pgSQL（专用的过程语言）、PL/Tcl、PL/Perl、PL/Python 、SQL 和 C 编写。 |
| 触发器                 | 支持行前触发器、行后触发器和语句触发器，触发器语句用过程语言复合语句编写。 | 支持行前触发器、行后触发器和语句触发器，触发器过程用 C 编写。 |
| 系统配置文件           | my.conf                                                      | Postgresql.conf                                              |
| 数据库配置             | my.conf                                                      | Postgresql.conf                                              |
| 客户机连接文件         | my.conf                                                      | pg_hba.conf                                                  |
| XML 支持               | 有限的 XML 支持。                                            | 有限的 XML 支持。                                            |
| 数据访问和管理服务器   | **OPTIMIZE TABLE** —— 回收未使用的空间并消除数据文件的碎片 **myisamchk -analyze** —— 更新查询优化器所使用的统计数据（MyISAM 存储引擎） **mysql** —— 命令行工具 **MySQL Administrator** —— 客户机 GUI 工具 | **Vacuum** —— 回收未使用的空间 **Analyze** —— 更新查询优化器所使用的统计数据 **psql** —— 命令行工具 **pgAdmin** —— 客户机 GUI 工具 |
| 并发控制               | 支持表级和行级锁。InnoDB 存储引擎支持 READ_COMMITTED、READ_UNCOMMITTED、REPEATABLE_READ 和 SERIALIZABLE。使用 SET TRANSACTION ISOLATION LEVEL 语句在事务级设置隔离级别。 | 支持表级和行级锁。支持的 ANSI 隔离级别是 Read Committed（默认 —— 能看到查询启动时数据库的快照）和 Serialization（与 Repeatable Read 相似 —— 只能看到在事务启动之前提交的结果）。使用 SET TRANSACTION 语句在事务级设置隔离级别。使用 SET SESSION 在会话级进行设置。 |



**MySQL相对于PostgreSQL的劣势：
**

| **MySQL**                                                    | **PostgreSQL**                                               |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| 最重要的引擎InnoDB很早就由Oracle公司控制。目前整个MySQL数据库都由Oracle控制。 | BSD协议，没有被大公司垄断。                                  |
| 对复杂查询的处理较弱，查询优化器不够成熟                     | 很强大的查询优化器，支持很复杂的查询处理。                   |
| 只有一种表连接类型:嵌套循环连接(nested-loop),不支持排序-合并连接(sort-merge join)与散列连接(hash join)。 | 都支持                                                       |
| 性能优化工具与度量信息不足                                   | 提供了一些性能视图，可以方便的看到发生在一个表和索引上的select、delete、update、insert统计信息，也可以看到cache命中率。网上有一个开源的pgstatspack工具。 |
| InnoDB的表和索引都是按相同的方式存储。也就是说表都是索引组织表。这一般要求主键不能太长而且插入时的主键最好是按顺序递增，否则对性能有很大影响。 | 不存在这个问题。                                             |
| 大部分查询只能使用表上的单一索引;在某些情况下，会存在使用多个索引的查询,但是查询优化器通常会低估其成本,它们常常比表扫描还要慢。 | 不存在这个问题                                               |
| 表增加列，基本上是重建表和索引，会花很长时间。               | 表增加列，只是在数据字典中增加表定义，不会重建表             |
| 存储过程与触发器的功能有限。可用来编写存储过程、触发器、计划事件以及存储函数的语言功能较弱 | 除支持pl/pgsql写存储过程，还支持perl、python、Tcl类型的存储过程：pl/perl，pl/python，pl/tcl。 也支持用C语言写存储过程。 |
| 不支持Sequence。                                             | 支持                                                         |
| 不支持函数索引，只能在创建基于具体列的索引。 不支持物化视图。 | 支持函数索引，同时还支持部分数据索引，通过规则系统可以实现物化视图的功能。 |
| 执行计划并不是全局共享的, 仅仅在连接内部是共享的。           | 执行计划共享                                                 |
| MySQL支持的SQL语法(ANSI SQL标准)的很小一部分。不支持递归查询、通用表表达式（Oracle的with 语句）或者窗口函数（分析函数）。 | 都 支持                                                      |
| 不支持用户自定义类型或域(domain)                             | 支持。                                                       |
| 对于时间、日期、间隔等时间类型没有秒以下级别的存储类型       | 可以精确到秒以下。                                           |
| 身份验证功能是完全内置的，不支持操作系统认证、PAM认证，不支持LDAP以及其它类似的外部身份验证功能。 | 支持OS认证、Kerberos 认证 、Ident 的认证、LDAP 认证、PAM 认证 |
| 不支持database link。有一种叫做Federated的存储引擎可以作为一个中转将查询语句传递到远程服务器的一个表上,不过,它功能很粗糙并且漏洞很多 | 有dblink，同时还有一个dbi-link的东西，可以连接到oracle和mysql上。 |
| Mysql Cluster可能与你的想象有较大差异。开源的cluster软件较少。 复制(Replication)功能是异步的,并且有很大的局限性.例如,它是单线程的(single-threaded),因此一个处理能力更强的Slave的恢复速度也很难跟上处理能力相对较慢的Master. | 有丰富的开源cluster软件支持。                                |
| explain看执行计划的结果简单。                                | explain返回丰富的信息。                                      |
| 类似于ALTER TABLE或CREATE TABLE一类的操作都是非事务性的.它们会提交未提交的事务，并且不能回滚也不能做灾难恢复 | DDL也是有事务的。                                            |





[PostgreSQL与MySQL比较 - 江湖一浪子 - 博客园 (cnblogs.com)](https://www.cnblogs.com/geekmao/p/8541817.html)

