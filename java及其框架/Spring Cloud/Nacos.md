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



pom.xml

```xml
   <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
            <version>2.2.1.RELEASE</version>
        </dependency>
```





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

在 Nacos Spring Cloud 中，dataId 的完整格式如下：

`${prefix}-${spring.profile.active}.${file-extension}`

>- prefix 默认为所属工程配置spring.application.name 的值（即：nacos-provider），也可以通过配置项 spring.cloud.nacos.config.prefix来配置；
>- spring.profile.active 即为当前环境对应的 profile，详情可以参考 Spring Boot文档。 注意：当spring.profile.active 为空时，对应的连接符 - 也将不存在，dataId 的拼接格式变成 ${prefix}.${file-extension}
>- file-exetension 为配置内容的数据格式，可以通过配置项 spring.cloud.nacos.config.file-extension 来配置。目前只支持 properties 和 yaml 类型；默认为 properties ；

[Nacos 入门教程_cristianoxm的博客-CSDN博客_nacos 教程](https://blog.csdn.net/cristianoxm/article/details/113639172)

pom.xml

```xml
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
            <version>2.2.1.RELEASE</version>
        </dependency>
```



配置文件:

> 新增bootstrap.yml文件，配置信息写在该文件里。（问题：如放在application.yml会导致项目启动报找不到配置属性错误，原因：application.yml与bootstrap.yml加载顺序优先级问题。）

```yaml
spring:
  application:
    name: javaDemo
  cloud:
    nacos:
      server-addr: ${spring.cloud.client.ip-address}:8848
      config:
        namespace: xkjNamespace
        file-extension: yml
        shared-configs:
          - data-id: mysqlTest.yml
            refresh: true
            group: xkjGroup
        extension-configs:
          - data-id: MyTest.yml
            refresh: true
            group: xkjGroup
            
        group: xkjGroup # 该配置如果放在share和extentsion配置的前面,将覆盖其下的组

```



java使用类:

```java
    @Value("${my.name:本地值name}")
    private String name;

    @Value("${person.name:本地值person.name}")
    private String person_name;

    @Value("${personAge:本地值personAge}")
    private String personAge;

    @Value("${personName:本地值personName}")
    private String personName;
```



![image-20220306005109148](https://gitee.com/xiaokunji/my-images/raw/master/myMD/nacos-配置中心-使用.png)



## 5.1 动态配置

### 5.1.1 使用

通常获取配置文件的方式

1.  `@Value`

2. `@ConfigurationProperties(Prefix)`

如果是在运行时要动态更新的话，

第一种方式要在bean上加`@RefreshScope `

第二种方式是自动支持的。



### 5.1.2 原理详解

![image-20220308002516886](https://gitee.com/xiaokunji/my-images/raw/master/myMD/nacos动态配置日志.png)

#### 5.1.2.1 采用延迟线程池定时执行"监听"文件是否有修改

服务启动后就回轮询的打印图上信息, 当有配置被改动时, 这个`[]` 就会包含数据了, 可想而知这是监听日志了, 那就找到这段代码

**`ClientWorker.java`**

```java
class LongPollingRunnable implements Runnable {
        private int taskId;

        public LongPollingRunnable(int taskId) {
            this.taskId = taskId;
        }

        @Override
        public void run() {

            List<CacheData> cacheDatas = new ArrayList<CacheData>();
            List<String> inInitializingCacheList = new ArrayList<String>();
            try {
                // check failover config
                for (CacheData cacheData : cacheMap.get().values()) {
                    if (cacheData.getTaskId() == taskId) {
                        cacheDatas.add(cacheData);
                        try {
                            checkLocalConfig(cacheData);
                            if (cacheData.isUseLocalConfigInfo()) {
                                cacheData.checkListenerMd5();
                            }
                        } catch (Exception e) {
                            LOGGER.error("get local config info error", e);
                        }
                    }
                }

                // check server config
                List<String> changedGroupKeys = checkUpdateDataIds(cacheDatas, inInitializingCacheList);
                LOGGER.info("get changedGroupKeys:" + changedGroupKeys);
//              省略剩下代码  .....
            }
        }
}
```

从这里就能看出, 先是对配置做了一些检查, 然后就打印结果, 而且这个是在run方法里, 说明这里肯定是开了线程在跑的, 找到调用`LongPollingRunnable`这个类的地方

还是在同一个类中, 发现是在execute中执行的, 那就是弄了一个线程池, 而且这里是在for循环里, 看一下任务, 就会联想到多个配置文件的情况, 同时监听的

```java
public void checkConfigInfo() {
        // 分任务
        int listenerSize = cacheMap.get().size();
        // 向上取整为批数
        int longingTaskCount = (int) Math.ceil(listenerSize / ParamUtil.getPerTaskConfigSize());
        if (longingTaskCount > currentLongingTaskCount) {
            for (int i = (int) currentLongingTaskCount; i < longingTaskCount; i++) {
                // 要判断任务是否在执行 这块需要好好想想。 任务列表现在是无序的。变化过程可能有问题
                executorService.execute(new LongPollingRunnable(i));
            }
            currentLongingTaskCount = longingTaskCount;
        }
    }
```

看一下这个线程池的参数.

```java
@SuppressWarnings("PMD.ThreadPoolCreationRule")
    public ClientWorker(final HttpAgent agent, final ConfigFilterChainManager configFilterChainManager, final Properties properties) {
        this.agent = agent;
        this.configFilterChainManager = configFilterChainManager;
        // Initialize the timeout parameter
        init(properties);
        executor = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("com.alibaba.nacos.client.Worker." + agent.getName());
                t.setDaemon(true);
                return t;
            }
        });
		// 执行的线程池
        executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("com.alibaba.nacos.client.Worker.longPolling." + agent.getName());
                t.setDaemon(true);
                return t;
            }
        });

        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    checkConfigInfo();
                } catch (Throwable e) {
                    LOGGER.error("[" + agent.getName() + "] [sub-check] rotate check error", e);
                }
            }
        }, 1L, 10L, TimeUnit.MILLISECONDS);
    }
```

还会发现这里还有个延迟线程池,而且只有一个线程数, 发现里面执行了`checkConfigInfo()`, 这刚好`LongPollingRunnable` 类执行的所在方法.

至此, 它的定时执行就清楚了, **它开了一个单线程的延时线程池,每隔10ms执行一次, 线程里再用线程池去"监听"文件是否有修改**

但是我们发现日志间隔并不是10ms,而且这个间隔也太小了, 肯定不合理



#### 5.1.2.2 通过长轮询的方式获得修改过的文件及其内容

去看一下它是如何"监听", 跟进`com.alibaba.nacos.client.config.impl.ClientWorker#checkUpdateDataIds`

```java
/**
     * 从Server获取值变化了的DataID列表。返回的对象里只有dataId和group是有效的。 保证不返回NULL。
     */
    List<String> checkUpdateDataIds(List<CacheData> cacheDatas, List<String> inInitializingCacheList) throws IOException {
        // 构造参数- 通过配置dataId/group/tenant等数据来指定文件
        StringBuilder sb = new StringBuilder();
        for (CacheData cacheData : cacheDatas) {
            if (!cacheData.isUseLocalConfigInfo()) {
                sb.append(cacheData.dataId).append(WORD_SEPARATOR);
                sb.append(cacheData.group).append(WORD_SEPARATOR);
                if (StringUtils.isBlank(cacheData.tenant)) {
                    sb.append(cacheData.getMd5()).append(LINE_SEPARATOR);
                } else {
                    sb.append(cacheData.getMd5()).append(WORD_SEPARATOR);
                    sb.append(cacheData.getTenant()).append(LINE_SEPARATOR);
                }
                if (cacheData.isInitializing()) {
                    // cacheData 首次出现在cacheMap中&首次check更新
                    inInitializingCacheList
                        .add(GroupKey.getKeyTenant(cacheData.dataId, cacheData.group, cacheData.tenant));
                }
            }
        }
        boolean isInitializingCacheList = !inInitializingCacheList.isEmpty();
        // 核心方法- 检查更新文件
        return checkUpdateConfigStr(sb.toString(), isInitializingCacheList);
    }
```

`com.alibaba.nacos.client.config.impl.ClientWorker#checkUpdateConfigStr`

```java

    /**
     * 从Server获取值变化了的DataID列表。返回的对象里只有dataId和group是有效的。 保证不返回NULL。
     */
    List<String> checkUpdateConfigStr(String probeUpdateString, boolean isInitializingCacheList) throws IOException {
        List<String> params = new ArrayList<String>(2);
        params.add(Constants.PROBE_MODIFY_REQUEST);
        params.add(probeUpdateString);

        List<String> headers = new ArrayList<String>(2);
        headers.add("Long-Pulling-Timeout");
        // 设置长轮询的过期时间
        headers.add("" + timeout);

        // told server do not hang me up if new initializing cacheData added in
        if (isInitializingCacheList) {
            headers.add("Long-Pulling-Timeout-No-Hangup");
            headers.add("true");
        }

        if (StringUtils.isBlank(probeUpdateString)) {
            return Collections.emptyList();
        }

        try {
            // In order to prevent the server from handling the delay of the client's long task,
            // increase the client's read timeout to avoid this problem.

            long readTimeoutMs = timeout + (long) Math.round(timeout >> 1);
            // 长轮询请求
            HttpResult result = agent.httpPost(Constants.CONFIG_CONTROLLER_PATH + "/listener", headers, params,
                agent.getEncode(), readTimeoutMs);

            if (HttpURLConnection.HTTP_OK == result.code) {
                setHealthServer(true);
                // 解析返参
                return parseUpdateDataIdResponse(result.content);
            } else {
                setHealthServer(false);
                LOGGER.error("[{}] [check-update] get changed dataId error, code: {}", agent.getName(), result.code);
            }
        } catch (IOException e) {
            setHealthServer(false);
            LOGGER.error("[" + agent.getName() + "] [check-update] get changed dataId exception", e);
            throw e;
        }
        return Collections.emptyList();
    }

```

就会发现它是发了一个请求过去, 然后通过`parseUpdateDataIdResponse(result.content)` 方法解析出返参里面的 dataId/group/tenant等数据

这个请求中设置了一些长轮询的参数,表示这是一个长轮询的请求

> **长轮询:** 客户端发起Long Polling，此时如果服务端没有相关数据，会hold住请求，直到服务端有相关数据，或者等待一定时间超时才会返回。返回后，客户端又会立即再次发起下一次Long Polling。



这里只是拿到了一个dataId和group等数据,那什么时候拿到具体的配置信息呢?  继续往下看 `LongPollingRunnable#run`

```java
                // 检查更新的dataId
                List<String> changedGroupKeys = checkUpdateDataIds(cacheDatas, inInitializingCacheList);
                LOGGER.info("get changedGroupKeys:" + changedGroupKeys);
				// 遍历这些文件
                for (String groupKey : changedGroupKeys) {
                    String[] key = GroupKey.parseKey(groupKey);
                    String dataId = key[0];
                    String group = key[1];
                    String tenant = null;
                    if (key.length == 3) {
                        tenant = key[2];
                    }
                    try {
                        // 获得具体配置
                        String[] ct = getServerConfig(dataId, group, tenant, 3000L);
                        CacheData cache = cacheMap.get().get(GroupKey.getKeyTenant(dataId, group, tenant));
                        // 把内容直接写到cacheMap中
                        cache.setContent(ct[0]);
                        if (null != ct[1]) {
                            cache.setType(ct[1]);
                        }
                        LOGGER.info("[{}] [data-received] dataId={}, group={}, tenant={}, md5={}, content={}, type={}",
                            agent.getName(), dataId, group, tenant, cache.getMd5(),
                            ContentUtils.truncateContent(ct[0]), ct[1]);
                    } catch (NacosException ioe) {
                        String message = String.format(
                            "[%s] [get-update] get changed config exception. dataId=%s, group=%s, tenant=%s",
                            agent.getName(), dataId, group, tenant);
                        LOGGER.error(message, ioe);
                    }
                }
                for (CacheData cacheData : cacheDatas) {
                    if (!cacheData.isInitializing() || inInitializingCacheList
                        .contains(GroupKey.getKeyTenant(cacheData.dataId, cacheData.group, cacheData.tenant))) {
                        // 检查md5
                        cacheData.checkListenerMd5();
                        cacheData.setInitializing(false);
                    }
                }
// ....省略剩下代码
}
```

`com.alibaba.nacos.client.config.impl.ClientWorker#getServerConfig` 获得具体配置的方法

```java
    public String[] getServerConfig(String dataId, String group, String tenant, long readTimeout)  throws NacosException {
        String[] ct = new String[2];
        if (StringUtils.isBlank(group)) {
            group = Constants.DEFAULT_GROUP;
        }

        HttpResult result = null;
        try {
            List<String> params = null;
            if (StringUtils.isBlank(tenant)) {
                params = new ArrayList<String>(Arrays.asList("dataId", dataId, "group", group));
            } else {
                params = new ArrayList<String>(Arrays.asList("dataId", dataId, "group", group, "tenant", tenant));
            }
            // 通过get请求,获得具体配置
            result = agent.httpGet(Constants.CONFIG_CONTROLLER_PATH, null, params, agent.getEncode(), readTimeout);
        } catch (IOException e) {
            String message = String.format(
                "[%s] [sub-server] get server config exception, dataId=%s, group=%s, tenant=%s", agent.getName(),
                dataId, group, tenant);
            LOGGER.error(message, e);
            throw new NacosException(NacosException.SERVER_ERROR, e);
        }
        switch (result.code) {
            case HttpURLConnection.HTTP_OK:
                // 先放到本地文件中
                LocalConfigInfoProcessor.saveSnapshot(agent.getName(), dataId, group, tenant, result.content);
                // 将请求返参放入ct数组中
                ct[0] = result.content;
                if (result.headers.containsKey(CONFIG_TYPE)) {
                    ct[1] = result.headers.get(CONFIG_TYPE).get(0);
                } else {
                    ct[1] = ConfigType.TEXT.getType();
                }
                return ct;
             case HttpURLConnection.HTTP_NOT_FOUND:
        //  省略剩下代码......
    }
```

至此, 清楚了它是如何拿到具体配置的了, **它通过(一次post请求)长轮询的方式和服务端建立连接, 获得dataId/group等数据, 再通过这些参数发起get请求获得具体的配置文件内容,并写到本地缓存中使用**



#### 5.1.2.3 拿到配置后通过applicationContext更新到项目内存中

它取到这些配置后,是如何写到项目的内存中并使其生效的呢?

```java
 try {
                        String[] ct = getServerConfig(dataId, group, tenant, 3000L);
                        CacheData cache = cacheMap.get().get(GroupKey.getKeyTenant(dataId, group, tenant));
                        cache.setContent(ct[0]);
                        if (null != ct[1]) {
                            cache.setType(ct[1]);
                        }
                        LOGGER.info("[{}] [data-received] dataId={}, group={}, tenant={}, md5={}, content={}, type={}",
                            agent.getName(), dataId, group, tenant, cache.getMd5(),
                            ContentUtils.truncateContent(ct[0]), ct[1]);
                    } catch (NacosException ioe) {
                        String message = String.format(
                            "[%s] [get-update] get changed config exception. dataId=%s, group=%s, tenant=%s",
                            agent.getName(), dataId, group, tenant);
                        LOGGER.error(message, ioe);
                    }
                }
                for (CacheData cacheData : cacheDatas) {
                    if (!cacheData.isInitializing() || inInitializingCacheList
                        .contains(GroupKey.getKeyTenant(cacheData.dataId, cacheData.group, cacheData.tenant))) {
                        // 检查md5
                        cacheData.checkListenerMd5();
                        cacheData.setInitializing(false);
                    }
                }
// 省略剩下代码.....
}

```

在取到具体配置后,遍历cacheDatas数据,并检查md5, 跟进去看一下, 它开始出现监听器了

```java

    void checkListenerMd5() {
        for (ManagerListenerWrap wrap : listeners) {
            if (!md5.equals(wrap.lastCallMd5)) {
                safeNotifyListener(dataId, group, content, type, md5, wrap);
            }
        }
    }

    private void safeNotifyListener(final String dataId, final String group, final String content, final String type,
                                    final String md5, final ManagerListenerWrap listenerWrap) {
        final Listener listener = listenerWrap.listener;

        Runnable job = new Runnable() {
            @Override
            public void run() {
                ClassLoader myClassLoader = Thread.currentThread().getContextClassLoader();
                ClassLoader appClassLoader = listener.getClass().getClassLoader();
                try {
                    if (listener instanceof AbstractSharedListener) {
                        AbstractSharedListener adapter = (AbstractSharedListener) listener;
                        adapter.fillContext(dataId, group);
                        LOGGER.info("[{}] [notify-context] dataId={}, group={}, md5={}", name, dataId, group, md5);
                    }
                    // 执行回调之前先将线程classloader设置为具体webapp的classloader，以免回调方法中调用spi接口是出现异常或错用（多应用部署才会有该问题）。
                    Thread.currentThread().setContextClassLoader(appClassLoader);

                    ConfigResponse cr = new ConfigResponse();
                    cr.setDataId(dataId);
                    cr.setGroup(group);
                    cr.setContent(content);
                    configFilterChainManager.doFilter(null, cr);
                    String contentTmp = cr.getContent();
                    // 处理配置信息
                    listener.receiveConfigInfo(contentTmp);

                    // compare lastContent and content
                    if (listener instanceof AbstractConfigChangeListener) {
                        Map data = ConfigChangeHandler.getInstance().parseChangeData(listenerWrap.lastContent, content, type);
                        ConfigChangeEvent event = new ConfigChangeEvent(data);
                        ((AbstractConfigChangeListener)listener).receiveConfigChange(event);
                        listenerWrap.lastContent = content;
                    }

                    listenerWrap.lastCallMd5 = md5;
                    LOGGER.info("[{}] [notify-ok] dataId={}, group={}, md5={}, listener={} ", name, dataId, group, md5,
                        listener);
                } catch (NacosException de) {
                    LOGGER.error("[{}] [notify-error] dataId={}, group={}, md5={}, listener={} errCode={} errMsg={}", name,
                        dataId, group, md5, listener, de.getErrCode(), de.getErrMsg());
                } catch (Throwable t) {
                    LOGGER.error("[{}] [notify-error] dataId={}, group={}, md5={}, listener={} tx={}", name, dataId, group,
                        md5, listener, t.getCause());
                } finally {
                    Thread.currentThread().setContextClassLoader(myClassLoader);
                }
            }
        };

        final long startNotify = System.currentTimeMillis();
        try {
            if (null != listener.getExecutor()) {
                listener.getExecutor().execute(job);
            } else {
                job.run();
            }
        } catch (Throwable t) {
            LOGGER.error("[{}] [notify-error] dataId={}, group={}, md5={}, listener={} throwable={}", name, dataId, group,
                md5, listener, t.getCause());
        }
        final long finishNotify = System.currentTimeMillis();
        LOGGER.info("[{}] [notify-listener] time cost={}ms in ClientWorker, dataId={}, group={}, md5={}, listener={} ",
            name, (finishNotify - startNotify), dataId, group, md5, listener);
    }
```

这么长的代码,核心就是处理了那个runable, 其中调用了`listener.receiveConfigInfo(contentTmp)` 方法处理的监听器,它是一个抽象类, 找到它的实现类

`com.alibaba.cloud.nacos.refresh.NacosContextRefresher` 

```java
	private void registerNacosListener(final String groupKey, final String dataKey) {
		String key = NacosPropertySourceRepository.getMapKey(dataKey, groupKey);
		Listener listener = listenerMap.computeIfAbsent(key,
				lst -> new AbstractSharedListener() {
					@Override
					public void innerReceive(String dataId, String group,
							String configInfo) {
						refreshCountIncrement();
						nacosRefreshHistory.addRefreshRecord(dataId, group, configInfo);
						// todo feature: support single refresh for listening
                        // 通过applicationContext的事件去更新配置
						applicationContext.publishEvent(
								new RefreshEvent(this, null, "Refresh Nacos config"));
						if (log.isDebugEnabled()) {
							log.debug(String.format(
									"Refresh Nacos config group=%s,dataId=%s,configInfo=%s",
									group, dataId, configInfo));
						}
					}
				});
		try {
			configService.addListener(dataKey, groupKey, listener);
		}
		catch (NacosException e) {
			log.warn(String.format(
					"register fail for nacos listener ,dataId=[%s],group=[%s]", dataKey,
					groupKey), e);
		}
	}
```

至此, 清楚了获得的配置是如何生效的, 它将获得发生修改过的文件, 如果md5不一样了, 则执行监听器,通过applicationContext 更新配置到项目内存中

> 明明已经知道了哪些文件被修改了,为啥还有比对md5, 因为可能是没有修改具体内容,只是点了编辑并保存
>
> <span style="color:red">md5用的是java的digest和位移,md5可能存在冲突, 那怎么解决冲突问题的?</span>



### 5.1.3 总结

- 1.Nacos 客户端会循环请求服务端变更的数据，并且超时时间设置为30s，当配置发生变化时，请求的响应会立即返回，否则会一直等到 29.5s+ 之后再返回响应
- 2.Nacos 客户端能够实时感知到服务端配置发生了变化。
- 3.实时感知是建立在客户端拉和服务端“推”的基础上，但是这里的服务端“推”需要打上引号，因为服务端和客户端直接本质上还是通过 http 进行数据通讯的，之所以有“推”的感觉，是因为服务端主动将变更后的数据通过 http 的 response 对象提前写入了。



[Long Polling长轮询详解 - 简书 (jianshu.com)](https://www.jianshu.com/p/d3f66b1eb748?isappinstalled=0)

[NACOS动态配置 - barryzhou - 博客园 (cnblogs.com)](https://www.cnblogs.com/barry-blog/articles/14305358.html)

[spring boot 配置文件动态更新原理 以Nacos为例 - 二奎 - 博客园 (cnblogs.com)](https://www.cnblogs.com/hankuikui/p/12084193.html)

[Nacos 配置中心原理分析 -第一篇- 简书 (jianshu.com)](https://www.jianshu.com/p/38b5452c9fec)

[Nacos 配置中心原理分析 -第二篇 - 简书 (jianshu.com)](https://www.jianshu.com/p/acb9b1093a54)

# A&Q

1. nacos集群架构?

   **答**:  和Eureka集群架构一样,他的数据是点对点的, 本身是主从结构,也就是说会产生过半选举,脑裂等知识点, 默认支持cap理论中的ap模式. 生产部署nacos集群时,推荐通过NGINX给nacos集群做负载均衡

   [浅谈Nacos中的CAP - 包子卖完了嘛 - 博客园 (cnblogs.com)](https://www.cnblogs.com/songfeifeine/p/14321014.html)

   

2. nacos如何实现动态配置?

   答: 使用长轮询机制,由客户端定时发起请求询问服务端是否有修改, 如果存在修改则通过applicationContext的publishEvent更新内存中的配置

   > 一次请求后将阻塞29.5s+, 才会发起下一次请求, 如果遇到服务端存在修改文件, 则会立即返回.
   >
   > 定时机制是开了一个延时线程池,其中只有一个线程,每隔10ms发起一次请求, 服务启动时该任务就执行了

   

   

