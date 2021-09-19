==写得很乱,以后知识体系完善了,再整理==

---



操作系统分类:

1. linux :
   	derbin,ubuntu,redhat,contos,fcortor,redflag,suse
   
2. unix:

3. window NT:   
4. MAC :


linux由Linux内核,系统基本库,应用程序组成

1. 设置Ubuntu的静态ip地址    
	`/etc/network/interfaces`   //ip地址文件路径    
	`sudo /etc/init.d/networking restart  ` //重启网卡   

修改如下(静态ip):
```
auto ens32
iface ens32 inet static
address 192.168.22.127
netmask 255.255.255.0
gateway 192.168.22.254
```

DNS设置:  
- 临时设置:`/etc/resolv.conf`   
- 永久设置:`/etc/resolvconf/resolv.conf.d/base`
		
		

使用 lrzsz  
可以实现window与Ubuntu之间的文件上传与下载    
	`sudo apt-get install lrzsz`
	

Linux命令:   
`shutdown -h now` 立刻关机   

`shutdown -r now` 立刻重启   

`cd`

`pwd`   

`ls`      

`grep`(筛选)   

`rm` 删除文件   

`rm -rf`	删除目录   

`mv  旧名字  新名字`  	//重命名文件   

`mv 	旧位置	新位置`    //移动文件   

`cp 旧文件 	新位置`  	拷贝文件   

`cp -r 旧目录	新位置` 	拷贝目录   

`ps -elf | grep lrzrs`   可以查看某个服务是否存在  

`history`  列出所有历史命令   

> !+命令所在行  就可执行    eg:!23     

`ln -s  源文件/目录  软连接名`  为源文件创建一个软连接   

`ln  源文件  连接名` 为源文件创建一个硬连接(目录不可以建立硬连接)

`which sshd`  	 //查找是否安装了sshd这个软件   

`ifconfig` 		//查看ip地址

`less`  分页显示文件内容

> -l 显示行号
>
> -S 不换行显示
>
> 进入文本显示(与vim的命令模式类似): 
>
> F  进入tailf 命令模式
>
> v  进入vim的编辑模式

`more`   分页显示文件内容

> 按enter键:	每次显示一行   
	按空格(space)键:	下一页   
	ctrl+b :  上一页   
	ctrl+f    
	按 "/" 查找,按n下一个找到的值,N上一个找到的值   

`head` 	: 查看头部分信息   

> eg.: `head -n5  xx.txt`
>
> 显示前5行数据    默认显示前十行

`tail`:	查看尾部分信息   

> eg.: `tail -n5  xx.txt `     

	显示前5行数据    默认显示前十行
	-f : 可以暂停文档,当改变时会实时显示,用于日志分析

`wc` :统计   

`"|" ` 管道符 把前一个的指令运行结果给后面的指令执行   
`"||"`    连接两条命令  上一条执行失败了才会执行下一条   
`"&&"`  连接两条命令  上一条执行成功了才会执行下一条   
`"&"`    连接两条命令  不关心失败与否,所有命令都会执行   
`">>",">"` 重定向符  把前一个的指令运行结果给(符号右边)一个文件    前者是追加,后者是覆盖

>当要写多行命令时,可以使用"\"或者"|"来连续输入

vi 和 vim  

有三种模式:

- 命令模式(只能输入命令执行,比如要退出vim,在界面的最下面,)
- 编辑模式(可以进行字符操作)
- 一般模式(可以删除行,或者复制粘贴等操作)
> 刚进入时是一般模式


- 一般模式 -->编辑模式  : 
    - a(在光标之后添加)  
    - i(光标前添加)  
    - o(光标下一行)   
    > (大写字母效果则相反且范围变大)

- 一般模式 --> 命令模式  : 
    - :
    - /
    - ?
- 命令模式,编辑模式 -->一般模式  : 
    - esc
				
> (命令可以组合使用  eg:  d表示删除,G表示跳到文件尾部,组合起来dG就是删除到文件尾部)

| 命令        | 备注                                       | 扩展                                                         |
| ----------- | :----------------------------------------- | :----------------------------------------------------------- |
| :wq 或者 ZZ | 保持并退出                                 | w表示保存, q表示退出                                         |
| :q!         | 强制退出(不保存)                           |                                                              |
| ctrl+f      | 下一页                                     |                                                              |
| ctrl+b      | 上一页                                     |                                                              |
| gg          | 跳到文件首位                               | 5gg: 跳到第5行                                               |
| G           | 跳到文件尾部                               | 5G:跳到第5行                                                 |
| ^           | 跳转光标行首                               |                                                              |
| $           | 跳转光标行尾                               |                                                              |
| :set nu     | 显示行号                                   |                                                              |
| dd          | 删除当前行(其实是放入一个剪切板,用p可粘贴) | 7dd: 删除光标初开始的后面的7行                               |
| dw          | 删除一个单词                               | d3w 表示删除3个单词                                          |
| yy          | 复制当前行                                 |                                                              |
| p           | 粘贴                                       |                                                              |
| r           | 剪切                                       |                                                              |
| x           | 删除光标后的一个字符                       |                                                              |
| u           | 撤销                                       |                                                              |
| ctrl+r      | 返回撤销                                   |                                                              |
| :r          | filepath                                   | 复制一个文件到当前光标出                                     |
| :s          | /old/new                                   | 在当前行中用new字符代替old字符                               |
| :s          | /old/new/cg                                | 同上, 外加c 表示对每个替换动作提示用户进行确定;<br />g 表示当前行查找并替换所有字符串 |
| :%s         | /old/new                                   | 该文件中用new字符代替old字符                                 |

> https://mssora.com/archives/182



`useradd/adduser 用户名` ->添加用户   

`userdel 用户名`  ->删除用户   

`passwd  用户名`  -> 修改密码   

`usermod  -l newname  oldname`  -> 修改用户名   

`adduser   用户名 组名` -> 更改用户权限(例如加入组,管理员组名:sudo)   

`jps`  查看(java启动的)后台进程   

`crontab 定时任务(-e,-l)`      

`sudo vim /etc/crontab`(系统的定时任务文件)   


linux知识:
	环境变量配置

 - 1.系统配置
    - /etc/profile
	- /etc/environment
- 2.用户环境变量(运行时优先于系统配置)
	- ~/.profile
	- ~/.bashrc
		
	
	快捷键:
		ctrl+alt+t 	 	//打开终端(桌面版)

( "#" 注释符号 )    
用户账户文件   `/etc/passwd`   
用户密码文件	`/etc/shadow`    
用户组信息		`/etc/group`   
管理ip地址与主机的映射		`/etc/hosts`   
主机名文件		`/etc/hostname`	   

`~/.profile`:用户每次登录时运行

`~/.bashrc`:每次进入新的Bash环境时执行(打开一个新的终端)

`~/.bash_login`:用户每次退出登录时执行
	
分区:
	bin boot sbin bin lib64 etc   
	这些文件夹一般放在一个分区中	   
	一种做为服务器:   
	一种做为客户端:   
	swap(交换空间):虚拟内存   
	boot:启动项目录  
	

- bin  sbin 是执行指令所在目录
- etc  	系统配置文件
- dev 	设备文件目录
- lib lib64	库文件
- home 		个人(用户)主目录,一般个人主目录名与用户名一致..	普通用户除了操作个人主目录外,默认情况下,不能操作(增删改)其他的文件

识别个人主目录  
`~` 表示个人主目录

  - `$`	 表示普通用户

  - `#`     表示管理员

    

 识别系统的版本   
	`unmae -m`		 

> x86_64->64位 ; i586 -> 32位   

`uname -a`   系统所有信息


`lsattr`:显示的是文件系统的物理属性,由linux内核支持
> ls显示的是文件系统的逻辑属性	

`lsattr a.txt`  查看文件隐藏属性    
`chattr -i  a.txt`	撤销文件某个隐藏属性

`ls -l`  显示列表(可以简写成ll)		
> 隐藏文件是以 "."  开头

drwxr-xr-x  24 root   root    4096 Mar 10 16:49 ./   
- 第一组:
  - 第一个字符:	
	  - "-" 开头表示文件,
	  - "d" 开头表示目录,
	  - "l" 开头表示软连接,可以认为是快捷方式
    - 2~4个字符表示:文件拥有者对文件拥有的操作权限		u
    - 5~7个字符表示:用户所属组对文件拥有的操作权限		g
    - 8~10个字符表示:其他用户对文件拥有的操作权限		o
	
- 第三组字符:
	- 表示所有者(这里表示该文件是root所有)
- 第四组字符:
	- 表示文件所属组


chmod  : 
- `+`  表示添加权限    ; 
- `-` 表示删除权限	;	
- `u` 表示文件拥有者	;	
- `g` 表示文件所属组	;	
- `o`  表示其他用户    ;
- `a`  表示ugo三者;

用二进制表示是否有权限,所有可能如下:
	
| 001  | 010  | 011  | 100  | 101  | 110  | 111  |
| ---- | ---- | ---- | ---- | ---- | ---- | ---- |
| 1    | 2    | 3    | 4    | 5    | 6    | 7    |

进而可以用十进制表示(如上)  
或者记住: 读等于4; 写等于2; 执行等于1

> 注: r表示可读,w表示可写,x表示可执行


`chown` :   改变所有者[和改变所属组]
`chgrp`:		改变所属组

`find` 命令:
`find [path] -name "pattern " 2>/dev/null  ` 

> 按文件名查找,并不输出错误信息(就是这个Permission denied)

`find  /home  | xargs grep -i "start" `  
> 在home路径下所有文件中查找start字符串  (贼好用)
> 直接用 grep 也是可以的,  
> 语法: grep [OPTION]... PATTERN [FILE]...    
> `grep -n "start" /home `   
>
> -r 参数表示递归查询
>
> 查找/home下文件中含"start" 字符串的文件名和对应行  

`find . -mtime +10 -name *.log | xargs rm -f`

> 删除十天以前的log文件   

`find . -type f -size +150M | xargs rm -f`
> 删除文件大小超过150M的 文件

`du`  查看文件大小

`du -sh ~/*  | sort -nr` 

> 查询指定目录下文件夹大小(包含其下文件),并排序

`sed`  文本操作工具

`sed [-nefri] ‘command’ 输入文本`        

常用选项：
- -n∶使用安静(silent)模式。在一般 sed 的用法中，所有来自 STDIN的资料一般都会被列出到萤幕上。但如果加上 -n 参数后，则只有经过sed 特殊处理的那一行(或者动作)才会被列出来。
- -e∶直接在指令列模式上进行 sed 的动作编辑；
- -f∶直接将 sed 的动作写在一个档案内， -f filename 则可以执行 filename 内的sed 动作；
- -r∶sed 的动作支援的是延伸型正规表示法的语法。(预设是基础正规表示法语法)
- -i∶直接修改读取的档案内容，而不是由萤幕输出。       

常用命令：
- a   ∶新增， a 的后面可以接字串，而这些字串会在新的一行出现(目前的下一行)～
- c   ∶取代， c 的后面可以接字串，这些字串可以取代 n1,n2 之间的行！
- d   ∶删除，因为是删除啊，所以 d 后面通常不接任何咚咚；
- i   ∶插入， i 的后面可以接字串，而这些字串会在新的一行出现(目前的上一行)；
- p  ∶列印，亦即将某个选择的资料印出。通常 p 会与参数 sed -n 一起运作～
- s  ∶取代，可以直接进行取代的工作哩！通常这个 s 的动作可以搭配正规表示法！例如 1,20s/old/new/g 就是啦！
 > 举例:   
 > `sed '1,2d' text.txt`   #删除第一行到第二行   
 > `sed -n '1,2p' a.txt`   #显示第一行到第二行   
 > `sed -n '/ruby/p' ab | sed 's/ruby/bird/g'`    #替换ruby为bird   
 > `sed -n '1,/'"${keyword}"'/p' a.txt`  # 显示第一行到出现关键词的一行,keyword是变量   
 > `sed -n '1,/xiaokj/p' a.txt`  # 显示第一行到出现关键词的一行
 >
 > 来自:https://www.cnblogs.com/dong008259/archive/2011/12/07/2279897.html

`xargs`  管道命令

- xargs 是给命令传递参数的一个过滤器，也是组合多个命令的一个工具。
- xargs 可以将管道或标准输入（stdin）数据转换成命令行参数，也能够从文件的输出中读取数据。
- xargs 也可以将单行或多行文本输入转换为其他格式，例如多行变单行，单行变多行。e.g.`cat test.txt | xargs`  `cat test.txt | xargs -n3`
- xargs 默认的命令是 echo，这意味着通过管道传递给 xargs 的输入将会包含换行和空白，不过通过 xargs 的处理，换行和空白将被空格取代。
- xargs 是一个强有力的命令，它能够捕获一个命令的输出，然后传递给另外一个命令。

之所以能用到这个命令，关键是由于很多命令不支持|管道来传递参数，而日常工作中有有这个必要，所以就有了 xargs 命令

`somecommand |xargs -item  command`

- -a file 从文件中读入作为sdtin
- -e flag ，注意有的时候可能会是-E，flag必须是一个以空格分隔的标志，当xargs分析到含有flag这个标志的时候就停止。
- -p 当每次执行一个argument的时候询问一次用户。
- -n num 后面加次数，表示命令在执行的时候一次用的argument的个数，默认是用所有的。
- -t 表示先打印命令，然后再执行。
- -i 或者是-I，这得看linux支持了，将xargs的每项名称，一般是一行一行赋值给 {}，可以用 {} 代替。e.g.:` cat arg.txt | xargs -I {} ./sk.sh -p {} -l`,可以给每行批量加数据
- -r no-run-if-empty 当xargs的输入为空的时候则停止xargs，不用再去执行了。
- -s num 命令行的最大字符数，指的是 xargs 后面那个命令的最大命令行字符数。
- -L num 从标准输入一次读取 num 行送给 command 命令。
-l 同 -L。
- -d delim 分隔符，默认的xargs分隔符是回车，argument的分隔符是空格，这里修改的是xargs的分隔符。e.g.:`echo "nameXnameXnameXname" | xargs -dX`
- -x exit的意思，主要是配合-s使用。。
- -P 修改最大的进程数，默认是1，为0时候为as many as it can ，这个例子我没有想到，应该平时都用不到的吧。
- 

用 rm 删除太多的文件时候，可能得到一个错误信息：/bin/rm Argument list too long. 用 xargs 去避免这个问题：   
e.g.:`find . -type f -name "*.log" -print0 | xargs -0 rm -f`
> xargs -0 将 \0 作为定界符。
>
> 来自:https://www.runoob.com/linux/linux-comm-xargs.html

`AWK`是一种处理文本文件的语言，是一个强大的文本分析工具。   

每行(默认)按空格或TAB分割，输出文本中的1、4项   

`awk '{print $1,$4}' log.txt`   

关闭名字叫jportal的进程   

`ps -ef | grep jportal | awk {'print $2'} | xargs kill -9`   

> `ps -ef | grep jportal | awk '{print "kill -9 "$2}' | sh`
>
> 用这种更好,不会因为重复执行而报错

格式化输出   
`awk '{printf "%-8s %-10s\n",$1,$4}' log.txt`   
使用","分割   
`awk -F, '{print $1,$2}'   log.txt`

> awk -F  #-F相当于内置变量FS, 指定分割字符

使用多个分隔符.先使用空格分割，然后对分割结果再使用","分割    
`awk -F '[ ,]'  '{print $1,$2,$5}'   log.txt`    
过滤第一列大于2并且第二列等于'Are'的行   
`awk '$1>2 && $2=="Are" {print $1,$2,$3}' log.txt`   
输出第二列包含 "th"，并打印第二列与第四列   
`awk '$2 ~ /th/ {print $2,$4}' log.txt`   

关于awk脚本，我们需要注意两个关键词BEGIN和END。
- BEGIN{ 这里面放的是执行前的语句 }
- END {这里面放的是处理完所有的行后要执行的语句 }
- {这里面放的是处理每一行时要执行的语句}
> 还有好多,这个命令真是超牛逼,以后想看了再看
>
> 来自:https://www.runoob.com/linux/linux-comm-awk.html



`tar` 压缩和解压命令

`tar -czvf test.tar.gz a.txt `
> 把a.txt文件压缩成test.tar.gz

`tar -xzvf test.tar.gz `

> 解压test.tar.gz



`flock` 文件锁命令

flock 命令最大的用途就是实现对 crontab 任务的串行化。

`* * * * * flock -xn /tmp/mytest.lock -c '/home/fdipzone/bin/test.sh start'`

> 这里的定时任务是每分钟执行一次，但是任务中并未直接执行目标命令 ‘/home/fdipzone/bin/test.sh start’ ，而是将命令作为 flock 的 -c 选项的参数。flock 命令中，-x 表示对文件加上排他锁，-n 表示文件使用非阻塞模式，-c 选项指明加锁成功后要执行的命令。
>
> 因而上面flock 命令的整体含义就是：如果对 /tmp/mytest.lock 文件（如果文件不存在， flock 命令会自动创建）加锁成功就执行后面的命令，否则不执行。
>
> 来源: https://zhuanlan.zhihu.com/p/25134841