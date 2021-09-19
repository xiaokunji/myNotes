[toc]

logstash全是插件,从input到output,幸运的是官方文档都有些,https://www.elastic.co/guide/en/logstash/6.5/index.html ,    
> 总感觉logstash和flume很相似,他们都有三个级别,前,中,后,但是他们还是区别,logstash更倾向于日志收集,比较轻量级,flume更倾向于链路路由,比较重量级,https://blog.csdn.net/jek123456/article/details/65658790

# 1.input
 能从许多地方读取信息,从`beats`(专门读文件(log)的)工具,`elasticsearch`,`shell`,`文件`,`jdbc`,`kafka`,`redis`,`rabbitmp`,`tcp/udp`等等
## 1.1 file插件
```
input{
    file{
        path => ["/var/log/nginx/access.log", "/var/log/nginx/error.log"] #处理的文件的路径, 可以定义多个路径
        exclude => "*.zip" #匹配排除
        sincedb_path => "/data/" #sincedb数据文件的路径, 默认<path.data>/plugins/inputs/file
        codec => "plain" #默认是plain,可通过这个参数设置编码方式
       # codec => multiline {  # 管理多线事件,将java的异常归纳到一条数据中
       #   pattern =>“^\s”
       #   what =>“previous”
       # }
        tags => ["nginx"] #添加标记
        type => "nginx" #添加类型
        discover_interval => 2 #每隔多久去查一次文件, 默认15s
        stat_interval => 1 #每隔多久去查一次文件是否被修改过, 默认1s
        start_position => "beginning" #从什么位置开始读取文件数据, beginning和end, 默认是结束位置end
    }
}

原文：https://blog.csdn.net/gekkoou/article/details/809
```

## 1.2 TCP/UDP插件
```
input{
    tcp{
       port => 8888 #端口
       mode => "server" #操作模式, server:监听客户端连接, client:连接到服务器
       host => "0.0.0.0" #当mode为server, 指定监听地址, 当mode为client, 指定连接地址, 默认0.0.0.0
       ssl_enable => false #是否启用SSL, 默认false
       ssl_cert => "" #SSL证书路径
       ssl_extra_chain_certs => [] #将额外的X509证书添加到证书链中
       ssl_key => "" #SSL密钥路径
       ssl_key_passphrase => "nil" #SSL密钥密码, 默认nil
       ssl_verify => true #核实与CA的SSL连接的另一端的身份
       tcp_keep_alive => false #TCP是否保持alives
    }
}
input{
    udp{
       buffer_size => 65536 #从网络读取的最大数据包大小, 默认65536
       host => 0.0.0.0 #监听地址
       port => 8888 #端口
       queue_size => 2000 #在内存中保存未处理的UDP数据包的数量, 默认2000
       workers => 2 #处理信息包的数量, 默认2
    }
}

原文：https://blog.csdn.net/gekkoou/article/details/809
```

# 2.filter
&nbsp;&nbsp;Filter是Logstash功能强大的主要原因，它可以对Logstash Event进行丰富的处理，比如说解析数据、删除字段、类型转换等等，常见的有如下几个：

- date: 日志解析
- grok：正则匹配解析
- dissect：分割符解析
- mutate：对字段做处理，比如重命名、删除、替换等
- json：按照json解析字段内容到指定字段中
- geoip：增加地理位置数据
- ruby： 利用ruby代码来动态修改Logstash Event

## 2.1 date插件
从字段解析日期以用作事件的Logstash时间戳，以下配置解析名为logdate的字段以设置Logstash时间戳：
```
filter {
  date {
    match => [ "logdate", "MMM dd yyyy HH:mm:ss" ]
  }
}
```
返回结果:
```
{"logdate":"Jan 01 2018 12:02:03"} 
{
      "@version" => "1",
          "host" => "Node2",
    "@timestamp" => 2018-01-01T04:02:03.000Z,
       "logdate" => "Jan 01 2018 12:02:03"
}

```

说明：   
match：类型为数组，用于指定日期匹配的格式，可以一次指定多种日志格式

```
match => [ "logdate", "MMM dd yyyy HH:mm:ss" ,"MMM d yyyy HH:mm:ss","ISO8601"]
```

- target:类型为字符串，用于指定赋值的字段名，默认是@timestamp 
- timezone：类型为字符串，用于指定时区

关于logstash时区的问题可以参考：[logstash 时间戳时区问题](https://www.zybuluo.com/StrGlee/note/1179723)

## 2.2 grok插件
将非结构化事件数据分析到字段中。 这个工具非常适用于系统日志，Apache和其他网络服务器日志，MySQL日志，以及通常为人类而不是计算机消耗的任何日志格式。但是消耗的资源也十分巨大

```
filter {
  grok {
    match => { "message" => "%{IP:client} %{WORD:method} %{URIPATHPARAM:request} %{NUMBER:bytes} %{NUMBER:duration}" }
  }
}
```

测试:  
```
55.3.244.1 GET /index.html 15824 0.043
```
以下配置将消息解析为字段：
```
client: 55.3.244.1
method: GET
request: /index.html
bytes: 15824
duration: 0.043
```

Grok语法：

```
%{SYNTAX:SEMANTIC}   # SYNTAX为grok pattern的名称，SEMANTIC为赋值字段名称

%{NUMBER:duration}可以匹配数值类型，但是grok匹配出的内容都是字符串类型，可以通过在最后指定为int或者float来强制转换类型。
%{NUMBER:duration:float}
```


常见pattern可以查看：[GitHub](https://github.com/logstash-plugins/logstash-patterns-core/tree/master/patterns)或者logstash家目录下的：


```
vendor/bundle/jruby/2.3.0/gems/logstash-patterns-core-4.1.2/patterns
```

自定义匹配规则：   
格式：`(?<field_name>the pattern here)`

- pattern_definitions参数，以键值对的方式定义pattern名称和内容
- pattern_dir参数，以文件的形式被读取

```
filter{
    grok {
        match => {"message"=>"%{SERVICE:service}"}
        pattern_definitions => {"SERVICE" => "[a-z0-9]{10,11}"}
        #patterns_dir => ["/opt/logstash/patterns", "/opt/logstash/extra_patterns"]
    }
}
```

- tag_on_failure: 默认是_grokparsefailure,可以基于此做判断

调试： 
正则表达式： 
https://regexr.com/

grok： 
http://grokdebug.herokuapp.com/ 
http://grok.elasticsearch.cn/   
x-pack
 
## 2.3 dissect插件
基于分隔符原理解析数据，解决grok解析时消耗过多cpu资源的问题

使用分隔符将非结构化事件数据提取到字段中。 解剖过滤器不使用正则表达式，速度非常快。 但是，如果数据的结构因行而异，grok过滤器更合适。

dissect的应用有一定的局限性：主要适用于每行格式相似且分隔符明确简单的场景 
dissect语法比较简单，有一系列字段(field)和分隔符(delimiter)组成

```
%{}字段
%{}之间是分隔符
```
例如，假设日志中包含以下消息：   
`Apr 26 12:20:02 localhost systemd[1]: Starting system activity accounting tool...`
以下配置解析消息：

```
filter {
  dissect {
    mapping => { "message" => "%{ts} %{+ts} %{+ts} %{src} %{prog}[%{pid}]: %{msg}" }
  }
}
```
解剖过滤器应用后，事件将被解剖到以下领域：
```
{
  "msg"        => "Starting system activity accounting tool...",
  "@timestamp" => 2017-04-26T19:33:39.257Z,
  "src"        => "localhost",
  "@version"   => "1",
  "host"       => "localhost.localdomain",
  "pid"        => "1",
  "message"    => "Apr 26 12:20:02 localhost systemd[1]: Starting system activity accounting tool...",
  "type"       => "stdin",
  "prog"       => "systemd",
  "ts"         => "Apr 26 12:20:02"
}
```
说明
```
Apr 26 12:20:02
%{ts} %{+ts} %{+ts}     #+代表该匹配值追加到ts字段下
{
    "ts":"Apr 26 12:20:02"
}

two three one go
%{+order/2} %{+order/3} %{+order/1} %{+order/4}     #/后面的数字代表拼接的次序
{
    "order": "one two three go"
}

a=1&b=2
%{?key1}=%{&key1}&%{?key2}=%{&key2}  #%{?}代表忽略匹配值，但是富裕字段名，用于后续匹配用；%{&}代表将匹配值赋予key1的匹配值
{
    "a":"1",
    "b":"2"
}

#dissect可以自动处理空的匹配值
John Smith,Big Oaks,Wood Lane,Hambledown,Canterbury,CB34RY
%{name},%{addr1},%{addr2},%{addr3},%{city},%{zip}

Jane Doe,4321 Fifth Avenue,,,New York,87432
{
    "name":"Jane Doe",
    "addr1":"4321 Fifth Avenue",
    "addr2":"",
    "addr3":"",
    "city":"New York",
    "zip":"87432"
}

#dissect分割后的字段值都是字符串，可以使用convert_datatype属性进行类型转换
filter{
    dissect{
        convert_datatype => {age => "int"}
    }
}

```

## 2.4 mutate插件
使用最频繁的操作，可以对字段进行各种操作，比如重命名、删除、替换、更新等，主要操作如下：
```
convert   #类型转换
gsub      #字符串替换
split/join/merge    #字符串切割、数组合并为字符串、数组合并为数组
rename    #字段重命名
update/replace   #字段内容更新或替换
remove_field     #删除字段

```
- `convert`：实现字段类型的转换，类型为hash,仅支持转换为integer、float、string和Boolean
```
filter{
    mutate{
        convert => {"age" => "integer"}
    }
}
```
- `gsub`：对字段内容进行替换，类型为数组，每3项为一个替换配置
```
filter {
  mutate {
    gsub => [
      # replace all forward slashes with underscore
      "fieldname", "/", "_",
      # replace backslashes, question marks, hashes, and minuses
      # with a dot "."
      "fieldname2", "[\\?#-]", "."
    ]
  }
}  
```
- `split`: 将字符串切割为数组
```
filter {
  mutate {
     split => { "fieldname" => "," }
  }
}   
```
- `join`：将数组拼接为字符串 
- `merge`：将两个数组合并为1个数组，字符串会被转为1个元素的数组进行操作 
- `rename`：字段重命名 
- `update/replace`：更新字段内容，区别在于update只在字段存在时生效，而replace在字段不存在时会执行新增字段的操作
```
filter {
  mutate {
    update => { "sample" => "My new message" }
    update => { "message" => "source from c:%{source_host}" }   #%{source_host}可以引用logstash Event中的字段值
  }
}     

```
```
input {
        stdin{type=>stdin}
}
filter{
        dissect{ mapping => {"message" => "%{a}-%{b}-%{c}"} }
        mutate{ replace => {"d" =>"source from c:%{c}"} }
}
output{
        stdout{codec=>rubydebug}
}   

hi-hello-123
{
             "a" => "hi",
             "b" => "hello",
    "@timestamp" => 2018-06-29T02:01:24.473Z,
             "c" => "123",
             "d" => "source from c:123",
      "@version" => "1",
          "host" => "Node2",
       "message" => "hi-hello-123",
          "type" => "stdin"
}       

```
## 2.5 json插件
将字段内容为json格式的数据进行解析
```
filter {
  json {
    source => "message"     #要解析的字段名
    target => "msg_json"    #解析后的存储字段，默认和message同级别
  }
}  
```

## 2.6 geoip插件
常用的插件，根据ip地址提供对应的地域信息，比如经纬度、城市名等，方便进行地理数据分析
```
filter {
  geoip {
    source => "clientip"
  }
}
```

## 2.7 ruby插件
最灵活的插件，可以 以ruby语言来随心所欲的修改Logstash Event对象,ruby能实现逻辑,理论上来说可以完成你想要的任何操作,反正很吊就对了(前提是你得会ruby语言)
```
filter{
    ruby{
        code => 'size = event.get("message").size;
                event.set("message_size",size)'
    }
}

ruby {
        code => "event.set('@read_timestamp',event.get('@timestamp'))"
}
```

原文：https://blog.csdn.net/wfs1994/article/details/80862952 

# 3.output
能输出到很多地方,比如,`csv`,`elasticsearch`,`shell`,`file`,`kafka`,`mongDB`,`rabbitMq`,`solr`,更多的参见官网

## 3.1 elasticsearch插件
```
  elasticsearch{  
    hosts=>["172.132.12.3:9200"]  
    action=>"index"  
    index=>"indextemplate-logstash"  
    #document_type=>"%{@type}"  
    document_id=>"ignore"  
      
    template=>"/opt/logstash-conf/es-template.json"  
    template_name=>"es-template.json"  
    template_overwrite=>true       
    }
    来自: https://yq.aliyun.com/articles/197785
```



下载工具:https://motrix.app/zh-CN/