[toc]
# 1.介绍
在Spring boot应用中，要实现可监控的功能，依赖的是 spring-boot-starter-actuator 这个组件。它提供了很多监控和管理你的spring boot应用的HTTP或者JMX端点，并且你可以有选择地开启和关闭部分功能。当你的spring boot应用中引入下面的依赖之后，将自动的拥有审计、健康检查、Metrics监控功能。
>基于springboot2.X版本

链接：https://www.jianshu.com/p/1aadc4c85f51

# 2.使用
```
// 先有parent和web包
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
然后就能访问了,哈哈哈,是不是超简单,`http://127.0.0.1:8767/actuator/health`
> 端口是项目的访问地址,即使是用Feign访问的server层   
返回值:
> - up:服务正常 
> - down:服务异常

**它是号称可以做监控中心的插件,展示东西远不止这些**   
加上配置:`management.endpoint.health.show-details=always`   
表示总是展示详细信息,因为还有个选择是`when-authorized`,默认是不展示(`never`)   
再次访问,会展示出项目中使用的插件,比如redis,数据库等使用情况还有磁盘的使用情况,基本上,用这个能很好的看到项目的当前状态.

++默认情况++下(Http)还能访问一个链接,/info (`http://127.0.0.1:8767/actuator/info`),这个能获取应用程序的定制信息，这些信息由 info 打头的属性提供,比如:`info.app.version=V1.0.0`,访问地址就能得到这个样的json字符串
>{
    "app":{
        "version":"V1.0.0"
    }
}

还有其他链接:

Http 方法 | 路径 | 描述
---|---|---
get | /autoconfig |提供了一份自动配置报告，记录哪些自动配置条件通过了，哪些没通过            
get | /configprops | 描述配置属性（包含默认值）如何注入 Bean      
|get |/beans  |描述应用程序上下文里全部的 Bean，以及它们的关系            
|get|/dump  |获取线程活动的快照            
|get |/env | 获取全部环境属性            
|get |/env/{name}|根据名称获取特定的环境属性值          |get |/health|报告应用程序的健康指标，这些值由 HealthIndicator 的实现类提供            
|get |/info  |获取应用程序的定制信息，这些信息由 info 打头的属性提供            
|get |/mappings |描述全部的 URI 路径，以及它们和控制器（包含 Actuator 端点）的映射关系
|get | /metrics |报告各种应用程序度量信息，比如内存用量和 HTTP 请求计数
|get |/metrics/{name}  |报告指定名称的应用程序度量值 
|post | /shutdown |关闭应用程序，要求 endpoints.shutdown.enabled 设置为 true（默认为 false）
|get |/trace  |提供基本的 HTTP 请求跟踪信息（时间戳、HTTP 头等）         

> **除了/info,/health默认是开启的,其他都是关闭的**
`management.endpoints.web.exposure.include=*` 使用这个配置可以打开所有端点(这些链接官方叫endpoint,翻译过来叫端点)
还有一个配置,表示"除了",一看就懂对吧
`management.endpoints.web.exposure.exclude=env,beans`


# 3. 安全
在 Actuator 启用的情况下，如果没有做好相关权限控制，非法用户可通过访问默认的执行器端点（endpoints）来获取应用系统中的监控信息。
## 3.1安全隐患:
- **认证字段的获取以证明可影响其他用户**:    
这个主要通过访问`/trace` 路径获取用户认证字段信息，比如如下站点存在 actuator 配置不当漏洞，在其 trace 路径下，除了记录有基本的 HTTP 请求信息（时间戳、HTTP 头等），还有用户 token、cookie 字段：    
- **数据库账户密码泄露**:   
通过其`/env` 路径, 由于 actuator 会监控站点 mysql、mangodb 之类的数据库服务，所以通过监控信息有时可以拿下 mysql、mangodb 数据库；
- **git 项目地址泄露**:  
这个一般是在`/health` 路径，比如如下站点，访问其 health 路径可探测到站点 git 项目地址：     
- **后台用户账号密码泄露**:  
这个一般是在`/heapdump` 路径下，访问/heapdump 路径，返回 GZip 压缩 hprof 堆转储文件。在 Android studio 打开，会泄露站点内存信息，很多时候会包含后台用户的账号密码（包含漏洞的图片暂时没得，大家记住思路就好了..），通过泄露的账号密码，然后进入后台一番轰炸也不错的。   
来自:https://www.freebuf.com/news/193509.html

## 3.2 措施
引入spring-boot-starter-security依赖
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

在`application.properties`中指定actuator的端口以及开启security功能，配置访问权限验证，这时再访问actuator功能时就会弹出登录窗口，需要输入账号密码验证后才允许访问。
```
management.port=8099
management.security.enabled=true
security.user.name=admin
security.user.password=admin
```