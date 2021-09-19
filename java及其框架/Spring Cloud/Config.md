[toc]
# 介绍
&emsp;&emsp;可以理解为配置中心,把配置放在某个地方(jdbc,git,svn,vault),统一管理.
- 和注册中心类似,分为客户端和服务端,服务端就是提供配置的,客户端就获取配置,
- 仅SpringCloudConfig不支持自动动态获取配置,需要结合bus(总线)来实现才行
- 使用配置中心后,会优先使用配置中心的地址,一旦配置中心连不通才会使用本地配置文件
以后再补充吧

# 使用

## 1. 基本使用
> 以snv为例

1.  服务端:   
pom.xml
```
<!-- 基础配置 -->
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-config-server</artifactId>
</dependency>
<!-- svn -->
<dependency>
	<groupId>org.tmatesoft.svnkit</groupId>
	<artifactId>svnkit</artifactId>
</dependency>
```
启动主类:
```
@SpringBootApplication
@EnableConfigServer //表示使用配置中心
public class SpringBootTestConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootTestConfigApplication.class, args);
	}

}
```
application.properties
```
server.port=8999
server.servlet.context-path=/test

spring.cloud.config.server.svn.uri=http://10.101.222.10/jht/标准项目/JSCP/code/trunk/jportal/jportal-server/src/main/
spring.cloud.config.server.svn.username=xiaokunqi
spring.cloud.config.server.svn.password=xiaokunqi
spring.cloud.config.server.default-label=resources # 具体某个文件夹下
spring.profiles.active=subversion
```
使用网址可以访问到配置: `http://localhost:8999/test/application/jdbc`
2. 客户端
pom.xml
```
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```
增加一个文件 bootstrap.properties
```
#配置中心(服务端)地址+名字
spring.cloud.config.uri=http://localhost:8999/test
spring.cloud.config.name=${spring.application.name} #取当前应用的名字
spring.cloud.config.profile=jdbc

#查询格式: name-profile.properties = jportal-jdbc.properties
```
> ++bootstrap.properties 优先级会大于application.properties++,或者说config只会读bootstrap.properties,我写在application中没有生效

3. 启动  
先启动配置中心再启动客户端


## 2. 使用jdbc配置
在生产中使用svn不太好,使用jdbc更好,就是把配置文件放在数据库中,config会默认使用  
`SELECT KEY, VALUE from PROPERTIES where APPLICATION=? and PROFILE=? and LABEL=?`  
查询数据,但是key和vaule在mysql中是关键字,如果使用默认sql,则如此:
```
SELECT `KEY`, `VALUE` from PROPERTIES where APPLICATION=? and PROFILE=? and LABEL=?
```
有application,profile,label三个字段唯一标识一个文件(文件中就可以有很多配置了)
1. 客户端
bootstrap.properties
```
#在基础配置中增加,这个配置在基础配置中可以不写
spring.cloud.config.label=dev  # 可以理解为分支,其他数据一样,写成test,就能读取数据库中test的配置文件,就可以做到配置文件的切换了
```
2. 服务端
pom.xml
```
 <!-- MYSQL -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```
application.properties
```
spring.profiles.active=jdbc
spring.cloud.config.server.jdbc.sql=select keye , valuee from config_info where application=? and profile = ? and lable = ? # 指定sql,这里换了个名字,加了e,

spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://10.10.203.10:3306/jplatdv?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowMultiQueries=true
spring.datasource.username=test
spring.datasource.password=Jht123456
spring.datasource.type=com.alibaba.druid.pool.DruidDataSource
spring.datasource.initialSize=5    
spring.datasource.minIdle=5    
spring.datasource.maxActive=20       
spring.datasource.maxWait=60000
spring.cloud.config.server.jdbc=true
```

在数据库中创建表,用来存储配置
```
CREATE TABLE config_info  (
  id varchar(255)     NULL DEFAULT NULL,
  keye varchar(255)     NULL DEFAULT NULL,
  valuee varchar(255)     NULL DEFAULT NULL,
  application varchar(255)     NULL DEFAULT NULL,
  profile varchar(255)    NULL DEFAULT NULL,
  lable varchar(255)      NULL DEFAULT NULL
)
```
插入一些例子
```
INSERT INTO config_info VALUES ('1', 'spring.datasource.driver-class-name', 'com.mysql.jdbc.Driver', 'jportal-server-xkj', 'jdbc', 'resource');
INSERT INTO config_info VALUES ('2', 'spring.datasource.url', 'jdbc:mysql://10.10.203.10:3306/jplatdv?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowMultiQueries=true', 'jportal-server-xkj', 'jdbc', 'resource');
INSERT INTO config_info VALUES ('3', 'spring.datasource.username', 'test', 'jportal-server-xkj', 'jdbc', 'resource');
INSERT INTO config_info VALUES ('4', 'spring.datasource.password', 'Jht123456', 'jportal-server-xkj', 'jdbc', 'resource');
INSERT INTO config_info VALUES ('5', 'spring.datasource.type', 'com.alibaba.druid.pool.DruidDataSource', 'jportal-server-xkj', 'jdbc', 'resource');
INSERT INTO config_info VALUES ('6', 'spring.datasource.initialSize', '5', 'jportal-server-xkj', 'jdbc', 'resource');
INSERT INTO config_info VALUES ('7', 'spring.datasource.minIdle', '5', 'jportal-server-xkj', 'jdbc', 'resource');
INSERT INTO config_info VALUES ('8', 'spring.datasource.maxActive', '5', 'jportal-server-xkj', 'jdbc', 'resource');
INSERT INTO config_info VALUES ('9', 'mybatis.mapper-locations', 'classpath*:mapper/mysql/*Mapper.xml', 'jportal-server-xkj', 'jdbc', 'resource');
INSERT INTO config_info VALUES ('10', 'jportal.server.name', 'jportal-server-xkj', 'jportal', 'constants', 'dev');
INSERT INTO config_info VALUES ('11', 'business.server.name', 'business-server-xkj', 'jportal', 'constants', 'dev');
INSERT INTO config_info VALUES ('12', 'logger.server.name', 'logger-server-xkj', 'jportal', 'constants', 'dev');
INSERT INTO config_info VALUES ('13', 'RAND_CODE_STRING', '1', 'jportal', 'constants', 'dev');
```
访问网址:`http://localhost:8999/test/jportal-server-xkj/jdbc/dev`就能看到配置了

## 3.将配置中心加到注册中心
将配置中心加到注册中心,这样可以实现配置中心高可用,客户端还不用根据ip和端口,可以通过注册中心找到服务
1. 服务端
pom.xml
```
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-eureka</artifactId>
	<version>1.3.1.RELEASE</version>
</dependency>
```
application.properties
```
#注册中心地址
eureka.client.serviceUrl.defaultZone=http://10.101.90.171:10001/eureka/
```

2. 客户端
注释掉`spring.cloud.config.uri`,因为不需要指定了.
添加如下配置:
```
spring.cloud.config.discovery.enabled=true
spring.cloud.config.discovery.serviceId=test // 配置中心的名字
eureka.client.serviceUrl.defaultZone=http://10.101.90.171:10001/eureka/
```



> 有找到特别适用的场景,即使可以动态获取配置,感觉也很鸡肋,哪会轻易动配置,公司想要的场景是,开发-测试-运维能共同使用的配置中心,配置大家都能看到并使用,遗弃目前的人工交接配置

