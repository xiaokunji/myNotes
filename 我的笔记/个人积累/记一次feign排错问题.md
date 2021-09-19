在feign接口上写@requestMapping,如下

```java
@FeignClient(value = "online-shop-search-provider", fallback = SearchHistoryServiceFeignImpl.class)
@Component
@RequestMapping(value = "/mallv2/search/search_history/")
public interface SearchHistoryServiceFeign {

    /**
     * 新增搜索历史
     * <p>若搜索词已存在,则修改更新时间
     *
     * @param params vo
     * @return ServerResult
     * @author A80080
     * @createDate 2020/11/27
     */
    @PostMapping(value = "/save",consumes = MediaType.APPLICATION_JSON_VALUE)
    ServerResult save(@RequestBody SearchHistoryVO params);
}
```

<b style="color:red"> 如果feign接口和controller层写在一个服务包里,则必定会报错,报错内容如下:</b>

```
org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'requestMappingHandlerMapping' defined in class path resource [org/springframework/boot/autoconfigure/web/servlet/WebMvcAutoConfiguration$EnableWebMvcConfiguration.class]: Invocation of init method failed; nested exception is java.lang.IllegalStateException: Ambiguous mapping. Cannot map 'com.gree.ecommerce.eoc.GreeServiceServiceFeign' method 
com.gree.ecommerce.eoc.GreeServiceServiceFeign#serviceList(Map)
to {POST /mallv2/manage/eoc/gree_service/list}: There is already 'greeServiceController' bean method
com.gree.ecommerce.controller.eoc.GreeServiceController#serviceList(Map) mapped.
```

[参考链接](https://www.cnblogs.com/xiufengchen/p/10621705.html) : 得出在feign的类上加@RequestMapping,spring会认为这是个bean,会把它加入到bean池中

以此衍生出几个疑惑:

 1. 为什么manage会报错?而search不会报错

 2. 只在manage下的eoc下的feign目录会报错,在"根目录"就不会报错

    

接下来就是漫长的解惑历程:



1. 经过对比两个feign类有什么不同,<u>写法基本一致</u>,只是manage下报错的feign放到在eoc下

2. 通过debug源码,发现manage下的那个链接被加入了两次,而search服务只加入了一次,验证了报错是正常的,且可见原理

   > org.springframework.web.servlet.handler.AbstractHandlerMethodMapping.MappingRegistry#mappingLookup 这个map中, key是地址对象,value是方法对象

3. 怀疑是feign类的路径问题

   3.1 增加feign包扫描路径,不起作用,原本就能扫描到 ×

   3.2 将报错的feign类换个位置,仍报错 ×

   说明不是扫描包路径的问题

4. 查看@enableFeignClients注解源码,发现有个变量存放了 扫描包路径下的有@FeignClient的类,debug它,看他存放了啥

5. 对比了manage和search服务,这个变量的区别

6. 进一步证明search服务的链接只加入了一次(即使在feign类上写@RequestMapping)

7. 仔细查看这些被加入的feign,有其他包下的feign类,但又不是全部

8. 怀疑这些是被maven引入的,去查看pom依赖,**发现被引入的feign才会被加载**,去manage核实一下,的确引入了自己的feign类,

9. 既然如此,为啥manage下只有eoc下的才报错呢,按理来说,应该全部报错才对

10. 进一步对比代码,发现不报错的feign下,**每个接口上定义了 `Content-Type`,** 把报错的feign上也定义了这个,也不报错了

11. 既然有两个,为啥不报错,难道什么不一样? 转向看`mappingLookup ` 变量里的值,发现地址对象里竟然包含了数据类型

    ![img](https://gitee.com/xiaokunji/my-images/raw/master/myMD/20210711124824.png)

解决方案有多种:

1. 不要在feign上写@RequestMapping, (垃圾方案)

2. 检查 feign不要和controller共同引入,如果不能避免就没办法了

3. 在@FeignClient直接下的path下写路径,如下

   > `@FeignClient(value = "online-shop-manage-provider",path = "/mallv2/manage/",fallback = GreeServiceServiceFeignImpl.class)`



[评论中提示扫描包的问题](https://blog.csdn.net/weixin_44495198/article/details/105931661)

[在path中写路径](https://my.oschina.net/u/2000675/blog/2244769)