settings.xml  中的参数看文件中的描述即可

**仓库和镜像的区别**

其中有两个标签, <repository>,<mirror>, 它们写的东西差不多,前者是仓库,后者是仓库

​	repository是指在局域网内部搭建的repository，它跟central repository, jboss repository等的区别仅仅在于其URL是一个内部网址 .
​	mirror则相当于一个代理，它会拦截去指定的远程repository下载构件的请求，然后从自己这里找出构件回送给客户端。配置mirror的目的一般是出于网速考虑

不过，很多internal repository搭建工具往往也提供mirror服务，比如Nexus就可以让同一个URL,既用作internal repository，又使它成为所有repository的mirror

```xml
<settings>
  ...
  <mirrors>
    <mirror>
      <id>maven.net.cn</id>
      <name>one of the central mirrors in china</name>
      <url>http://maven.net.cn/content/groups/public/</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
  ...
</settings>
```

<mirrorOf>表示该镜像所指向的仓库id是哪个?

该例中，<mirrorOf>的值为central，表示该配置为中央仓库的镜像，任何对于中央仓库的请求都会转至该镜像，用户也可以使用同样的方法配置其他仓库的镜像。另外三个元素id,name,url与一般仓库配置无异，表示该镜像仓库的唯一标识符、名称以及地址

Maven还支持更高级的镜像配置： 

1. <mirrorOf>*</mirrorOf> 
   匹配所有远程仓库。 
2. <mirrorOf>external:*</mirrorOf> 
      匹配所有远程仓库，使用localhost的除外，使用file://协议的除外。也就是说，匹配所有不在本机上的远程仓库。 
3. <mirrorOf>repo1,repo2</mirrorOf> 
      匹配仓库repo1和repo2，使用逗号分隔多个远程仓库。 
4. <mirrorOf>*,!repo1</miiroOf> 
      匹配所有远程仓库，repo1除外，使用感叹号将仓库从匹配中排除。

这里的仓库配置,指的在pom.xml中配置的; 好像在<profiles>也能配置

```xml
<profiles>
    <profile>
        <repositories>
				<repository>
                    <id>jdk14</id>
                    <name>Repository for JDK 1.4 builds</name>
                    <url>http://www.myhost.com/maven/jdk14</url>
                </repository>

            </repositories>
        </profile>
    </profiles>
```

