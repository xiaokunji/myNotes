[toc]
# parent
现在有这样一个场景，有两个web项目A、B，一个java项目C，它们都需要用到同一个jar包：common.jar。如果分别在三个项目的pom文件中定义各自对common.jar的依赖，那么当common.jar的版本发生变化时，三个项目的pom文件都要改，项目越多要改的地方就越多，很麻烦。这时候就需要用到parent标签, 我们创建一个parent项目，打包类型为pom，parent项目中不存放任何代码，只是管理多个项目之间公共的依赖。在parent项目的pom文件中定义对common.jar的依赖，ABC三个子项目中只需要定义`<parent></parent>`，++parent标签中写上parent项目的pom坐标就可以引用到common.jar了++。

上面的问题解决了，我们在切换一个场景，有一个springmvc.jar，只有AB两个web项目需要，C项目是java项目不需要，那么又要怎么去依赖。如果AB中分别定义对springmvc.jar的依赖，当springmvc.jar版本变化时修改起来又会很麻烦。解决办法是在parent项目的pom文件中使用`<dependencyManagement></dependencyManagement>`将springmvc.jar管理起来，++如果有哪个子项目要用，那么子项目在自己的POM文件中使用++


```
<dependency>

<groupId></groupId>

<artifactId></artifactId>

</dependency>
```

标签中写上springmvc.jar的坐标，不需要写版本号，可以依赖到这个jar包了。这样springmvc.jar的版本发生变化时只需要修改parent中的版本就可以了。

原文：https://blog.csdn.net/qq_41254677/article/details/