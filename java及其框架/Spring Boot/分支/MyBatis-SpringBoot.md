在application.properties 中添加配置文件
```properties
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowMultiQueries=true
spring.datasource.username=xkj
spring.datasource.password=xiaokunji 
spring.datasource.initialSize=50
spring.datasource.minIdle=10
spring.datasource.maxActive=100
spring.datasource.maxWait=60000

mybatis.mapper-locations=classpath*:mapper/*Mapper.xml   
mybatis.type-aliases-package=com.xkj.demo.entity

# 打印sql查询结果值
mybatis.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl

#默认插入空值
mybatis.configuration.jdbc-type-for-null=null
# 支持驼峰
mybatis.configuration.map-underscore-to-camel-case=true
```
> 注:扫描实体类和Mapper文件用配置文件了,在spring中是用xml方式


导入包:
```xml
<!--  MyBatis -->
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>1.2.0</version>
    </dependency>

    <!--  MySQL -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>5.1.41</version>
    </dependency>
```
实例化接口:
```
@Autowired
private UserService userService;//service接口不贴了,普通接口
```
方法调用:
```java
@RequestMapping("/getUser")
    public User getUser() {
        User user = userService.getUser();
        logger.info("用户数据:{}",user);
        return user;
    }
```
实现类:
```java
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    public UserDao userDao;
    
    @Override
    public User getUser() {
        User user = userDao.getUser();
        return user;
    }

}
```
映射mapper的接口类:
```java
@Mapper
public interface UserDao {
    public User getUser() ;
}
```
mapper文件:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">  
<mapper namespace="com.xkj.demo.dao.UserDao">
    <select id="getUser" resultType="com.xkj.demo.entity.User">
        select id,name,password from person
    </select>
</mapper>
```