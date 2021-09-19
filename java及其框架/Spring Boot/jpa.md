# 首先了解JPA是什么？
&emsp;&emsp; JPA(Java Persistence API)是Sun官方提出的Java持久化规范。它为Java开发人员提供了一种对象/关联映射工具来管理Java应用中的关系数据.   
&emsp;&emsp;他的出现主要是为了简化现有的持久化开发工作和整合ORM技术，结束现在Hibernate，TopLink，JDO等ORM框架各自为营的局面。值得注意的是，JPA是在充分吸收了现有Hibernate，TopLink，JDO等ORM框架的基础上发展而来的，具有易于使用，伸缩性强等优点。   
&emsp;&emsp;从目前的开发社区的反应上看，JPA受到了极大的支持和赞扬，其中就包括了Spring与EJB3.0的开发团队。
> 注意:JPA是一套规范，不是一套产品，那么像Hibernate,TopLink,JDO他们是一套产品，如果说这些产品实现了这个JPA规范，那么我们就可以叫他们为JPA的实现产品。

# spring data jpa
&emsp;&emsp;Spring Data JPA 是 Spring (所以这不是spring boot的)基于 ORM 框架、JPA 规范的基础上封装的一套JPA应用框架，可使开发者用极简的代码即可实现对数据的访问和操作。它提供了包括增删改查等在内的常用功能，且易于扩展！学习并使用 Spring Data JPA 可以极大提高开发效率！
spring data jpa让我们解脱了DAO层的操作，基本上所有CRUD都可以依赖于它来实现  
来自 <http://www.ityouknow.com/springboot/2016/08/20/spring-boo-jpa.html> 

> 注:在项目中应该用得不多,毕竟项目中都是复杂查询,不会用简单的,即使有也不用(要保持代码风格一致),一些用sql一些用jpa,阅读会有障碍,虽然jpa支持一定的复杂查询,但还是对sql比较熟悉


导入包:
```
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
        <version>1.5.6.RELEASE</version>
    </dependency>
```

1. 继承JpaRepository
```
public interface UserRepository extends JpaRepository<User, Long> {
}
```

2.  实体类
```
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="person")
public class User {
    @Id
    private String id;
    
    @Column(name = "name")//映射数据库字段名
    private String userName;
    private String password;
    // set , get ...
}
```
3.  使用默认方法
```
@Test
public void testBaseQuery() throws Exception {
    User user=new User();
    userRepository.findAll();
    userRepository.findOne(1l);
    userRepository.save(user);
    userRepository.delete(user);
    userRepository.count();
    userRepository.exists(1l);
    // ...
}
```
> 来自 <http://www.ityouknow.com/springboot/2016/08/20/spring-boo-jpa.html> 


自定义的简单查询
根据方法名来自动生成SQL，主要的语法是findXXBy,readAXXBy,queryXXBy,countXXBy, getXXBy后面跟属性名称：
(其实还是要根据它的规则来命名)
```
import org.springframework.data.jpa.repository.JpaRepository;
import com.xkj.demo.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{
    User findByUserName(String UserName);//
}
```
> 来自 <http://www.ityouknow.com/springboot/2016/08/20/spring-boo-jpa.html> 


