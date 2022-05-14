在使用上的差别: 

mysql中 where 条件允许 1,0,true,false 条件,而Oracle只允许true/false,

例如 : select * from person where 1  ,相当于where 1=1 ,是个恒等式

 

| 事项       | MySQL                                    | Oracle                                                  | 注释                                                         | 例子                                                         |
| ---------- | ---------------------------------------- | ------------------------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| where      | 支持1,0,true,false                       | 支持true/false                                          |                                                              | mysql : select * from person where 1Oracle: select * from person where 1=1 |
| as         | 表和字段都能用,也可以不用                | 仅字段能用                                              | 都支持不写as,直接写别名就行                                  |                                                              |
| 当前时间   | SYSDATE()                                | SYSDATE                                                 |                                                              | Oracle:SELECT SYSDATE FROM JBP_ACCOUNT                       |
| 判空       | IFNULL                                   | nvl                                                     |                                                              | oracle: nvl( LOCK_FLAG,'') as lockFlag,                      |
| 连接字符串 | concat(str1.str2,str3,.....) 不定参数    | concat(str1,str2) 仅两个参数, 可以用\|\| 任意连接字符串 |                                                              | eg. '3'\|\| id \|\| '4'                                      |
| 时间格式化 | DATE_FORMAT(#{time} , %Y-%m-%d %H:%i:%s) | TO_CHAR(yyyy-MM-dd hh24:mi:ss)                          |                                                              |                                                              |
| 查找字符串 | find_in_set(a,b)                         | 可用替代 instr                                          | find_in_set查找a在b中的位置,b是又逗号隔开的字符串,用到了比特运算,速度很快 |                                                              |
| 分组并合并 | GROUP_CONCAT                             | wm_concat                                               |                                                              |                                                              |
|            |                                          |                                                         |                                                              |                                                              |
|            |                                          |                                                         |                                                              |                                                              |
|            |                                          |                                                         |                                                              |                                                              |