[toc]

# **一.简易操作**

## **1.安装docker,jdk**

## **2.制作项目镜像**

1.项目用mvn打包即可, (mvn install) 项目

2.在 项目.jar 包同一个目录下新建(放一起方便管理嘛)

3.Dockerfile文件,并写入:

```
# Docker image for springboot file run
# VERSION 0.0.1
# 基础镜像使用java
# 下载jdk8 ,没有才下载
FROM java:8
 
# VOLUME 指定了临时文件目录为/tmp。
# 其效果是在主机 /var/lib/docker 目录下创建了一个临时文件，并链接到容器的/tmp
VOLUME /tmp
 
# 将jar包添加到容器中并更名为app.jar
ADD SpringBootTest-0.0.1-SNAPSHOT.jar app.jar
 
# 运行jar包
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```



> 来自 <https://blog.csdn.net/Sirius_hly/article/details/83685256>

 

4. 编译镜像

docker build -t spring-boot-docker . #末尾的点代表文件所在目录执行,指上下文目录,应该是可以指定目录的,虽然没试过 docker images # 可以查看制作好的镜像 

可以上传镜像,搭环境时只需要pull就行

 

## **3.安装Mysql**

```
docker run -it --rm --name mysql -v /usr/conf.d:/usr/mysql/conf.d   -e MYSQL_ROOT_PASSWORD=123456 -p 3306:3306 -d mysql
注:  -i  -t  :  大致是运行镜像后能接受输入流(就是能往镜像里输东西,等会要初始化数据嘛)
       --rm  :  如果名字叫mysql 的已存在,则删除
       -p   :   宿主端口:镜像端口   ,  即 : 虚拟机的3306端口 对应到 镜像里的3306端口
       -d   :  后台启动并返回镜像ID
       -v  :  本地文件夹:镜像文件夹, 将本地文件夹映射到镜像里,这样就能是使用外部配置文件了
```



 

## **4.配置数据库**

\#进入mysql容器 docker exec -it mysql bash  #输入用户名密码 mysql -u root -p123456  #设置外部网络访问mysql权限 ALTER user 'root'@'%' IDENTIFIED WITH mysql_native_password BY '123456'; FLUSH PRIVILEGES;  #创建用户 CREATE USER 'test'@'%' IDENTIFIED BY 'Jht123456'; #授权 GRANT ALL ON *.* TO 'test'@'%'; FLUSH PRIVILEGES;  #切换用户(不切换也行,貌似数据是共通的) exit && exit mysql -u test -pJht123456  # 创建数据库 CREATE DATABASE IF NOT EXISTS jplatdvv default charset utf8; #切换数据库 use jplatdvv  #创建表和插入数据 (linux 下mysql是大小写敏感的,可以在配置文件中指定不敏感) create table jpf_help_info.....(略) 

## **5.启动项目**

docker run --name spring-boot-docker -d -p 8080:8080 --link mysql:mysql spring-boot-docker # --link 将两个镜像连接起来,  mysql使用了别名(冒号后面是别名,虽然是本身),这样在数据库url中不用写ip,直接写mysql 

这样就能访问了,

http://192.168.142.128:8998/test/getHelp

 

 [SpringBootTest.zip 模拟代码](..\..\MDImages\SpringBootTest.zip) 

使用docker-compose工具能更好的使用docker