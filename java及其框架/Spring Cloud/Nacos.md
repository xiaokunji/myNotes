[toc]



# 前言

阿里开源的注册中心和配置中心

![nacos_map](https://nacos.io/img/nacosMap.jpg)

[Nacos 官方文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)

[Nacos 源码](https://github.com/alibaba/nacos)

# 1. 概念

**Name Space**

用于进行租户粒度的配置隔离。不同的命名空间下，可以存在相同的 Group 或 Data ID 的配置。Namespace 的常用场景之一是**不同环境的配置**的区分隔离，例如开发测试环境和生产环境的资源（如配置、服务）隔离等。

**Configuration**

配置文件

**Data ID**

Nacos 中的某个配置集的 ID。配置集 ID 是组织划分配置的维度之一。Data ID 通常用于组织划分系统的配置集。一个系统或者应用可以包含多个配置集，每个配置集都可以被一个有意义的名称标识。Data ID 通常采用类 Java 包（如 com.taobao.tc.refund.log.level）的命名规则保证全局唯一性。此命名规则非强制。

**Group**

Nacos 中的一组配置集，是组织配置的维度之一。通过一个有意义的字符串（如 Buy 或 Trade ）对配置集进行分组，从而区分 Data ID 相同的配置集。当您在 Nacos 上创建一个配置时，如果未填写配置分组的名称，则配置分组的名称默认采用 DEFAULT_GROUP 。配置分组的常见场景：不同的应用或组件使用了相同的配置类型，如 database_url 配置和 MQ_topic 配置。



![nacos_data_model](https://cdn.nlark.com/yuque/0/2019/jpeg/338441/1561217857314-95ab332c-acfb-40b2-957a-aae26c2b5d71.jpeg)



[Nacos 概念](https://nacos.io/zh-cn/docs/concepts.html)



# 2. 架构

![nacos_arch.jpg](https://cdn.nlark.com/yuque/0/2019/jpeg/338441/1561217892717-1418fb9b-7faa-4324-87b9-f1740329f564.jpeg)



![nacos-logic.jpg](https://cdn.nlark.com/yuque/0/2019/png/338441/1561217775318-6e408805-18bb-4242-b4e9-83c5b929b469.png)



# 3. 安装



Nacos Server下载地址：

https://github.com/alibaba/nacos/releases

下载解压后打开bin目录

`/work/nacos/bin/startup.sh -m standalone`

> windows: `startup.cmd -m standalone`

启动完后访问后台

Nacos Server的后台访问地址：

http://192.168.10.13:8848/nacos/index.html

默认账号和密码为：`nacos/nacos`

![image-20220109225601199](https://gitee.com/xiaokunji/my-images/raw/master/myMD/nacos启动日志.png)



## 3.1 Nacos Server 有两种运行模式：

- standalone
- cluster (默认模式)

### 3.1.1 standalone 模式

此模式一般用于 demo 和测试，不用改任何配置，直接敲以下命令执行

### 3.1.2 cluster 模式

cluster 模式需要依赖 MySQL，然后改两个配置文件：

```
conf/cluster.conf
conf/application.properties
```

**`conf/cluster.conf`**

```conf
# 集群节点(此处是在同一台机器上,所以用ip区分)
192.168.10.13:8841
192.168.10.13:8845
192.168.10.13:8848
```

**`conf/application.properties`**

```properties
spring.datasource.platform=mysql
db.num=1
db.url.0=jdbc:mysql://127.0.0.1:3306/nacos_config?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC&AllowPublicKeyRetrieval=True
db.user.0=test
db.password.0=xiaokunji
```

> 每个节点上需要改这两个文件
>
> 注意在application.properties文件中修改端口



启动后 任意节点都能进入管理界面

![image-20220110000124372](https://gitee.com/xiaokunji/my-images/raw/master/myMD/nacos控制台-集群管理.png)

[阿里巴巴NACOS（3）- 部署Nacos的生产集群环境-阿里云开发者社区 (aliyun.com)](https://developer.aliyun.com/article/738219)



> 一般集群部署需要搭配NGINX, 这样就可以统一管理对外ip, 且需要利用NGINX的故障转移策略, nacos本身不具备故障转移
