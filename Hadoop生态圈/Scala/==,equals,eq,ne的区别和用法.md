
**equals**

**比较的是值**  , 和java一样,利用hashcode()方法进行比较
例如`("he"+"llo")`

**eq**   
**比较的是引用**,比较的对象的引用地址

**ne**  
是`eq`的反义

**==**   
当要比较的值是否null,   
如果为null则使用`eq`,   
如果不为null,则使用`equals`

> https://blog.csdn.net/do_yourself_go_on/article/details/72758380