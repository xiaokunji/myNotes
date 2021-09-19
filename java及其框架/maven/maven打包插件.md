[toc]

# 1. maven介绍

我们都知道Maven本质上是一个插件框架，具有打包和jar管理的功能.

对于打包来说,它的核心并不执行任何具体的构建任务，所有这些任务都交给插件来完成，例如编译源代码是由maven- compiler-plugin完成的。进一步说，每个任务对应了一个插件目标（goal），每个插件会有一个或者多个目标，例如maven- compiler-plugin的compile目标用来编译位于src/main/java/目录下的主源码，testCompile目标用来编译位于src/test/java/目录下的测试源码。

用户可以通过两种方式调用Maven插件目标。第一种方式是将插件目标与生命周期阶段（lifecycle phase）绑定，这样用户在命令行只是输入生命周期阶段而已，例如Maven默认将maven-compiler-plugin的compile目标与 compile生命周期阶段绑定，因此命令mvn compile实际上是先定位到compile这一生命周期阶段，然后再根据绑定关系调用maven-compiler-plugin的compile目标。第二种方式是直接在命令行指定要执行的插件目标，例如mvn archetype:generate 就表示调用maven-archetype-plugin的generate目标，这种带冒号的调用方式与生命周期无关。

# 2. 生命周期及插件

以下测试用的是maven默认的打包插件(打common包这种), 执行 mvn clean install 　对应的输出

## 2.1 总览生命周期

生命周期（lifecycle）由各个阶段组成，每个阶段由maven的插件plugin来执行完成。生命周期（lifecycle）主要包括**clean**、**resources**、**complie**、**install**、**package**、**testResources**、**testCompile**等，其中带test开头的都是用业编译测试代码或运行单元测试用例的。

![img](https://img-blog.csdn.net/20180515140153420?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3poYW9qaWFudGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

![img](https://img-blog.csdn.net/20180515140202872?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3poYW9qaWFudGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)



由上图可知，各个插件的执行顺序一般是：1：clean、２：resources、３：compile、４：testResources、５：testCompile、６：test、７：jar、８：install。在图中标记的地方每一行都是由冒号分隔的，前半部分是对应的插件，后半部分是插件的执行目标也就是插件执行产生的结果。现在我们来看下上面的pom文件，我们如配置了maven-compiler-plugin这个插件，其它的插件没有配置，但最后项目构建成功，说明maven内置的各种插件，如果pom中没有配置就调用默认的内置插件，如果pom中配置了就调用配置的插件。到此我们理解maven的构建过程或者有更多的人称是打包，就是由各种插件按照一定的顺序执行来完成项目的编译，单元测试、打包、布署的完成。各种插件的执行过程也就构成的maven的生命周期（lifecycle）。生命周期（lifecycle）各个阶段并不是独立的，可以单独执行如mvn clean，也可以一起执行如mvn clean install。而且有的mvn命令其是包括多个阶段的，如mvn compile其是包括了resources和compile两个阶段。下面分别来分析各个阶段需要的插件和输出的结果

> 也就是说,每个步骤的插件包都能指定特定版本和配置其下参数

## 2.2 打包插件

这个插件是把class文件、配置文件打成一个jar(war或其它格式)包。依赖包是不在jar里面的，需要建立lib目录，且jar和lib目录在同级目录。常用的打包插件有maven-jar-plugin、maven-assembly-plugin、maven-shade-plugin三种，下面分别介绍下各自己pom配置和使用特点。

### 2.2.1 maven-jar-plugin
　　可执行jar与依赖包是分开，需建立lib目录里来存放需要的j依赖包，且需要jar和lib目录在同级目录, 这也是默认打包方式,所以打common包不写插件也能打包

### 2.2.2  maven-assembly-plugin

这个插件可以把所有的依赖包打入到可执行jar包。需要在pom文件的plugin元素中引入才可以使用，功能非常强大，是maven中针对打包任务而提供的标准插件。它是Maven最强大的打包插件，它支持各种打包文件格式，包括zip、tar.gz、tar.bz2等等，通过一个打包描述文件设置（src/main/assembly.xml），它能够帮助用户选择具体打包哪些资源文件集合、依赖、模块，甚至本地仓库文件，每个项的具体打包路径用户也能自由控制。

但是该插件有个bug会缺失spring的xds文件，导致无法运行jar，同时如果同级目录还有其它可执行jar文件依赖可能会产生冲突。

### 2.2.3 maven-shade-plugin
需要在pom文件的plugin元素中引入才可以使用，它可以让用户配置Main-Class的值，然后在打包的时候将值填入/META-INF/MANIFEST.MF文件。关于项目的依赖，它很聪明地将依赖的JAR文件全部解压后，再将得到的.class文件连同当前项目的.class文件一起合并到最终的CLI包(可以直接运行的jar包)中，这样，在执行CLI JAR文件的时候，所有需要的类就都在Classpath中了。(springboot打包插件用的就是这个)

[如何选用这几个插件](https://link.jianshu.com?t=http://stackoverflow.com/questions/38548271/difference-between-maven-plugins-assembly-plugins-jar-plugins-shaded-plugi)

- 如果在开发一个库，直接使用默认的maven-jar-plugin插件即可；

- 如果是开发一个应用程序，可以考虑使用maven-shade-plugin进行打包生成Über jar(Über jar是将应用程序打包到单独的jar包中，该jar包包含了应用程序依赖的所有库和二进制包)

- 如果打包生成了Über jar都不能满足你的需求的话，那么推荐使用maven-assembly-plugin插件来自定义打包内容。




### 2.2.4 maven-war-plugin

war项目默认的打包工具，默认情况下会打包项目编译生成的.class文件、资源文件以及项目依赖的所有jar包。

#### 2.2.4.1 jar和war

1、war是一个web模块，其中需要包括WEB-INF，是可以直接运行的WEB模块；jar一般只是包括一些class文件，在声明了Main_class之后是可以用java命令运行的。

2、war包是做好一个web应用后，通常是网站，打成包部署到容器中；jar包通常是开发时要引用通用类，打成包便于存放管理。

3、war是Sun提出的一种Web应用程序格式，也是许多文件的一个压缩包。这个包中的文件按一定目录结构来组织；classes目录下则包含编译好的Servlet类和Jsp或Servlet所依赖的其它类（如JavaBean）可以打包成jar放到WEB-INF下的lib目录下。

> 1.我的一个springboot项目，用mvn install打包成jar，换一台有jdk的机器就直接可以用java -jar 项目名.jar的方式运行，没任何问题，为什么这里不需要tomcat也可以运行了？
>
> 答: 通过jar运行实际上是启动了内置的tomcat,所以用的是应用的配置文件中的端口
>
> 2.然后我打包成war放进tomcat运行，发现端口号变成tomcat默认的8080（我在server.port中设置端口8090）项目名称也必须加上了。
> 也就是说我在原来的机器的IDEA中运行，项目接口地址为 ip:8090/listall,打包放进另一台机器的tomcat就变成了ip:8080/项目名/listall。这又是为什么呢？
>
> 答:  直接部署到tomcat之后，内置的tomcat就不会启用，所以相关配置就以安装的tomcat为准，与应用的配置文件就没有关系了







## 2.8  maven-source-plugin 插件

用来打源代码包的, 就像spring写的包,我们要下源代码来看注释,我们自己写的包有时也需要打出源码包给别人用,打完只有就会有一个`-sources`后缀的包

```xml
		<plugin>  
   			<groupId>org.apache.maven.plugins</groupId>  
              <artifactId>maven-source-plugin</artifactId>  
              <version>3.0.0</version>  
              <configuration>  
                  <attach>true</attach>  
              </configuration>  
              <executions>  
                  <execution>  
                      <phase>compile</phase>  
                      <goals>  
                          <goal>jar</goal>  
                      </goals>  
                  </execution>  
              </executions>  
          </plugin>  
```

# 3. springboot打包插件

spring-boot-maven-plugin插件在Maven中提供了对Spring Boot的支持，可以帮助我们打包出可执行的jar包或者war包。其实spring-boot-maven-plugin所做的工作是在默认的maven-jar-plugin插件打包结束后，将项目依赖的jar包中的.class文件重新进行打包。

```
[INFO] 
[INFO] --- maven-jar-plugin:2.6:jar (default-jar) @ helloworld ---
[INFO] Building jar: /Users/gaozengrong/IdeaProjects/helloworld/target/helloworld-1.0-SNAPSHOT.jar
[INFO] 
[INFO] --- spring-boot-maven-plugin:1.5.2.RELEASE:repackage (default) @ helloworld ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 26.357 s
[INFO] Finished at: 2017-03-19T17:51:33+08:00
[INFO] Final Memory: 33M/289M
[INFO] ------------------------------------------------------------------------
```



可以看出，在调用maven-jar-plugin的goal:jar任务打包之后，又调用了spring-boot-maven-plugin的goal:repackage任务，这样会产生两个jar包。在helloworld这个工程里分别对应`helloworld-1.0-SNAPSHOT.jar.original`(maven-jar-plugin打包生成的jar包)，`helloworld-1.0-SNAPSHOT.jar`(spring-boot-maven-plugin重新打包生成的可执行jar包)。



> 它使用的打包插件是` maven-shade-plugin`
>
> ![image-20210402184511005](C:\Users\gree\AppData\Roaming\Typora\typora-user-images\image-20210402184511005.png)

# 4.实际操作

## 4.1 一个可用的打包插件配置, 插件外置且打出可发布的包

```xml
<build>
    <plugins>
         <!--springboot自带的-->
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
        <!-- 打包 -->
        <!-- 使用这个插件打包 -->
       <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-jar-plugin</artifactId>
            <configuration>
             <!-- 在1.8的环境下-->
            	<source>1.8</source>
            	<target>1.8</target>
            	<encoding>UTF-8</encoding>
            	<!--排除这些文件-->
            	<excludes>
            	    <exclude>*.properties</exclude>
            		<exclude>*.xml</exclude>
            	</excludes>
            </configuration>
        </plugin>
        <!-- 再使用这个插件打包 -->      <!-- 允许打多个包 -->
        <plugin>
            <artifactId>maven-assembly-plugin</artifactId>
            <executions>
            	<execution>
            		<id>make-zip</id>
            		<phase>package</phase>
            		<goals>
            			<goal>single</goal>
            		</goals>
            		<!-- 确定包名,输出路径,用assembly.xml文件 -->
            		<configuration>
            			<finalName>${project.artifactId}</finalName>
            			<outputDirectory>target</outputDirectory>
            			<descriptors>
            				<descriptor>src/assembly/assembly.xml</descriptor>
            			</descriptors>
            		</configuration>
            	</execution>
            </executions>
        </plugin>
        <!-- 再使用这个插件打包 -->  
         <plugin>
                 <artifactId>maven-resources-plugin</artifactId>
                  <executions>
                      <execution>
                          <id>copy-xmls</id>
                          <phase>process-sources</phase>
                          <goals>
                              <goal>copy-resources</goal>
                          </goals>
                          <configuration>
                              <outputDirectory>${basedir}/target/classes</outputDirectory>
                              <resources>
                                  <resource>
                                      <directory>${basedir}/src/main/java</directory>
                                      <includes>
                                          <include>**/*.xml</include>
                                      </includes>
                                  </resource>
                              </resources>
                          </configuration>
                      </execution>
                  </executions>
            </plugin>   
    </plugins>
</build>
```



## 4.2 引用可执行jar的方式

使用springboot插件打出来的包无法被其他应用程序引用,应该有多个可执行jar了(maven默认的可以被引用),要想被使用采用如下方式

```xml
<plugins>
  # 这是 springboot的打包插件,就是常说的服务包,它带了各种依赖和启动类,用这个插件打出来的包不能为其他服务包pom引用
    <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
            <executable>true</executable>
        </configuration>
    </plugin>
</plugins>

# 如果要使用,就再打一个普通包,用于引用,改写成如下:
    关键是<classifier>标签,这是新jar的后缀
<plugins>
    <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
            <!--  额外打包普通jar用于 activity-consumer服务-->
            <mainClass>com.gree.ecommerce.ActivityServiceApplication</mainClass>
            <classifier>OfConsumer</classifier>
            <executable>true</executable>
        </configuration>
    </plugin>
</plugins>
```

>  使用的时候正常用,这是找了"漏洞",打出来的包会发布到仓库中,平常部署运行时用的是target下的,这是可运行的,而且引用时用的仓库里的jar,这又是被额外生成的 https://www.cnblogs.com/kingsonfu/p/11805455.html





参考链接

[插件介绍](https://zhuanlan.zhihu.com/p/70596302)

[打包生命周期](https://blog.csdn.net/zhaojianting/article/details/80321488)

[maven-source-plugin使用](https://blog.csdn.net/j080624/article/details/78353634)

[maven体系](http://c.biancheng.net/view/4951.html)

[springboot打包插件](https://www.jianshu.com/p/93888e19db7e)

