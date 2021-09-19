1. 配置:

- 1.  ~ 为区分大小写匹配
  2.  ~* 为不区分大小写匹配
  3.  !~和!~*分别为区分大小写不匹配及不区分大小写不匹配

> 来自* *<*[*https://www.cnblogs.com/xuey/p/7631690.html*](https://www.cnblogs.com/xuey/p/7631690.html)*>*



```
location ~ .*\.(sh|bash)?$ {   # 拦截sh和bash结尾的访问,并返回403状态码
return 403;
}
 
# 如果localhost  后的地址有冲突 ,比如还有以下,则匹配最长的(也就是最精准的),所以上面这个location会起作用
location /  {  # 拦截所有的访问,并返回200状态码
return 200;
}
 
# 用等号是最精准的
location =  /index.html  {  # 拦截index的访问,并返回200状态码
return 200;
}
 
 
```

