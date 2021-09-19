**字节流: 大多以Stream结尾**


![字节流](https://p-blog.csdn.net/images/p_blog_csdn_net/fenglian521/javaio.bmp)



**字符流:大多以Reader和Writer结尾**

![img](https://p-blog.csdn.net/images/p_blog_csdn_net/fenglian521/javaio2.bmp)



## 字符流与字节流的区别

  经过以上的描述，我们可以知道字节流与字符流之间主要的区别体现在以下几个方面：

- 字节流操作的基本单元为字节；字符流操作的基本单元为Unicode码元。
- 字节流默认不使用缓冲区；字符流使用缓冲区。
- 字节流通常用于处理二进制数据，实际上它可以处理任意类型的数据，但它不支持直接写入或读取Unicode码元；字符流通常处理文本数据，它支持写入及读取Unicode码元。

> 参考地址:
>
> [字符流和字节流简述](https://www.cnblogs.com/absfree/p/5415092.html)
>
> [IO类图](https://blog.csdn.net/fenglian521/article/details/1324010)