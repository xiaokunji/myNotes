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

**Virtual Cluster**

同一个服务下的所有服务实例组成一个默认集群, 集群可以被进一步按需求划分，划分的单位可以是虚拟集群。

> 如图: <img src="https://gitee.com/xiaokunji/my-images/raw/master/myMD/nacos服务集群.png" alt="image-20220124164513719" style="zoom:50%;" />



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

[Nacos 集群部署 - CanntBelieve - 博客园 (cnblogs.com)](https://www.cnblogs.com/FlyAway2013/p/11201250.html)

# 4. 注册中心

 **参数解释**

|  参数	| 描述 |
| ---- | ---- |
|  `com.alibaba.nacos.naming.log.level`	| Naming客户端的日志级别，改属性通过客户端启动时通过命令行加参数指定 注：默认为info |
| spring.cloud.nacos.discovery.heart-beat-interval 	| nacos客户端向服务端发送心跳的时间间隔，默认5s<br />注：客户端向服务端每隔5s向服务端发送心跳请求，进行服务续租，告诉服务端该实例IP健康。若在3次心跳的间隔时间(默认15s)内服务端没有接受到该实例的心跳请求，则认为该实例不健康，该实例将无法被消费。如果再次经历3次心跳的间隔时间，服务端接受到该实例的请求，那么会立刻将其设置外健康，并可以被消费，若未接受到，则删除该实例的注册信息。推荐配置为5s，如果有的业务线希望服务下线或者出故障时希望尽快被发现，可以适当减少该值。 |
| spring.cloud.nacos.discovery.heart-beat-timeout: 	| 服务端没有接受到客户端心跳请求就将其设为不健康的时间间隔，默认为15s. 注：推荐值该值为15s即可，如果有的业务线希望服务下线或者出故障时希望尽快被发现，可以适当减少该值。|
| spring.cloud.nacos.discovery.log-name: 	|  nacos客户端会在启动时打印一部分发送注册请求信息和异常日志，可以通过日志查看注册的nacos集群地址、服务名、nameSpace、IP、元数据等内容，文件名默认为naming.log . 注:推荐将该日志的位置设置为和其他日志在一个文件夹下 |
| spring.cloud.nacos.discovery.metadata: 	| 给服务添加一些标签，例如属于什么业务线，该元数据会持久化存储在服务端，但是客户端消费时不会获取到此值，默认为空. 注:推荐为空，我们可以通过已经注册的服务名来找到具体的业务线，无需添加metadata|
| spring.cloud.nacos.discovery.namespace: |  命名空间ID，Nacos通过不同的命名空间来区分不同的环境，进行数据隔离，服务消费时只能消费到对应命名空间下的服务。|
| spring.cloud.nacos.discovery.naming-load-cache-at-start: 	|  默认为false。客户端在启动时是否读取本地配置项(一个文件)来获取服务列表. 注：推荐该值为false，若改成true。则客户端会在本地的一个文件中保存服务信息，当下次宕机启动时，会优先读取本地的配置对外提供服务。|
| spring.cloud.nacos.discovery.port: 	| 向nacos注册服务时，服务对应的端口号 . 注:无需修改，默认为应用对外提供服务的端口号,server.port |
| spring.cloud.nacos.discovery.register-enabled: |  该项目是否向注册中心注册服务，默认为true . 注：如果服务从注册中心只消费服务，没有对外提供服务，那么该值可设置为false，可减少客户端线程池的创建，无需向服务端发送心跳请求，提高性能。|
| spring.cloud.nacos.discovery.server-addr	|  nacos集群地址。注：多个IP可以通过“，”号隔离，例如`192.168.80.1:8848,192.168.80.1:8848`  填写域名时前缀不要加上http:// |
| spring.cloud.nacos.discovery.service:  | 项目向注册中心注册服务时的服务名，默认为spring.application.name 变量 . 注:该服务名必须使用小写，因为nacos服务名区分大小写，如果服务名不完全匹配，那么无法调用服务 |
| spring.cloud.nacos.discovery.watch-delay: 	| 默认为30s。客户端在启动时会创建一个线程池，该线程定期去查询服务端的信息列表，该请求不会立刻返回，默认等待30s，若在30s内，服务端信息列表发生变化，则该请求立刻返回，通知客户端拉取服务端的服务信息列表，若30s内，没有变化，则30s时该请求返回响应，客户端服务列表不变，再次发生该请求。 注：推荐该值为30s即可，无需修改 |
| spring.cloud.nacos.discovery.watch.enabled: 	| 默认为true,客户端在启动时会创建一个线程池，该线程定期去查询服务端的信息列表，该请求不会立刻返回，默认等待30s，若在30s内，服务端信息列表发生变化，则该请求立刻返回，通知客户端拉取服务端的服务信息列表，若30s内，没有变化，则30s时该请求返回响应，客户端服务列表不变，再次发生该请求。 注:推荐该功能为true，这是nacos类似长连接推送服务变化的功能，不要关闭 |
| spring.cloud.nacos.discovery.weight: | nacos支持服务端基于权重的负载均衡，该值默认为1 . 注:建议该值保持默认即可，因为代码可能会部署到不同的服务器上，无法确保某台服务器的配置一定较好，如果有需要修改该值的需求，可以上控制台修改，这样可以保证对应IP服务器的权重值较高 |

> <b style='color:red'> watch-delay 有什么作用?</b>



**使用配置文件如下:**

```yaml
spring:   
    cloud:
        nacos:
        	-- 配置中心
            config:
                namespace: xkjNamespace
                server-addr: 192.168.2.101:8841,192.168.2.101:8845,192.168.2.101:8848
                group: xkjGroup
            -- 注册中心
            discovery:
                server-addr: ${spring.cloud.nacos.config.server-addr}
                cluster-name: xkjCluster
                -- 此处是命名空间的id
                namespace: xkjNamespace
                service: javaDemoDiscovery
                group: xkjGroup

    application:
        name: javaDemo

```



# 5. 配置中心





# A&Q

1. nacos集群架构?

   **答**:  和Eureka集群架构一样,他的数据是点对点的, 本身是主从结构,也就是说会产生过半选举,脑裂等知识点, 默认支持cap理论中的ap模式. 生产部署nacos集群时,推荐通过NGINX给nacos集群做负载均衡

   [浅谈Nacos中的CAP - 包子卖完了嘛 - 博客园 (cnblogs.com)](https://www.cnblogs.com/songfeifeine/p/14321014.html)

2. nacos如何实现动态配置?

