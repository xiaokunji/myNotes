&emsp;&emsp;随着服务的越来越多，越来越杂，服务之间的调用会越来越复杂，越来越难以管理。而当某个服务发生了变化，或者由于压力性能问题，多部署了几台服务，怎么让服务的消费者知晓变化，就显得很重要了。不然就会存在调用的服务其实已经下线了，但调用者不知道等异常情况。   

Eureka是Netflix开源的服务发现组件，本身是一个基于REST的服务。它包含Server和Client两部分。Spring Cloud将它集成在子项目Spring Cloud Netflix中，从而实现微服务的注册与发现。

&emsp;&emsp;著名的CAP理论指出，一个分布式系统不可能同时满足C(一致性)、A(可用性)和P(分区容错性)。由于分区容错性在是分布式系统中必须要保证的，因此我们只能在A和C之间进行权衡。在此Zookeeper保证的是CP, 而Eureka则是AP。Zookeeper保证CP
> 来自 <https://blog.csdn.net/qq_38363255/article/details/80909731



**Eureka服务端**   

​		也称为注册中心，用于提供服务的注册与发现。支持高可用配置，依托与强一致性提供良好的服务实例可用性，可以应对多种不同的故障场景。

**Eureka客户端**  

​	主要处理服务的注册与发现。客户端服务通过注解和参数配置方式，嵌入在客户端的应用程序代码中，在应用程序启动时，向注册中心注册自身提供的服务并周期性地发送心跳来更新它的服务租约。同时，它也能从服务端查询当前注册的服务信息并把它们缓存到本地并周期性地刷新服务状态。


![Eureka总体架构](http://qiniu.xds123.cn/18-9-4/69602661.jpg)

从这个简图中，可以看出，Eureka有三部分组成：
- Service Provider： 暴露服务的提供方。
- Service Consumer：调用远程服务的服务消费方。
- EureKa Server： 服务注册中心和服务发现中心

服务端:
```xml
 <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-eureka</artifactId>
    </dependency>
```


```properties
eureka.instance.instance-id=${spring.cloud.client.ipAddress}:${server.port}
eureka.instance.prefer-ip-address=true
server.port=8766
eureka.client.serviceUrl.defaultZone=http://10.101.90.171:10000/eureka/
# 指定服务注册中心地址 这里直接指向了本服务
#eureka.client.service-url.defaultZone=http://${eureka.instance.hostname}:${server.port}/eureka/
```
> 注: 默认不写时，是注册至：DEFAULT_URL中,默认就是http://localhost:8761/eureka。

```java
/**
* Eureka服务端
* @author oKong
*
*/
@SpringBootApplication
@EnableEurekaServer
public class EureakServiceApplication {
    
    public static void main(String[] args) throws Exception {
        SpringApplication.run(EureakServiceApplication.class, args);
        log.info("spring-cloud-eureka-service启动!");
    }
}
```
启动应用，访问：http://127.0.0.1:1000/,  就能看到注册中心界面(此时没有客户端,所以应用列表为空)

客户端:
	(关于Eureka)配置与服务端一样,
```java
	/**
	* 服务提供者示例-eureka客户端

	* @author oKong
	*
	*/
	@SpringBootApplication
	//注意这里也可使用@EnableEurekaClient
	//springcloud是灵活的，注册中心支持eureka、consul、zookeeper等
	//这样在替换注册中心时，只需要替换相关依赖即可。

	@EnableDiscoveryClient
	public class EurekaClientApplication {
	
	    public static void main(String[] args) throws Exception {
	        SpringApplication.run(EurekaClientApplication.class, args);
	        log.info("spring-cloud-eureka-client启动!");
	    }
	
	}
```
>	~~注意：这里也可使用@EnableEurekaClient注解，但一般不这么用，直接使用@EnableDiscoveryClient实现自动发现。因为SpringCloud本身支持Eureka、Consul、zookeeper等实现注册中心功能，若写死了某个注册中心的相关注解，之后替换时，还需要修改注解类。源码中,注解@EnableEurekaClient上有@EnableDiscoveryClient注解,可以说基本就是EnableEurekaClient有@EnableDiscoveryClient的功能,但是注释写明@EnableEurekaClient是一个方便使用eureka的注解而已,至于为啥就不清楚了,~~

>来自 <https://www.jianshu.com/p/f6db3117864f>    
>
>来自 <https://blog.lqdev.cn/2018/09/06/SpringCloud/chapter-two/> 

**Eureka自我保护模式**  

​	默认情况下，如果Eureka Server在一定时间(30s)内没有接收到某个微服务实例的心跳，Eureka Server将会注销该实例（默认90秒）。但是当网络分区故障发生时，微服务与Eureka Server之间无法正常通信，这就可能变得非常危险了，因为微服务本身是健康的，此时本不应该注销这个微服务。  

&emsp;&emsp;Eureka Server通过“自我保护模式”来解决这个问题，当Eureka Server节点在短时间(15分钟)内丢失过多客户端(85%)时（可能发生了网络分区故障），那么这个节点就会进入自我保护模式。一旦进入该模式，Eureka Server就会保护服务注册表中的信息，不再删除服务注册表中的数据（也就是不会注销任何微服务）。当网络故障恢复后，该Eureka Server节点会自动退出自我保护模式。   

自我保护模式是一种对网络异常的安全保护措施。使用自我保护模式，而让Eureka集群更加的健壮、稳定。
开发阶段可以通过配置：``eureka.server.enable-self-preservation=false`关闭自我保护模式。

具体的配置参数，可至官网查看：   

http://cloud.spring.io/spring-cloud-static/Finchley.RELEASE/single/spring-cloud.html#_appendix_compendium_of_configuration_properties  

配置字段中文说明，大家可查看网站：微服务架构：Eureka参数配置项详解，里面很详细的说明了。


**Eureka 如何获取IP?**

1. 从`eureka.instance.ip-address`获取,如果没有设置则取自动获取`EurekaInstanceConfigBean instance = new EurekaInstanceConfigBean(inetUtils);`
2. InetUtils工具类会获取所有网卡，依次进行遍历，取ip地址合理、ipv4地址,不是回环地址(127.0.0.1),索引值最小且不在忽略列表的网卡的ip地址作为结果。如果仍然没有找到合适的IP, 那么就将`InetAddress.getLocalHost()`做为最后的fallback方案
> 何谓合理?:   
>
> 设置了仅使用本地接口的或者是首选地址(支持正则)    
>
> 类: `EurekaClientAutoConfiguration`, `InetUtils`,`org.springframework.cloud.commons.util.InetUtils#findFirstNonLoopbackAddress`   
>
> http://www.manongjc.com/detail/15-ziuyupkzzzamoqr.html    
>
> https://www.cnblogs.com/orangesea/articles/11300266.html






Eureka高可用详见:
来自 <https://blog.lqdev.cn/2018/09/06/SpringCloud/chapter-two/> 



---


