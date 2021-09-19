[toc]

# http 2.0 新特性

1. 二进制分帧

   > 在应用层(HTTP2.0)和传输层(TCP、UDP)新增的二进制分帧层。在这层中,数据会被分割成更小的消息和帧,然后可以无序发,最后组装就行

2. head压缩

3. 多路复用

   > 做到同一个连接并发处理多个请求，而且并发请求的数量比HTTP1.1大了好几个数量级。
   >
   > 因为请求都在同一个tcp连接上完成

4. 服务器推送

   >服务器可以对一个客户端请求发送多个响应,之前是一个请求一个响应
   >
   >**实现原理大致**: 客户端发送一次请求,服务端的请求并不会关闭,发完第一次,接着发第二次
   >
   ><u>目前NGINX的V1.13.9和tomcat已经支持,后端也能实现</u>
   >
   >关键字是`link`
   >
   >服务器推送有一个很麻烦的问题。所要推送的资源文件，如果浏览器已经有缓存，推送就是浪费带宽。即使推送的文件版本更新，浏览器也会优先使用本地缓存。
   >
   >一种解决办法是，只对第一次访问的用户开启服务器推送。

> 来源: [服务器推送实现](https://www.ruanyifeng.com/blog/2018/03/http2_server_push.html)
>
> [http2.0新特性](https://www.jianshu.com/p/c90e458a26b2)
>
> [http各版本之间的区别](https://blog.csdn.net/weixin_48502062/article/details/108330192)
>
> 



# HTTP协议的Keep-Alive 

可以看到里面的请求头部和响应头部都有一个key-value`Connection: Keep-Alive`，这个键值对的作用是让HTTP保持连接状态，因为HTTP 协议采用“请求-应答”模式，当使用普通模式，即非 Keep-Alive 模式时，每个请求/应答客户和服务器都要新建一个连接，完成之后立即断开连接（HTTP 协议为无连接的协议）；当使用 Keep-Alive 模式时，Keep-Alive 功能使客户端到服务器端的连接持续有效。

在HTTP 1.1版本后，默认都开启Keep-Alive模式，只有加入加入 `Connection: close`才关闭连接，当然也可以设置Keep-Alive模式的属性，例如 `Keep-Alive: timeout=5, max=100`，表示这个TCP通道可以保持5秒，max=100，表示这个长连接最多接收100次请求就断开。

**Keep-Alive模式下如何知道某一次数据传输结束**

如果不是Keep-Alive模式，HTTP协议中客户端发送一个请求，服务器响应其请求，返回数据。服务器通常在发送回所请求的数据之后就关闭连接。这样客户端读数据时会返回EOF（-1），就知道数据已经接收完全了。
 但是如果开启了 Keep-Alive模式，那么客户端如何知道某一次的响应结束了呢？

**以下有两个方法**

- 如果是静态的响应数据，可以通过判断响应头部中的Content-Length 字段，判断数据达到这个大小就知道数据传输结束了。
- 但是返回的数据是动态变化的，服务器不能第一时间知道数据长度，这样就没有 Content-Length 关键字了。这种情况下，服务器是分块传输数据的，`Transfer-Encoding：chunk`，这时候就要根据传输的数据块chunk来判断，数据传输结束的时候，最后的一个数据块chunk的长度是0。

**使用HTTP建立长连接**

当需要建立 HTTP 长连接时，HTTP 请求头将包含如下内容：

 `Connection: Keep-Alive`

 如果服务端同意建立长连接，HTTP 响应头也将包含如下内容：

 `Connection: Keep-Alive`

 当需要关闭连接时，HTTP 头中会包含如下内容：

 `Connection: Close`

> **慢速攻击**：Http协议中规定，HttpRequest以\r\n\r\n结尾来表示客户端发送结束。攻击者打开一个Http 1.1的连接，将Connection设置为Keep-Alive， 保持和服务器的TCP长连接。然后始终不发送\r\n\r\n， 每隔几分钟写入一些无意义的数据流， 拖死机器。
>
> [cc攻击](https://www.cnblogs.com/sochishun/p/7081739.html#:~:text=CC%E6%94%BB%E5%87%BB%EF%BC%88Challenge,Collapsar%EF%BC%89%E6%98%AFDDOS%EF%BC%88%E5%88%86%E5%B8%83%E5%BC%8F%E6%8B%92%E7%BB%9D%E6%9C%8D%E5%8A%A1%EF%BC%89%E7%9A%84%E4%B8%80%E7%A7%8D%EF%BC%8C%E5%89%8D%E8%BA%AB%E5%90%8D%E4%B8%BAFatboy%E6%94%BB%E5%87%BB%EF%BC%8C%E4%B9%9F%E6%98%AF%E4%B8%80%E7%A7%8D%E5%B8%B8%E8%A7%81%E7%9A%84%E7%BD%91%E7%AB%99%E6%94%BB%E5%87%BB%E6%96%B9%E6%B3%95%E3%80%82)

[来源地址](https://www.jianshu.com/p/49551bda6619)

