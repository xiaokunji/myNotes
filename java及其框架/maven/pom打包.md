 目录结构

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



**运行:**  直接install,就行了,

**结果:**

![img](https://gitee.com/xiaokunji/my-images/raw/master/myMD/20210712003145.png)



文件:   [assembly.xml](https://gitee.com/xiaokunji/my-images/raw/master/myMD/assembly.xml) 





```xml
# 这是打普通包的插件, 常用于common包,skenton包等公共包,,没有启动类,且结构与服务包不一样,可以被其他服务包引用
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-source-plugin</artifactId>
    </plugin>
</plugins>
```



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
    关键是<classifier>标签,这是新jar的后缀,新包是可执行的,原包名是可引用的普通jar
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



参见 : https://www.cnblogs.com/kingsonfu/p/11805455.html