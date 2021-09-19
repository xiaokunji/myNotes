[toc]
# 介绍
能用注解的方式来写代码.  

比如:在编译的时候回自动生成get和set方法(不是生成代码,是生成字节码,反编译后可以看到)

# 安装
**windows环境**   
1. 下载lombok.jar包https://projectlombok.org/download.html

2. 运行Lombok.jar:   

`Java -jar D:\software\lombok.jar D:\software\lombok.jar `
    这是windows下lombok.jar所在的位置
    数秒后将弹出一框，以确认eclipse的安装路径 (无法运行就跳过2,3步骤)

3. 确认完eclipse的安装路径后，点击install/update按钮，即可安装完成

4. 安装完成之后，请确认eclipse安装路径下(与eclipse.ini 同级目录下)是否多了一个lombok.jar包，并且其
    配置文件eclipse.ini中是否 添加了如下内容: 
   -javaagent:lombok.jar 
   -Xbootclasspath/a:lombok.jar 
    如果上面的答案均为true，那么恭喜你已经安装成功，否则将缺少的部分添加到相应的位置即可 

5. 重启eclipse或myeclipse,然后clean工程

> 来自 <https://www.cnblogs.com/justuntil/p/7120534.html> 


# 使用
```
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.16.20</version>
    <scope>provided</scope>
</dependency>
```
```
@Getter
@Setter
@ToString
public class SysUserEntity{
	private String name;
	private String age;
}
```
然后就可以直接使用get和set方法,tostring方法,还有其他注解,就不写出来了

> 来自 <https://www.cnblogs.com/qnight/p/8997493.html> 

# 注解
1. `@NoNull`  

   对方法上的参数进行判null处理,如果为空抛`NullPointerException`异常

2. `@CleanUp`   

   自动资源管理器,能自动关闭流,这个注解会吞噬原有的异常(就是自己的代码有流异常),小心使用   
   
   使用: ` @Cleanup InputStream in = new FileInputStream(args[0]);`

3.  `@Getter/@Setter`   

   生成get/set方法

4. `@ToString`   

   生成toString方法

5. `@EqualsAndHashCode`  
   

生成equal和hash方法
      
6. `@NoArgsConstructor, @RequiredArgsConstructor and @AllArgsConstructor`   

      生成构造函数,不带参数的; 构造参数含@nonNull属性的 ; 带全部参数的

      > spring 有构造方式注入,可以结合`@RequiredArgsConstructor `实现

7. `@Date`  

      包含`@ToString, @EqualsAndHashCode, @Getter,@RequiredArgsConstructor`,`@setter`但不包括final属性的参数

8. `@Value`   

      用在类或者方法上,将整个类变成final

9. `@Build`   

      构造器,能挨个给属性值注参,用了注解后,类里的参数默认值为null(即使你有自己的默认值),构造器不是私有的,大家都能调用,还有其他坑,建议先不使用
      使用:`Officer.build().id("00001").name("simon qi")` 

10. `@Synchronized`   

      给某个类加 Synchronized 锁

11. `@Log, @Slf4j, @CommonsLog`   

       日志注解, 分别属于`java.util.logging.Logger`,`org.slf4j.Logger`,`org.apache.commons.logging.Log`

12.  `@SneakyThrows`

       翻译就是暗中抛出异常 , 当我们需要抛出异常，在当前方法上调用，不用显示的在方法名后面写 throw

       `@SneakyThrows(Exception.class)`

来自: https://projectlombok.org/features/all

[学习Spring Boot：（十五）使用Lombok来优雅的编码 - KronChan - 博客园 (cnblogs.com)](https://www.cnblogs.com/qnight/p/8997493.html)

[Lombok之@XXXArgsConstructor系列注解使用_cauchy6317的博客-CSDN博客_allargsconstructor注解](https://blog.csdn.net/cauchy6317/article/details/102579178)


# 扩展
使用@Data用在派生类(有继承关系的)上会警告,如下:  

`Generating equals/hashCode implementation but without a call to superclass, even though this class does not extend java.lang.Object. If this is intentional, add '@EqualsAndHashCode(callSuper=false)' to your type.`

大致意思是默认子类的equals和hashCode方法，不会包含或者考虑基类的属性。我们可以通过反编译工具查看项目target/classes目录下的User.class的hashCode方法，默认情况下属性都是使用的他自身的属性。

1.  解决方式一：直接在子类上声明 `@EqualsAndHashCode(callSuper = true)`
2.  解决方式二[推荐]：在项目的`src/main/java`根目录下创建`lombok.config`配置文件

内容如下:
```
# 该配置声明这个配置文件是一个根配置文件，他会从该配置文件所在的目录开始扫描
config.stopBubbling=true
#全局配置 equalsAndHashCode 的 callSuper 属性为true，这样就不用每个类都要去写了
lombok.equalsAndHashCode.callSuper=call
```
来源: https://blog.csdn.net/qq_15071263/article/details/91660519#3srcmainjavalombok_15   

https://blog.csdn.net/feinifi/article/details/85275280