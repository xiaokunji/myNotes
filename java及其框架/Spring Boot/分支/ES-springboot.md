[TOC]

# 前言

到目前为止，ES有4种客户端，分别是：Jest client、Rest client、Transport client、Node client，相信大家在项目集成中选择客户端比较纠结，搜索案例的时候一会是这个客户端实现的，一会儿又是别的客户端实现的，自己又不了解每个客户端的优劣势，但又想集成最好的，下面就来说说各个客户端的区别，以及优劣势

ES支持两种协议  
       HTTP协议，支持的客户端有Jest client和Rest client

Native Elasticsearch binary协议，也就是Transport client和Node client

**Jest client和Rest client区别**

​      Jest client非官方支持，在ES5.0之前官方提供的客户端只有Transport client、Node client。在5.0之后官方发布Rest client，并大力推荐

**Transport client和Node client区别**

​       Transport client（7.0弃用）和Node client（2.3弃用）区别：最早的两个客户端，Transport client是不需要单独一个节点。Node client需要单独建立一个节点，连接该节点进行操作，ES2.3之前有独立的API，ES2.3之后弃用该API，推荐用户创建一个节点，并用Transport client连接进行操作

综合：以上就是各个客户端现在的基本情况，可以看出Rest client目前是官方推荐的，但是springBoot默认支持的依然Transport client，这可能和ES更新速度有关

> **Springboot 2.3.x版本开始, Spring Data Elasticsearch 4.0.x 开始使用 restClient**
>
> [版本对应-官方文档](https://docs.spring.io/spring-data/elasticsearch/docs/4.2.2/reference/html/#preface.versions)

# 1. Rest client

> 此处仅使用HighLevelClient

## 1. application.properties

```properties
spring.elasticsearch.rest.uris=http://127.0.0.1:9200
```



## 2. pom.xml

```xml
<!--rest-->
        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-client</artifactId>
            <version>6.4.0</version>
        </dependency>
        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-high-level-client</artifactId>
            <version>6.4.0</version>
        </dependency>

```



## 3. RestHighTest.java

```java
 
package com.rancho.demo.elasticsearch;
 
import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchPhrasePrefixQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
 
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
 
@RunWith(SpringRunner.class)
@SpringBootTest
public class RestHighTest {
 
    @Resource
    private RestHighLevelClient restHighLevelClient;
 
 
    @Test
    public void add() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", "20190909");
        map.put("name", "测试");
        map.put("age", 22);
        try {
            IndexRequest indexRequest = new IndexRequest("content", "doc", map.get("id").toString()).source(map);
            IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            System.out.println(indexResponse.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    @Test
    public void search() {
        SearchRequest searchRequest = new SearchRequest().indices("content").types("doc");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        MatchPhrasePrefixQueryBuilder mppqb = QueryBuilders.matchPhrasePrefixQuery("name", "测试");
        sourceBuilder.query(mppqb);
        try {
            SearchResponse sr = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            String result = sr.toString();
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
 
    @Test
    public void update() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", "20190909");
        map.put("name", "测试-update");
        map.put("age", 22);
        try {
            UpdateRequest request = new UpdateRequest("content", "doc", map.get("id").toString()).doc(map);
            UpdateResponse updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
            System.out.println(updateResponse.toString());
        } catch (Exception e) {
 
        }
    }
 
    @Test
    public void get() {
        try {
            GetRequest request = new GetRequest("content", "doc", "20190909");
            GetResponse getResponse = this.restHighLevelClient.get(request, RequestOptions.DEFAULT);
            System.out.println(getResponse.toString());
        } catch (Exception e) {
 
        }
    }
 
    @Test
    public void delete() {
        try {
            DeleteRequest request = new DeleteRequest("content", "doc", "20190909");
            DeleteResponse deleteResponse = this.restHighLevelClient.delete(request, RequestOptions.DEFAULT);
            System.out.println(deleteResponse.toString());
        } catch (Exception e) {
 
        }
    }
}
```

> 因为它是直接用了client,所以是没有线程池的,可以利用 apache的 `GenericObjectPool`类来实现线程池
>
> > [有文章说](https://www.jianshu.com/p/0b4f5e41405e): Rest Client是长连接，而且内部有默认的线程池管理，因此一般无需自定义线程池管理连接
> >
> > [源码分析1](https://cloud.tencent.com/developer/article/1618394)  线程池是访问es的http连接池
> >
> > [源码分析2](https://www.cnblogs.com/duanxz/p/5206304.html)
>
> 详情见search服务代码

# 2. Springboot 方式

## 1.application.properties

文件增加以下内容
```properties
spring.data.elasticsearch.cluster-name=elasticsearch
spring.data.elasticsearch.cluster-nodes=127.0.0.1:9300  #java的es默认连接端口是9300，9200是http端口
spring.data.elasticsearch.repositories.enabled=true
```

## 2.pom.xml
```xml
 <!--spring整合elasticsearch包-->
<dependency>  
   <groupId>org.springframework.boot</groupId>  
   <artifactId>spring-boot-starter-data-elasticsearch</artifactId>  
</dependency>
```

> 这里用的是springboot DB的方式,还有其他方式也可连接,springboot 对应的Es版本关系有严格匹配,这里用的是,springboot 2.0.6版,es是5.6.16版

>1、None of the configured nodes are available 或者org.elasticsearch.transport.RemoteTransportException: Failed to deserialize exception response from stream   
原因：spring data elasticSearch 的版本与Spring boot、Elasticsearch版本不匹配。
这是版本之间的对应关系。Spring boot 1.3.5默认的elasticsearch版本是5.2，此时启动7.2版本以下的Elasticsearch客户端连接正常。   
2、Caused by: java.lang.IllegalArgumentException: @ConditionalOnMissingBean annotations must specify at least one bean (type, name or annotation)   
原因：spring boot是1.3.x版本(es用的是1.X版本)，而es采用了boot2.x版本。在es的boot2.x版本去除了一些类，而这些类在spring boot的1.3.x版本中仍然被使用，导致此错误    
3、es在5.x版本中一个索引允许有两个type,在6.x版本中一个索引只允许有一个type,在7.x版中支持不要type;(这是es的type移除规划)



## 3.实体类
```
@Document(indexName = "jportal", type = "platformLogin")
public class PlatformLogin  {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 448279746126620954L;

	@Id
	private String id;
	private Date operateTime; // es存时间存的是时间戳,
	private String operateTimeStr;// 用来格式化时间
	private String loginAcc;
	private String loginName;
	private String loginIp;
	private String loginType;
	private String operateContent;
	// get,set方法省略
}
```
`Spring-data-elasticsearch`为我们提供了@Document、@Field等注解，如果某个实体需要建立索引，只需要加上这些注解即可

1.类上注解：@Document (相当于Hibernate实体的@Entity/@Table)(==必写==)，加上了@Document注解之后，++默认情况下这个实体中所有的属性都会被建立索引、并且分词。++


类型 | 	属性名 | 	默认值 | 说明  |
---|---|---|---|
String| indexName|	     无|	索引库的名称，建议以项目的名称命名(只能是小写)|
String|	type|       	“”| 类型，建议以实体的名称命名|
short|	shards|	        5|  默认分区数|
short|	replica	|       1|  每个分区默认的备份数|
String|	refreshInterval	|“1s”|	刷新间隔|
String|	indexStoreType|	“fs”|	索引文件存储类型|

2.主键注解：@Id (相当于Hibernate实体的主键@Id注解)(==必写==)
只是一个标识，并没有属性。

3.属性注解 @Field (相当于Hibernate实体的@Column注解)
@Field默认是可以不加的，默认所有属性都会添加到ES中。加上@Field之后，@document默认把所有字段加上索引失效，只有加@Field 才会被索引(同时也看设置索引的属性是否为no)

 

类型|	属性名|	默认值|	说明|
---|---|---|---|
FieldType|	type|	FieldType.Auto(大部分是text类型)|	自动检测属性的类型
FieldIndex|	index|	FieldIndex.analyzed|	默认情况下分词
boolean|	store|	false|	默认情况下不存储原文
String|	searchAnalyzer|	“”|	指定字段搜索时使用的分词器
String|	indexAnalyzer|	“”|	指定字段建立索引时指定的分词器
String[]|	ignoreFields|	{}|	如果某个字段需要被忽略

## 4.代码

 需某个类继承ElasticsearchRepository,因为是demo,这里直接用dao层继承了,上面controller中的业务逻辑也可以写在impl中.   
 dao.java

> 也可以像redis那样,使用 template
>
> ```java
> @Resource
> private ElasticsearchTemplate elasticsearchTemplate;
> ```

 ```
 public interface IEsDao extends ElasticsearchRepository<PlatformLogin, String> {
    // 这种方式不用写具体实现,springboot会帮我们做,和访问mysql类型,反正项目中没用到
	public PlatformLogin findByLoginNameLike(String string);

}
 ```
controller.java:
```
	@Autowired
	private IEsDao esDao;
	
	
@RequestMapping("search")
	public String search( ) {
		 //按标题进行搜索
		 // 这里要写keyword, 在之后的查询中使用loginName是将loginName作为text类型查询，而使用loginName.keyword则是将loginName作为keyword类型查询。前者会对查询内容做分词处理之后再匹配，而后者则是直接对查询结果做精确匹配。
         PrefixQueryBuilder queryBuilder = QueryBuilders.prefixQuery("loginName.keyword", "肖坤");
         ///ES的term query做的是精确匹配而不是分词查询，因此对text类型的字段做term查询将是查不到结果的（除非字段本身经过分词器处理后不变，未被转换或分词）。此时，必须使用foobar.keyword来对foobar字段以keyword类型进行精确匹配。
         // 来自:https://segmentfault.com/q/1010000017312707
         // es中存了两份数据,一份分词的(text类型),一份不分词的(keyword类型)
		//如果没写keyword则是用条件按分词之后查询
		// 写了keyword则是对条件按整个文档搜索
        //还有好多查询条件,慢慢看api
        //withfilter和withquery入参条件是一样的,但是es处理却是不一样的
        //1.在查询(query)上下文中，查询会回答这个问题——“这个文档匹不匹配这个查询，它的相关度高么？”如何验证匹配很好理解，如何计算相关度呢？ES中索引的数据都会存储一个_score分值，分值越高就代表越匹配。另外关于某个搜索的分值计算还是很复杂的，因此也需要一定的时间。
        //2. 在过滤器上下文中，查询会回答这个问题——“这个文档匹不匹配？”答案很简单，是或者不是。它不会去计算任何分值，也不会关心返回的排序问题，因此效率会高一点。过滤上下文 是在使用filter参数时候的执行环境，比如在bool查询中使用Must_not或者filter另外，经常使用过滤器，ES会自动的缓存过滤器的内容，这对于查询来说，会提高很多性能。
        //原文：https://blog.csdn.net/qq_29580525/article/details/80908523 



//        QueryBuilder queryBuilder = QueryBuilders.termQuery("这里是你要查询的字段", searchContent);
        SearchQuery searchQuery =new NativeSearchQueryBuilder().withQuery(queryBuilder).build(); 
       
        System.out.println("查询的语句:" + searchQuery.getQuery().toString());
        Page<PlatformLogin> searchPageResults = esDao.search(searchQuery);

       PlatformLogin jpfloginInfo = esDao.findByLoginNameLike("肖");
        logger.info("返回结果:{}",searchPageResults.getContent());
        logger.info("返回结果:{}",jpfloginInfo);
		return LocalDateTime.now().toString();
	}
```



参考链接:

[几种链接方式](https://blog.csdn.net/qq_25012687/article/details/101050412)