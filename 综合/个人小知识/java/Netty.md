Netty是由JBOSS提供的一个java开源框架，现为 Github上的独立项目。Netty提供异步的、事件驱动的网络应用程序框架和工具，用以快速开发高性能、高可靠性的网络服务器和客户端程序。
也就是说，Netty 是一个基于NIO的客户、服务器端的编程框架，使用Netty 可以确保你快速和简单的开发出一个网络应用，例如实现了某种协议的客户、服务端应用。Netty相当于简化和流线化了网络应用的编程开发过程，例如：基于TCP和UDP的socket服务开发。
> 简单的说,可以用来做网络编程,取代websocket,
其底层使用NIO(new IO:同步非阻塞IO, 这玩意听说贼难)


netty能够受到青睐的原因有三：
1. 并发高
2. 传输快
3. 封装好

并发高是因为非阻塞接收,使用多线程接收   

传输快是因为 使用了零拷贝   

封装好是因为 封装了NIO,让网络编程变得简单(但还是太难...)

> 来源: https://www.jianshu.com/p/b9f3f6a16911

> NIO的零拷贝和kafka的零拷贝应该是一样的,零拷贝其实是分类型的,总的来说是减少了数据拷贝的次数  
零拷贝分类:https://www.ibm.com/developerworks/cn/linux/l-cn-zerocopy1/index.html