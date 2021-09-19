[toc]

# 1. 定义

ElasticStack在升级到5.0版本之后，带来了一个新的脚本语言，painless。这里说“新的“是相对与已经存在groove而言的。Groove脚本开启之后，如果被人误用可能带来各种漏洞，由于这些外部的脚本引擎太过于强大，用不好或者设置不当就会引起安全风险，基于安全和性能方面，所以elastic.co开发了一个新的脚本引擎，名字就叫Painless，和Groove的沙盒机制不一样，Painless使用白名单来限制函数与字段的访问，针对es的场景来进行优化，只做es数据的操作，更加轻量级，速度要快好几倍，并且支持<u>Java静态类型，语法保持Groove类似，还支持Java的lambda表达式</u>。

> 可以做很多事情,改字段值 / 加字段 / 查询时处理结果值等等

# 2. 语法

**脚本变量**

ctx._source.field：add, contains, remove, indexOf, length

ctx.op：应该对文档应用的操作：索引或删除

ctx._index：访问文档元数据字段

_score 只在script_score中有效

doc[‘field’], doc[‘field’].value: add, contains, remove, indexOf, length



1、常用数据类型： int、double、String、List、Map、bool  (和java一致）

2、变量定义：

​    定义变量有两种方式，动态类型和静态类型，建议用静态类型，静态类型的计算速度是动态类型的10倍

- 动态类型定义方式： def a = "abc"
- 静态类型定义方式： int a = 1;   Sting b = "asdsd"

 3、获取文档中某个字段的值的用法：

获取doc下字段：  doc['name'].value 

nested字段取出来后就是数组，可用下标获取, 后面不用加value： `doc['expect_jobs'][1]`

doc以外的字段可直接使用，如下：

```json
  POST twitter/_update/1
    {
      "script": {
        "source": "ctx._source.age = params.value",
        "params": {
          "value": 34
        }
      }
    }
```

> 如果params是其他类型,基本遵循java的语法

lambda语法:

```java
list.sort((x, y) -> x - y);
list.sort(Integer::compare);
```



# 3. 脚本使用

脚本格式

```json
"script": {
  "lang":   "...",  
  "source" | "id": "...", 
  "params": { ... } 
}
```

> 三个参数分别为: 
>
> - 脚本编写的语言，默认为 painless。
> - 脚本本身可以指定为内联脚本的 source 或存储脚本的 id。
> - 应传递给脚本的任何命名参数。



**访问source里的字段**

Painless中用于访问字段值的语法取决于上下文。在Elasticsearch中，有许多不同的Plainless上下文。就像那个链接显示的那样，Plainless上下文包括：ingest processor, update, update by query, sort，filter等等。
Context 访问字段
Ingest node: 访问字段使用ctx ctx.field_name
Updates: 使用`_source` 字段 `ctx._source.field_name`

这里的updates包括`_update`，`_reindex`以及update_by_query。这里，我们对于context（上下文的理解）非常重要。它的意思是针对不同的API，在使用中ctx所包含的字段是不一样的



## 3.1 inline 脚本

```http
    PUT twitter/_doc/1
    {
      "user" : "双榆树-张三",
      "message" : "今儿天气不错啊，出去转转去",
      "uid" : 2,
      "age" : 20,
      "city" : "北京",
      "province" : "北京",
      "country" : "中国",
      "address" : "中国北京市海淀区",
      "location" : {
        "lat" : "39.970718",
        "lon" : "116.325747"
      }
    }
```

在这个文档里，我们现在想把age修改为30，那么一种办法就是把所有的文档内容都读出来，让修改其中的age想为30，再重新用同样的方法写进去。首先这里需要有几个动作：先读出数据，然后修改，再次写入数据。显然这样比较麻烦。在这里我们可以直接使用Painless语言直接进行修改：

```http
  POST twitter/_update/1
    {
      "script": {
        "source": "ctx._source.age = 30"
      }
    }
```

这里的source表明是我们的Painless代码。这里我们只写了很少的代码在DSL之中。这种代码称之为inline。在这里我们直接通过`ctx._source.age`来访问 `_souce`里的age。这样我们通过编程的办法直接对年龄进行了修改

**每次更新值不一样,es会认为是不同的脚本,都会进行编译,所以需要用参数的方式,这样es就会当成一个脚本**

> Elasticsearch第一次看到一个新脚本，它会编译它并将编译后的版本存储在缓存中。无论是inline或是stored脚本都存储在缓存中。新脚本可以驱逐缓存的脚本。**默认的情况下是可以存储100个脚本**。我们可以通过设置script.cache.max_size来改变其大小，或者通过script.cache.expire来设置过期的时间。这些设置需要在config/elasticsearch.yml里设置。

```http
  POST twitter/_update/1
    {
      "script": {
        "source": "ctx._source.age = params.value",
        "params": {
          "value": 34
        }
      }
    }
```

```http
   GET hockey/_search
    {
      "query": {
        "match_all": {}
      },
      "script_fields": {
        "total_goals": {
          "script": {
            "lang": "painless",
            "source": """
              int total = 0;
              for (int i = 0; i < doc['goals'].length; ++i) {
                total += doc['goals'][i];
              }
              return total;
            """
          }
        }
      }
    }
```



> source里面可以用python的多行字符串方式写语法

**source也能具有查询的功能**



## 3.2 stored 脚本

source字段不光能写脚本,还能写id,这个id就是指的是一个写好的脚本,这样,脚本就能更好的管理,不用内嵌到查询中

在这种情况下，scripts可以被存放于一个集群的状态中。它之后可以通过ID进行调用：

```http
 PUT _scripts/add_age
    {
      "script": {
        "lang": "painless",
        "source": "ctx._source.age += params.value"
      }
    }
```

在这里，我们定义了一个叫做add_age的script。它的作用就是帮我们把source里的age加上一个数值。我们可以在之后调用它：

```http
  POST twitter/_update/1
    {
      "script": {
        "id": "add_age",
        "params": {
          "value": 2
        }
      }
    }
```



## 使用Painless访问Doc里的值

文档里的值可以通过一个叫做doc的Map值来访问。例如，以下脚本计算玩家的总进球数。 此示例使用类型int和for循环。

```http
    GET hockey/_search
    {
      "query": {
        "function_score": {
          "script_score": {
            "script": {
              "lang": "painless",
              "source": """
                int total = 0;
                for (int i = 0; i < doc['goals'].length; ++i) {
                  total += doc['goals'][i];
                }
                return total;
              """
            }
          }
        }
      }
    }
```

或者，您可以使用script_fields而不是function_score执行相同的操作：

```http
   GET hockey/_search
    {
      "query": {
        "match_all": {}
      },
      "script_fields": {
        "total_goals": {
          "script": {
            "lang": "painless",
            "source": """
              int total = 0;
              for (int i = 0; i < doc['goals'].length; ++i) {
                total += doc['goals'][i];
              }
              return total;
            """
          }
        }
      }
    }
```





# 脚本优化
使用脚本缓存, 预先缓存可以节省第一次的查询时间

使用ingest pipeline进行预先计算

相比于_source.field_name使用doc[‘field_name’]语法速度更快, doc语法使用doc value , 列存储





参考链接:

https://blog.csdn.net/qq_24499615/article/details/116161674

https://www.elastic.co/guide/en/elasticsearch/painless/current/painless-lambdas.html

https://blog.csdn.net/u013613428/article/details/78134170



