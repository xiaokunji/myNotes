[toc]

图片: [流程图](#processImg) ; [过滤工厂架构](#filterFactory)


# 1.介绍
&emsp;&emsp;Spring Cloud Gateway 是 Spring Cloud 的一个全新项目，该项目是基于 Spring 5.0，Spring Boot 2.0 和 Project Reactor 等技术开发的网关，它旨在为微服务架构提供一种简单有效的统一的 API 路由管理方式。

&emsp;&emsp;Spring Cloud Gateway 作为 Spring Cloud 生态系统中的网关，目标是替代 Netflix Zuul，其不仅提供统一的路由方式，并且基于 Filter 链的方式提供了网关基本的功能，例如：安全、监控、埋点和限流等。

Spring Cloud Gateway 的特征：

- 基于 Spring Framework 5，Project Reactor 和 Spring Boot 2.0
动态路由
- Predicates 和 Filters 作用于特定路由
- 集成 Hystrix 断路器
- 集成 Spring Cloud DiscoveryClient
- 易于编写的 Predicates 和 Filters
- 限流
- 路径重写


## 1.2**术语**
- **Route（路由）**：这是网关的基本构建块。它由一个 ID，一个目标 URI，一组断言和一组过滤器定义。如果断言为真，则路由匹配。-> 理解为分发(改变)路径
- **Predicate（断言）**：这是一个 Java 8 的 Predicate。输入类型是一个 ServerWebExchange。我们可以使用它来匹配来自 HTTP 请求的任何内容，例如 headers 或参数。-> 按指定的路径拦截
- **Filter（过滤器）**：这是org.springframework.cloud.gateway.filter.GatewayFilter的实例，我们可以使用它修改请求和响应。-> 拦截下的路径可以做一些操作

## 1.2**流程:**

![img](http://favorites.ren/assets/images/2018/springcloud/spring-cloud-gateway.png)

客户端向 Spring Cloud Gateway 发出请求。然后在 Gateway Handler Mapping 中找到与请求相匹配的路由，将其发送到 Gateway Web Handler。Handler 再通过指定的过滤器链来将请求发送到我们实际的服务执行业务逻辑，然后返回。
过滤器之间用虚线分开是因为过滤器可能会在发送代理请求之前（“pre”）或之后（“post”）执行业务逻辑。
> <u>请求会两次经过filter -> 进来时和出去时</u>


# 2.使用
pom.xml
```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```
> 注意,web和路由不能同时引进
## 2.1 路由
路由是用来分发的,这是基础,后面的过滤器和断言器都是为路由服务的.  

Spring Cloud Gateway 网关路由有两种配置方式：

1. 在配置文件 yml 中配置
2. 通过`@Bean`自定义 RouteLocator，在启动主类 Application 中配置

这两种方式是等价的，建议使用 yml 方式进配置。

application.yml
```yaml
server:
  port: 8081
spring:
  cloud:
    gateway:
      routes:
      - id: neo_route
        uri: http://www.ityouknow.com
        predicates:
        - Path=/spring-cloud

logging:
  level:
    org.springframework.cloud.gateway: trace
    org.springframework.http.server.reactive: debug
    org.springframework.web.reactive: debug
    reactor.ipc.netty: debug
```

各字段含义如下：

- id：我们自定义的路由 ID，保持唯一
- uri：目标服务地址
- predicates：路由条件，Predicate 接受一个输入参数，返回一个布尔值结果。该接口包含多种默认方法来将 Predicate 组合成其他复杂的逻辑（比如：与，或，非）。
- filters：过滤规则，本示例暂时没用。
> routes下可以有很多id,predicates下可以有很多过滤工厂,

上面这段配置的意思是，配置了一个 id 为 neo_route 的路由规则，当访问地址 `http://localhost:8080/spring-cloud`时会自动转发到地址：`http://www.ityouknow.com/spring-cloud`。

转发功能同样可以通过代码来实现，我们可以在启动类 GateWayApplication 中添加方法 customRouteLocator() 来定制转发规则。

```java
@SpringBootApplication
public class GateWayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GateWayApplication.class, args);
	}

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("path_route", r -> r.path("/about")
						.uri("http://ityouknow.com"))
				.build();
	}

}
```


上面配置了一个 id 为 path_route 的路由，当访问地址`http://localhost:8080/about`时会自动转发到地址：`http://www.ityouknow.com/about`和上面的转发效果一样，只是这里转发的是以项目地址/about格式的请求地址。

上面两个示例中 uri 都是指向了我的个人网站，在实际项目使用中可以将 uri 指向对外提供服务的项目地址，统一对外输出接口。

> 其实不一定写在application里,写到任意一个会被加载的类中都可以,@component 放在类上就会被加载

## 2.2 断言器

Spring Cloud Gateway 是通过 Spring WebFlux 的 HandlerMapping 做为底层支持来匹配到转发路由，Spring Cloud Gateway 内置了很多 Predicates 工厂，这些 Predicates 工厂通过不同的 HTTP 请求参数来匹配，多个 Predicates 工厂可以组合使用。

application.yml
```
spring:
  cloud:
    gateway:
      routes:
       - id: host_foo_path_headers_to_httpbin
        uri: http://ityouknow.com
        predicates:
        - Host=**.foo.org
        - Path=/headers
        - After=2018-01-20T06:06:06+08:00[Asia/Shanghai]
```
> predicates下的配置使用的都是断言工厂
- Host -> HostRoutePredicateFactory

- Path -> PathRoutePredicateFactory
  只需要写前缀,(后面的RoutePredicateFactory不用写,也不能写);   

  <u>springcloud是把所有的断言工厂加载进来,然后得到类名,去掉后缀作为key,类对象作为vaule,然后拿着配置文件中的值去get出值</u>
```java
源码:
############
# 得到key
# org.springframework.cloud.gateway.support.NameUtils.class
public static String normalizeRoutePredicateName(Class<? extends RoutePredicateFactory> clazz) {
		return removeGarbage(clazz.getSimpleName().replace(RoutePredicateFactory.class.getSimpleName(), ""));
}   
############
# put进map,存起来
# org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator.class
	public RouteDefinitionRouteLocator(RouteDefinitionLocator routeDefinitionLocator,
			List<RoutePredicateFactory> predicates,
			List<GatewayFilterFactory> gatewayFilterFactories,
			GatewayProperties gatewayProperties, ConversionService conversionService) {
		this.routeDefinitionLocator = routeDefinitionLocator;
		this.conversionService = conversionService;
		initFactories(predicates);
		gatewayFilterFactories.forEach(
				factory -> this.gatewayFilterFactories.put(factory.name(), factory));
		this.gatewayProperties = gatewayProperties;
	}  
############
# 根据名字取值
# org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator.class
	@SuppressWarnings("unchecked")
	private List<GatewayFilter> loadGatewayFilters(String id,
			List<FilterDefinition> filterDefinitions) {
		List<GatewayFilter> filters = filterDefinitions.stream().map(definition -> {
			GatewayFilterFactory factory = this.gatewayFilterFactories
					.get(definition.getName());
			if (factory == null) {
				throw new IllegalArgumentException(
						"Unable to find GatewayFilterFactory with name "
								+ definition.getName());
			}
		# ... 后面还有很多,不粘贴出来了
}
```

## 2.3 过滤器
Spring Cloud Gateway 的 Filter 的生命周期不像 Zuul 的那么丰富，它只有两个：“pre” 和 “post”。

- PRE： 这种过滤器在请求被路由++之前调用++。我们可利用这种过滤器实现身份验证、在集群中选择请求的微服务、记录调试信息等。

- POST：这种过滤器在路由到微服务++以后执行++。这种过滤器可用来为响应添加标准的 HTTP Header、收集统计信息和指标、将响应从微服务发送给客户端等。
  Spring Cloud Gateway 的 Filter 分为两种：

- GatewayFilter 

- GlobalFilter  

  GlobalFilter 会应用到所有的路由上，而 GatewayFilter 将应用到单个路由或者一个分组的路由上。GatewayFilteryer加上配置后也可以变成全局过滤(还不如直接写成全局过滤器呢)
### 2.3.1 GlobalFilter
直接上自定义的   

```java
package com.example.demo.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component  // 让框架加载这个类
public class TestGlobalFilter implements GlobalFilter, Ordered {
// 继承 GlobalFilter 就是全局过滤器了

	private static final Logger log = LoggerFactory.getLogger(TestGlobalFilter.class);
	
	// 过滤器的优先级,数字越大优先级越小
	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

    // 可以在这里干很多很多事情,比如验证请求头信息
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		log.info("这是gateway-filter,{}",exchange);
		String value = exchange.getRequest().getPath().value();
		System.out.println("过滤器...."+value);
		List<String> name = Optional.ofNullable(exchange.getRequest().getHeaders().get("name")).orElse(new ArrayList<String>());
		System.out.println("过滤器....头信息-name:"+name);
		if (name.contains("xkj")) {
			ServerHttpResponse response = exchange.getResponse();
			response.getHeaders().add("name", "xkj");
			byte[] datas = "非法请求".getBytes();
			DataBuffer buffer = response.bufferFactory().wrap(datas);
			return response.writeWith(Mono.just(buffer));// 不正常情况时返回
		}
		// 这里只是对进来的链接处理了,在后面加then(mono)方法就可以处理出去时的链接,mono就是具体的操作,可以用来记录链接使用时间等
		return chain.filter(exchange);
	}
}
```
> 过滤通过后会经过路由拦截并转发啦

### 2.3.2 GatewayFilter
直接上自定义的
```java
package com.example.demo.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.StripPrefixGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
// 继承GatewayFilter 就是过滤器了
public class TestFilter implements GatewayFilter, Ordered {

	private static final Logger log = LoggerFactory.getLogger(TestFilter.class);
	
	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 0;
	}
    
    // 和全局的差不多
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		log.info("这是gateway-filter,{}",exchange);
		String value = exchange.getRequest().getPath().value();
		System.out.println("过滤器...."+value);
		List<String> ages = Optional.ofNullable(exchange.getRequest().getHeaders().get("age")).orElse(new ArrayList<>());
		if (ages.contains("20")) {
			ServerHttpResponse response = exchange.getResponse();
			response.getHeaders().add("name", "xkj");
			byte[] datas = "非法请求".getBytes();
			DataBuffer buffer = response.bufferFactory().wrap(datas);
			return response.writeWith(Mono.just(buffer));
		}
		return chain.filter(exchange);
	}
	
	// 写好过滤器后,就要通过路由来生效了,和前面的路由案例一样
	@Bean
	public RouteLocator customerRouteLocator(RouteLocatorBuilder builder) {
		 log.info("这是gateway-customerRouteLocator,{}",builder);
		return builder.routes()
					.route(r -> r.path("/jp1/**")
											.filters(f ->  f.rewritePath("/jp1/", "/jportal/").filter(new TestFilter()) )
											.uri("http://localhost:5766")
											.order(0)
											.id("test_route1")
					 	)
					 .build();
	}

}

```



### 2.3.3 自定义过滤工厂

前面说到,路由建议写配置,但是路由里只能写工厂,所以接下来介绍自定义过滤工厂

过滤器工厂的顶级接口是`GatewayFilterFactory`，我们可以直接继承它的两个抽象类来简化开发`AbstractGatewayFilterFactory`和`AbstractNameValueGatewayFilterFactory`，这两个抽象类的区别就是前者接收一个参数（像StripPrefix），后者接收两个参数（像AddResponseHeader和我们这种）。

![img](http://favorites.ren/assets/images/2018/springcloud/spring-cloud-gateway3.png)

application.yml
```yaml
 spring:
   cloud:
      gateway:
        routes:
        - id: jportal_route
          uri: http://localhost:5766/
          predicates:
          - Path=/jpxkj/**
          filters:
          - Test=name,xkj
#          - StripPrefix=1
#          - PrefixPath=/jportal
          - RewritePath=/jpxkj/, /jportal/
#          - RewritePath=/jpxkj/(?<segment>.*), /jportal/${segment}
  
```
> 重点解释filters里面的  

filters下面全是工厂  
  
  Test是自己写的工厂,同样只能写前缀,原理和断言工厂一样    
  
    - StripPrefix : 截断,会截断路径,数字表示截断一个(不懂就看源码)   
  
    - prefixPath  : 加前缀,会在uri后面加上这个前缀   
  
    - RewritePath : 重定向,把/jpxkj/的这段路径变成/jportal/,原理就是用了replaceAll()方法    
      
    两个RewritePath能实现同样的效果,官网案例是复杂的,我也不懂非得写复杂,还是两者有区别?   
    
      StripPrefix,PrefixPath组合实现了RewritePath的功能

工厂类
```java
package com.example.demo.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class TestGatewayFilterFactory extends AbstractNameValueGatewayFilterFactory {
//这是接收两个参数的类

	@Override
	public GatewayFilter apply(NameValueConfig config) {
		System.out.println("----TestGatewayFilterFactory:"+config);
		return new TestFilter();// 这是前面写好的过滤器
		// 也可以现场写一个
//		return (exchange, chain) -> {
//			ServerHttpRequest request = exchange.getRequest().mutate()
//					.header(config.getName(), config.getValue())
//					.build();
//
//			return chain.filter(exchange.mutate().request(request).build());
//		};
    }

}

```

## 2.4 加入注册中心
前面使用的路径全是指定的,在生产中,肯定要加入注册中心
`application.yml`

```yaml
# 增加配置
spring:
   application:
      name: gateway-server-test
   cloud:
      gateway:
        discovery:
          locator:
            enabled: true
eureka:
  client:
    service-url:
      defaultZone: http://10.101.90.171:10000/eureka/

```
> 加入配置后,网关中心会注册到注册中心,注册中心里所有的应用都会成为uri,名称都是大写,这个要注意

pom.xml
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

启动类加上注解`@EnableDiscoveryClient`

路径用`lb://${appName}` 就可以了,
application.yml就是这样了
```yaml
 spring:
   cloud:
      gateway:
        discovery:
          locator:
            enabled: true
  #          lowerCaseServiceId: true # 应用名变小写
        routes:
        - id: jportal_route
          uri: lb://JPORTAL-XKJ
          predicates:
          - Path=/jpxkj/**
          filters:
          - Test1=name,xkj
```























---



<div id="processImg">

[processImg]:data:image/png;base64,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caJNlW6yySY2VcqIgxENIz6ec0Hftm3bsPrqqxt26PDSSy+F1VZbLay99topnkynMgJdaaWVrFx9+emnn8LIkSNtCrFZs2Y2imFU+corr4SZM2caP9OLbdq0sTTtI79OnTphm222SUduaog+wb/TTjvZNKemE+Gj38h99tlnw5577hlOPfVUY4NGo0AKbr/9dsPiySefNBrqpC/15N97772w1FJLBXBBNvX0/csvvwzDhw+3aVLw3HXXXa0cPmiYGkbHHXfcMUydOjW8/PLLRrvqqquGDh06GC3yhg4dGn788UfDA3nLLbdcWH/99U1f/3EE8h6BXHhMl1GcCFT0bpy7/Ndff93u+A8//PAFgqERw6hRo+ajmThxYrLtttsaP23qWHrppZObbrrJaPv3758sueSSaV2tWrWSoUOHWt2IESOSddZZx+oohx/anj17piOqNddcM1lvvfVsZKbGBwwYYLRt2rSx8nnz5lnV7bffbuVvv/228ffu3TtZaqmlEskmXmKJJZKddtopmTVrltHst99+NvKaNm1a2oZGQ6uuumqy/fbbq1mL45HeP/7xD2sPfaTDfMR/ZKZOnZoWa5TLyE6BdtCJoFHyWWedldSpU8fkgwu6g8WYMWOMDj3uuOMOK7/iiiuSunXrprT0sV27dml/dF6IGWV269ZNTZcbw+PBEahJBPwKrEn087ztihgoObArr7zSDCbOSWWa8osNe9xlyqHZfffdzXg+9dRTVi3n2bRp06R+/frJTz/9lEyfPj157bXXzBDjHMaPH58wLfrdd98lGPmVV145ET8O6G9/+5s5pGuvvdb0Ofvss433q6++So338ccfb2U4xpkzZ6aqoc/qq69ufEy3gsOhhx6afPvtt0YzZcoUM/SU33bbbVYmx0l7BGHw/PPPGy633HJLOlWoOmKcW4cOHVLd4KX/cpTksw6Q+nHjxhnPUUcdZW0hCyeGLPFedtll1vbRRx+doDOBm5IVV1zR+odjhu+uu+4yWWB9zz33GBY4086dOxuG6I5MyrbZZhsrA3/4Kxoqci1VVJbTOQKLg4A7u8VBrUR4KmqgMJinn366GUxGW+Tj8O677yYvv/xy8uKLL9oxbNiwhAM6nsftueeeyTnnnGMsMtRkTjvtNJPJ8ymCRjOMtAjwY9DRk+eFCmofw4zDxFm88MIL5lD/+9//iixZ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WDqSVzqmRV6scyxX9JIlumyf4jxpDs4lsYLkIoOgevKcl1gn8ag95Qs1pu8xFoXaD9fbEcg3BHL936rQM7vKgIAhwODJIDJqIGAQCRg9GQuVQU+ZysmrDB7JEg11GG/Ry+gSQ0ud+LJllIsPoxwH0aoM/SSbMulBmcoZERIkExrRUY4M5cWTpUUPyqQ3o16lFVNPv9WWZAoLyikjT8wR6yle6CgX9soTowd8apM0dOQlEzoF9U31agP90Vd9IobfgyPgCDgC1YVAlTs7jNqNN94Yttxyy9CqVauw6aabhp49e4aZM2ea8csaSBlFlWOIKSPIQLZt2zZMnjw5NbgYVxl16OBVjMGW0UUOdchUGXQy5pTdcMMNYYsttgi8hHT99dcPvXv3Nno5wl122SV88MEH5gjEi0wOyZGToJ62COiufkgWPJTR7qGHHhreffddy8s5UC6Z3377bTjllFPCxhtvHFZfffWwySabhD59+gRGksiWLNpS35DNgQziDz/8MPz4449pXnXwEqCTLPJZPaAXdpKpcxL3U+3TT+mPPGhVRxp5HhwBR8ARqBYEkgqE33777S9Uvw9e/lL8l4Lbbrstad68eXLfffclY8eOTV588cVkq622Sg4++OAEub/88kvKo3YUq075n3/+2Wj79OmTfP/99ykfCdGQ/vXXX+fLU4YsaGK6OA3PQQcdlKy99trJI488kowfPz555plnktatWyf77beftYUM+k0fFNSWZClWvfLorrTqFFO3+uqrJ8OGDVORxeo/ujRr1swwo+3PP/88efLJJw3H3XbbzWjRg4NAO0qTl5xatWoZrxFlfmL6WFZMtiD9xasYHp0reMQ3b968VFxMmxYWYKKi/4MC7Jqr7AjUKAK5/m9xd11ukLGKCSuqyKGHHpqcf/75KSuy3nrrLTPuP/30k5W/+uqrCennnnsuGTBgQDJt2jQrh/azzz5LJkyYkLz++uvJ0KFDzXDiFGbPnm0HZRhWeB966KH5nCAGderUqckDDzxgjgQHiZw4qG84jzp16iRffPFF2jaJTz/9NKlXr14yevRoK19iiSVMDzI4EegffPDB5Nlnn03mzJljNPyMGjUq+b//+z/Lo8eMGTOs7djIows3Ad99913SsmXL5JVXXkmdciooSZIDDzww6dSpkxVJX+RMnjw5ady4cTJmzJjUoXz77bfJo48+an0eMWJEWv7aa6+Zo77//vuTH374wZwhzgfc7r333uSrr75KHSS08c0EeoIjQecPGYQff/wxefrpp5M777wzGT58uJWh28iRI5Nx48ZZXjp/8MEHhleMgREU8E9F/wcF3EVX3RGoEQRy/d+qcmd3ySWXJK1atbJR0qxZs1LQZAApYM+i6W4AACAASURBVFTTrl27BMfIwSgGJ0Po3bt30r59ezPq0M2cOdOMNk4GGkYrO+64o43KNt98cxtF4jwIGOmmTZsm+++/f9KxY0cbpeFU1LZiaI855pjkgAMOSOs0GqIupuMEDBkyxORff/31Jr9bt25JmzZtkvXXXz+ZNGmS1VHWvXt3S/PDiKxFixZpvmvXrslaa62VQLfBBhsk9evXN4cctwUxmNWtW9ecacr8x+iVPE5fPIMGDUqWW265pEuXLtY2Tvrqq682tn/84x+GG6PXjz/+2PSk3R122CFBl+WXX95GtBDvsssuybXXXmt8OD36fOmll1oep81NAf3EoXOuGPnSj5VXXtli9Onbt6+dF5hwbui5wgorJDi8GFsTWsA/uf5DFjAUrrojkFMEcv3fqnJnx6jrjDPOSBo1amRGcqeddkquuOKKdKQAOjggaGQEe/bsaY4HI9mrV69kxRVXNKOvqTFAYGpv4sSJZogffvhhA5mR1TrrrGOjDAq22GILM7o6AyeccII5XvJyEErjUC+88MJ0dCMexdCjH86V0eTXX39t6dh442QOP/xwk3HUUUclPXr0MHZ4GY3i3OjT4MGDzfDjOAg4n9q1aycvvPBCSm+JJLGRLf1lhKtAGof70ksv2YEuhNNOOy256667rG+M2m666aakQ4cOiaYPkcMoGRyPPfbY5JBDDkn7++abb9o5Qqd+/fole+yxh8kBW25WcOaEgQMH2o0JaRziueeem05Z0seGDRsaHSNFRsFffvml5R9//PFks802S2kpLIYRXq7/kAaW/zgCjoDZ9lzCUOULVFjkcPnll4cpU6aEQYMGBRaX3HLLLWHHHXcMs2fPtgUPLG44+OCD08ULHTp0CC+//LItulh66aWNBzkcCiyq0GIJFmsQWAm54oor2kKIn376yRaS7L///ukiEdLxggktkGCxBLJZvKHFGlo1Khpi6jhIo996661nC0VYdIEuPXr0SPXWYg9okU+93iv65ptvhnbt2oWGDRuarLXXXtsW8KC/FreoXXgJ8Co8+uij4dhjjw1du3YNO++8cxgyZIjxXXvttWGbbbYJjz32WCB95513Wn+FG3oil/ywYcPCnDlzwqWXXhrOP//88Oyzz1rde++9Fzp27Gj9oP75558Pp512mi2eQYeBAweGzp07myqUn3DCCXZer7766tC3b1/DkMqVVlop7LbbbuGOO+4wucTdu3f/yzlUnzx2BBwBR6AqEahyZ8eKwenTp4dlllkm4MQwrO+//36YNGlSGDx4cOpcmjZtmjqiunXrBpwVAeNfr1699N2cMRg4EJwKb0shyJCTxlDL4cgxIhceOSA5EuhZ5ThixIjUMUomNP/85z9NZ9KSOWvWLNNL7VKHsxIfbcpxiQZnIxn0CV3Ic4APKytxRHJ08DVv3tyc4jvvvEPW6k4//fTw6aefhokTJ9pNg/hYObrtttuGhx9+2G4ccKhydMhEd8mGZ9lll7WXFQsX5LZo0SKstdZatnL29ddft3PUqVOnsPnmm4fhw4eHF198Mey7776my+233x423HDDcM899wTwaNOmjZXzQ/+POuqocN9994XvvvvOnCYrTgnoID1SBk84Ao6AI1CFCFS5s2MUd+utt6bGDWcgo9+gQQPrGsYew0o54aOPPjLjijMgyGArTxnGUvTk5Vig4Vh++eXNcOMkyGN8Gc2obfgx/gRiDDOG/NVXX011ReYzzzwTrr/++tC4ceP52sTIjxw5Mvzwww/mrJDz1ltvhRVWWMHag54gnb/++mvjp122YDC6QyfyjCLHjh2bOnQ5QOrAi1HchRdeaE6DOvHgsL755psUn4suuig8/vjj5mBw0Mstt5zVSx76IA+d1lxzzdCyZUvbvnDeeeeFM844w25AcLrI33vvve284QhxfjvssEP417/+FThn66yzjvXtiiuuCJdcckl48MEHQ69evULr1q2tHH7CPvvsE6ZOnWptMFpk1E0QJpbxH0fAEXAEqgGBP+cFq6gxHB3TXozmuPNnf90DDzxgIwKmMjF8OKCzzjorMGVJPUYbA4ozwNHhjKDDaCtgUGXEqZNDpAx58GCITzrppDBhwgRzSjje+vXrmwj45SyJ2VeHQ9ljjz3CySefbHlGT//5z38CRh0HBQ9yibfaaitzAAceeGA4++yzw7hx48wZDBgwwPTebrvtzIEyzQjPNddckzq3Qw45xHQ78sgjbeqTNqZNm5bqFfeTQkbHjDqZNj3iiCNs/98nn3xiIyr6s/XWW5tO6Mh0Ic7z448/tj2DjDZpnz7iuHBYOLZzzz3X9vbhgHBS7C/8/PPPzVnT/l577WVyjzvuONMLXJimBCMCmDP9ytQ008jwoifOlPY4H7TZpUuXcNNNN4WHHnrIzpfONzKklwn0H0fAEXAEqhCBKh/ZsQkbR7fyyiuHl156KYwePTpgQF944QUz/jg0jPPFF19sU11Dhw4NOAxGEjiV7bffPhx00EFGKyeAUW3SpElo1KhRuOCCC2w6Thh169bNpiQxtDiGJ554wqY0V1llFWuD0Y4ChheDq8A0IMYbh/vUU0/ZSIfnX4ySoKN9aJjqIzCK4rlUv379bJT35JNPBvpLYKqPzfSU4aiQw6ZwOQKmBJmixMngWG6++WbbLK4+0nfS6Mho6rnnngt33323PbtD5owZM8KVV14ZRo0aZY4ZenSHFudJPc8VDz/8cJODTv379zd+phVxwujERnP032yzzYwe3AhMW/I8j3OFbBwqjg55Ctw8MMKlD2PGjLHROTSM5gjoj5NkunTPPfe0Mp1vMmrLKvzHEXAEHIEqRKBa3o2JwdaUnfoio04e53HXXXfZ8ycZedERi19peGN+ldOG0tQzFXfqqaeGXXfd1WSwoIJpR0Y/ooWe6UpGIrQTy43TJviP6VPKpad0K2uUAg0BetGRF6/iuC7bjvLiI87qJX50oF/Z9qinLTkX9TeWDQ18MW9cFtNKb5Upr1jlxDh4nk+ySAnZopHOMW0hptWnQtTddXYE8hmBXP+3qnwaEzDlWFA+GzB+eo5EnYwhMQEe8ZOP0zE95TL2pOFnRMgqTxbGMJXJohVGetTHxlZToFnZpkDmR31QLB45kphcNJSJjrTKFcd14led8jFfXEZaWMU6IFN9zMpXf2M5MY3SimM60mXpli3neSYjVjBnZA+Pzm1ZNwbZNvIlX5bOulnQzYBwRmf1TXz50o9C1UPYKlY/snmVe+wILAiBahnZLahxlfO8hyk9jLUM7KIYC134imVwiHmnpBZ/8M5LjeBoZ1HakK75FsvwSi+mhLl5qK4gzNUemHIQXnnlFXu+qEU7OLzYUeo8iTef42w/WU282mqrpdPbTB9zHTNlqz5mefK5f/msW/aa1jUmW5HPurtui4+AbpAXX8L8nDXu7GQQso5ncQyhZNHF2AlkZRfbn0VYxf1nTxwLfqor0HbszGLMpZfKyBMKwVhJd/SN+0i6WbNmtkKWlb8samLRj0bX6mt14V8K7YA5uApjXfel0PdS7GOunV2VL1Ap7yTFBo+LWYELmgu7vAAPFz2xRmvwMIKjHBmSAw2H7rzLk10I9fQHrNR/dCZdnY6ONuPzSD7GnjpuPnQewD9Ln69Yoyd9UR917RDvvvvuhjV7DHF2MsL0k3rx5WvfCkEvYUjMuRDGXE9KF0I/XMeaR6DGnR0QYJwxEGUZxfIg0h+AmICR4Y9A4M9AnjrJJy862i30oL6pX+on/SJdHUE4ooOMEzcbWZ3icySe6tCvMm2AYWxU1T/6xt5Bwoknnpjuw4zbivnick9XHAFhqFjXjfIVl+SUpY5AjU9jcgIwKLFhJI3DIi7vosb4iAaeshZf6CSLtjw60RdCDHYcciQYg6wDrO5+oA9BusTnlvI4r3R161jR9tQX0aMvZcRs6GevIYuf2AqjvqhePB4vPgJ6XqebDP7r+h8vvlTnLAQE9F/Lla55MbKjM7pjo4Nc4DgtObGFdRYa8crR8WdQmYwVseRBR151C5Of73XgJedGf2KnVx26ywjFbaETB3gTQ4NupIk18i6LN5aTD2npT8zBdaXrhoU37I9k7yZ1BN2kFULf8gHf8nTQYiuuJf3X9T/WeShPhtc7AiBQ4yO72AiaQtH2BAyLjPeCTpdoJKcsurgOI6Q/S1m0hVpGH9ngzaZ9gowv5VUZwFKGnTaF9YLahQYeOYUF0VWlzosqO+4j16NupCQnW095lka0HucGAd4mpGs9NxJdSr4hIHuSK71q3NnlqiMu53cHVwjOw8+VI7AoCOgGKubJtSGMZXs6PxDI9TnOm2nM/IDXtXAEHAFHwBEoRgTc2RXjWfU+OQKOgCPgCMyHgDu7+eDwjCPgCDgCjkAxIuDOrhjPqvfJEXAEHAFHYD4E3NnNB4dnHAFHwBFwBIoRAXd2xXhWvU+OgCPgCDgC8yHgzm4+ODzjCDgCjoAjUIwIuLMrxrOa4z5VZO+eNlGLVjGqxHVllateasc0Kotj1SumTumsLPGpXHTaCB+XU6f6WKbKsjxZ2eSRJzrVK5+Vrzakg+iVF73ap16yxCse0WRj1Yte9crH9UrHbaiMmHLxxzpSp3w2jvlJi19xtt7zjkBVIeDOrqqQLSK5sWEiLYNGWofedMNGUAJxTCc4VI7hFI9kQE9aMsSjWHSqV4ws0tRLptqGl7TKoSHPW09ULlpkSCZlSsc8yENW7BDI84o71Um22o7fsCKZ1ElvY/zjVWPIka6USyd0gB5ZBOksXslVufLUS9dYlvhiWfDSjtoQDWW88SYuR0fK1Y50Jk+56uP2JY/PT0EnXVXusSNQlQi4s6tKdItENoZL77PESMmwzZgxI3zxxRfhxx9/tJ5i5HRQIDqMpPhVThm0BNLI1SEjqPo4hoYQ00h+XBfzSA/4SHPI0ci5xLzIFo/SyqsftCkdkME7HMnHjkXtUY4+tBHrJb2RDQ3vbJWcLD280COfOngokw7CBBql1a7KKFe/ScNPXu1LJnVy3qQJeu8saWGg9tVn6RzrBL3ahw8efX6K9jw4AtWFgF9t1YV0gbaDIZMhpgvjxo2zT9rwle7GjRuHFi1aWLzpppuGG2+80YykjCD0Mu56+TZlMo7QqZ42KH/yySfN+MpwQg8dITaOKlNbyJdc6DCwyBRdth3qqdOLhq2ByBmShwdZkkuZjD6GW/ogQ7Qy7OL/6quvwlprrRW6detmTUCHPNomRt4bb7wRWrZsGc444wyjoY4DWmIF6GP5lEsHpdELOso5Yv65c+f+hR95O+ywQ2jfvr01Q5tHHHFE2HXXXU1OjCEEyENnOUPakE7wQk9efMQcBGFHWmVW4T+OQDUgsGQ1tOFNFDACGC6MGGHAgAHh2GOPDdOnTw+77LJL+Nvf/hZWWWWV8OGHH4bHH3/cPmD64IMPhqFDh5rBk3HGAGPoMJQqk7HDWGpkxIus+ZLAXnvtZbRqV3wy3pTLiJOmHHkqkyOinKA2FVvhH85MsmMDTRny4Ee+MFAZ9WorbldyVQbN7Nmz7RNAODNCVifKZs6cGT7//PPwww8/zDddSNuSBZ30UNuUxVjQP3CmTOVK026dOnVgSessE4KNzqFT+PLLL8PEiROtPcrUb6VpXw4ePmRLT+qEc8wHr3QiLR7hQZkHR6AqEXBnV5XoFoFsOaoxY8aEI488MtSvXz+88soroU2bNmbwMWiEvn37miO84447wg033BD+/ve/m7GkXnf0GEIMNkHGDsOo0RVtrLzyyqlRjI069Morjg00ZSpXezK6alPOgvZjetUvzPBCLzraVVo8lMmYU6b8MsssY/0FB9UTix8dcULwCGtjiByj8sTIgVey1GeVEyNLfVe9ZFAe60w58qQz9KqXrFhenEaHOI+suG9qWzczykNDUDuW8R9HoIoRcGdXxQAXung5jl69etkIBGe2/fbbp46F/mG0MGD9+vULTz31lH3j7bTTTpvPeE+dOjUMHz7cRoU4zM033zysscYaBg8LFhhNYHR/+umn8Nlnn4UVV1wx1KtXz9rBWDId+Pbbb5sRp/3VV189hfbrr782J7Dqqqumz5PQGx4cafPmzedzDnxstVGjRvYdOgw6B7KZokWHddZZJ2y55ZYpz+TJk63vrVq1SvtEfzHe33//vR3oIxxkxKln6jA28qQ5ZPBpT1OC6EGQoyL9f//3f2HkyJGGC/3gJqNBgwapbt99913g2SnTyegybNiwMGvWLDtH4ItM2iBGLw5G3mCzwQYbhM0228xkqT84XOizTpGyOXPmGC/nEtlMf3Lu9AyOeuRyHjgnnO/tttvOpmjVPnLpvzBKT6InHIGqRiCpQPjtt9/+QvX7Tdxfir2gBhGoinPy66+/Jj/++GNSt27dZK211rLecT3omlAMHeHNN99MfvjhhxSFefPmJccff3yy1FJLJbVq1eKWPqldu3ay5JJLJueee67JGTp0qJVTt8QSS1j69ttvNxkzZsxI9tlnHyuL+Y8++ujk559/Npo99tgjWWaZZZLZs2en7Y4fP954mjRpkkg3dH3rrbes/X79+hnt448/njRv3nw++bSz1VZbJV9//bXpd+aZZ1r9q6++msongTx0a9So0Xxtx0SfffaZ8Xbo0CH55Zdf4ipLI+Oll14ymh49eqT1kyZNSnbccUfDDLzAhmO55ZZLHn300RT/Pn36GM1DDz2UNGjQIMUY2tNOOy1tg8To0aPtHMY4d+vWLVlllVWS1VdfPW27Xbt2yZprrpnmSTzyyCNJw4YNTb7O0RprrJEMHz7c+gXG9APsrrjiCsOEdho3bpzMmjXLZOk8kNF1M18jC8iURYtsD8WNQK7PcYWuGL/YCuOiyvXFoV6/+OKLZmhPOeUUK4qvBxmw2JBTL5revXubATzvvPOSadOmmYPCQG644YYmc+zYseYoJkyYYE5o6623TnBUOC5k4ySWXnrp5KqrrkpwfD/99FPSq1cv4z355JNNn1tvvdXygwYNSvW77bbbrAxM3n///dThXXLJJabPF198kdBmnTp1ks022yx59913jebzzz9PTjrpJOPFyRE++ugjy59wwgmWV1+/+eYb0/nYY49N+2sE0c+nn35qNJtuumkyZMgQcwg4hcGDByfDhg1LcPTXXXedyT/yyCNTOe3btze+Bx54IJk+fbrdcOBw6tWrl6y00kppC8IXh3vTTTdZn3Dom2yyicnEQYPjzJkzzdHVr1/fHBdltL/++usbHc5N53KnnXZKWrRokerC+cfBbb/99sl7771nbXNTs+6665oDBEfO9wsvvGDYcuNxxBFHJLfcckvy3//+N5UDY3xtpJ0oJ6FrKSarqms9bsPTNYtArs+xO7uaPZ85bT3XFwfKYQD79+9vBvHiiy9O9ZUBwllhDDHgGM/nnnvO0jgmeI855pgE40mAR44CI8iI5eGHH06NLPpj5CUb58BIQU4HGRq9HX744TZa/PLLL20EBi904j3ooIPMiVHOSIPASBBnusUWW1h+4MCByeabb276ytDDDx3GnRGO5MG3wgorJHPmzDFefm688UbD5eWXX7ayWIbSjOw0EtLIFJ044nLS3bt3Nzk4NxxLz549LS8d0KtLly7GN3HiRNPtwgsvNFnXXHON0fID/VNPPWV0F110kZXjNGkTB0TQeRgxYoSV49wUGFG2bNnSsvSDc4KTnDJlikiMf+TIkcarEeQrr7xi+TZt2qR02YT6wohf6SxNNl8WHX3xUNwI5Poc+zO7qp4nLnD5PFvhGQvPwHieo6BnQZdeemno37+/FUNH4PnPiy++GDp06GDP7/R8hnr25I0ePdoWufD8hpWI1OsZltqj7qWXXjJ52267ra1olBxoeeZ37733htdeey0cdNBB1hbbFlgoA++zzz4bzjrrLFsNiZwzzzwz8HzrrbfeCpdffrm1t88++wQO6Yxcno+99957Voa+yKLvXbt2Daecckp4/vnnw5577mn1PL9ce+21Q9u2bdNnYlTAx4E8cOJA3+uuu86ee2lBjmhoD12FAc8TX331VWtDMqT7J598YvLUjuKtt97adEUmOLGylXbh57zxLI9+7L333iaXZ3DUbbzxxmGTTTaxZ6lqCwLSBFaTwrvTTjtZWs81qeOZKguKwPrqq69O24+3MaCPngNKJmXCwBrxH0egGhBwZ1cNIBdyExh7Fl8Qs4FcQc6ue/futkcLA0vZ//73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P3z48LQd+gP9c889F7bcckvr75NPPmmx9MHhDRkyxBxfhw4d0psGtQ+/dFCZ8JIOimkvrlN5fA6ysiTTY0cgHxFwZ5ePZ6WAdMIgcmBIMYSxgcSZ/fOf/7TvvQ0ePDhsu+221jPoMMx16tQJ5557bjj66KPDG2+8EYYNG2b81MuQIo9R4KxZs9KpNRl36jDKos86DxqjTNN9WWMftwGtnpXFcpAfL/agjnbVT+IpU6akZcikLJYR60taOhNndSDfsWNHK5ezk6zp06eHt956K3Tu3Dlss802NopD71ifl156KbRr1y40bNjQdJB86H788cdUN5UTc+BkidXXOXPmwJLmqaMdzpt4yHtwBAoFAXd2hXKm8lRPGWIMN4cCjuP+++8PM2bMCBdccEFo3ry5GUnqMZgYVYwmaRzilVdeGVZffXWxhx9++CGccMIJNv1Zr149M97LL798OPXUU82IY2i7dOkSrr/+euNhNLTjjjumBv6LL74IBx54oPFh+Bs1ahROPvlkkwsDo8s111zTpgtlvPksUp8+fezr40yxKtDWaaedFvheoBzrHXfcEbbYYgtzEiuttJI57l133TUwrUpgKnKNNdawZ2Hwc4gX59iqVatw+eWXWzntE6Ahveyyy4btt9/edBSulHPDgAzaYST86aefhnHjxqUO6b333rObAuoJ8CIT/NGR6WPK2rdvb3rG7XK+oGX6FNq6deuGFVZYwUbec+fOtTrpr75YI/7jCBQKAkkFwm+//fYXqt9ndf5S7AU1iEBNnxOuk19//TVFoHPnzkmtWrWSzz77bL5yCGI68nPnzk355s2bl2y44YZJgwYNknPPPTe57bbbkltvvTVp06aNyTvvvPOMtn///knbtm2T2rVrJyeddFJyww03mNzx48cnzZs3Txo1apRcdNFFySOPPJKcf/75yRJLLJFstNFGyc8//5zMmjUrqVevXnLQQQeZLHSnfIMNNjC6Y445JtURXZG3zz77GO2VV15pNDvvvHNyyy23JLfffnty4oknJksuuWSy0korpXxbbbVVsvzyyyc//vhj2jfaueaaa/BuyRtvvJGWCw9heM4551hfP/7445Sma9euyQorrGD51157zWTceOONCXgR6D9yX331Vcsj8+CDD7ayww8/PHn44YeNZu21106WWWaZZPjw4Ub3yy+/JGussYbhDeYX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VatW/th4443d9ttv7/r37+9XNIr4ICEIJMnA9yAffvhhN2LECMdOCwSRnUi6qfZVXuWeffZZh72nn36669Spk7ItNgQMgUYQMLJrBBy7lB0EcP7jx4/3Czg0YoIkRIbEp512mvvVr35VIBuNlEQmkB8EFNbTaExIiCCj+ZxTL6xLnTfeeMPxfUsIF7KTPpJDmVBWmFZZ5ekcWy+++GI3duxYIzt1jMWGQBMI2DRmEwDZ5XQjIKKCEAj33HOP++STT9xHH33kPvjgA/fhhx+6+++/32211Vbu+uuvd3fddZcnJMpDOKov8tG55Gn0pXONBMmnDgREEEn6ky//UGebbbbxxET7BJVHjupTl3wOtRdOwSqP+tKDtN6/84LtjyFgCDSKgI3sGoXHLqYdAQgiHE11797dcUAQIglGVexdt8MOO7hbbrnFHX300YUXtEOSC2Vp1Kc8yRLZcC7iowyHykgfzrfddls3ZMiQAozkIUP1w/YphByCnsshizLE1CGmvtouCLaEIWAINIqAjewahccuph0BnD8BIuBgtANRkBYxEfPMDiKZOXOmv8704q677upefvlld9BBB7nevXu7ww8/3NdB5kMPPeT23Xdf169fP9enTx//zO2vf/2rb4vrU6ZMccOGDXMnnnhigYi4+Omnn7oDDjjAEyok9dRTT7k99tjD/elPf/Ky0Q/d/uu//su337NnT0/Eo0ePdu+9915BPgn0fvrpp307EDh7wl111VW+vkjSV7A/hoAh0CQCRnZNQmQF0o6AVliGBLBmzRpPCugOObEJLTEjPMKCBQvcCy+84CAZCAXSWblypa8zYcIEd9hhh7nZs2e7UaNGuWOPPda99tprbs8993R//OMfvRxGaxtuuKG7+eab3cSJE/3oC7mQ32OPPebGjBnjZUGuPGObO3euP2fxzE9/+lMvl/LnnHOOO+qoo9wTTzzhhg4d6jc49Qo65x599FFPuB9//LE75ZRT3N577+0uu+wyd9NNN3lZ2GvBEDAESkSgzoIh0AACX8yqNXAxZdkTJkxg/q/uueeeq/t//+//FbRbvnx53Ysvvlg3ZMgQf33ixIl1//rXv3w5yvfu3btuwYIFPm/NmjV1b731Vt1//ud/1u222251K1eu9HL+7//+r27u3Ll1ffv2revYsWPdkiVLfD4x9Tt16lQ3b968uhtvvNG3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NO3yjIBNY+a5d2vANo08caann366J7qLL77YT1eyc8HChQv910fYt+6SSy5xjz32mH/XTVOKlYCI/e7QDR3VLnpzbsEQMAQqg4CN7CqDcyZbycqvcYgDEmnVqpXfTJUpzGhgNwI+yXXQQQe5hx9+2JcPpzCLjfaihBSeh2na0jkxB7LDvJDY0JXzMK9YWcpJjmTKLpXXedrjrNxLacfR9Gs5Ajayazl2VjMlCOD4mSbkORzfjSSQF8aM7K6++mq/M4FIhOszZ8507CoAEfLtyaFDh/pdDERIlHn00Uc9ib7xxhuOXQ3Yh+7YY491e++9t/vJT37i2xVxEUNQbBV09tlnez2OOuood/TRR3t91Pbs2bP9buUDBw50PFdk26Df/va3noQhXgI2sFce38PcaKON/G4J7HrAtzZpR+V8YftjCBgCjSPQ/G9HW41aQeALf5sNa9kl4KCDDvK7ARxwwAF1Tz31VN3KlSv9jgba6SBqCbsPdOvWra5t27Z1p59+et1VV11Vt/fee/tdCE488cTCbgi/+93vfN7mm29e17Vr17rBgwfXjR49uo521ltvvbpFixZ50bSDHo888ojX46abbvL5m2yySV3fvn0L8titADnsjnDaaafVXX755XW77rqr393gkksuKcg69NBD/c4Ie+21l9cNHTfYYAOv88yZM6PmpPo8S/dSqoE05VqMAL8eLRgCRRHIgoMSwWAA2+vssssunmjYNqdNmzZ1e+65Z93FF19c98orr9Szcc2aNXX77rtvXatWrereeOMNf02yzj33XC+DbYDYzufWW2/12/xstdVWdUuWLPFlqf/AAw/4chMnTqwn+5hjjvFyP/vsM7/dENvy9OnTp9DGYYcd5kmS7Yi0/RDEjO6tW7euW7x4cd29997rCRZdwvDyyy97XUaNGhVmpz6dhXsp9SCagmUhYGRXFnz5rpwVB6V96BhVQR6PPfZY3XHHHVfXr18/T0bYAfkx8ps/f77vtIULF/prRx999Dr7382aNctfY3RHYC866l999dX+nHY42NuuS5cufu875S1fvtyTLHvOKa9///51jO44X716tSc6RmuchwHSffTRR/1IccSIEV6HpUuXFopohPrtb3+77mtf+1qBKAsFUpzIyr2UYghNtTIRsC+oND7La1dTjgDP6bSTAM+xeHbHczV2HOf5GM/knnzy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
</div>