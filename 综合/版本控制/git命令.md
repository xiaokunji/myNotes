[toc]



# 1. 初始化git和github

1. 先在github上新建一个项目

2. `ssh-keygen -t rsa -C "1767822853@qq.com"` //123 是你自己注册GitHub的邮箱 (一路回车即可)

   > 用户主目录里找到`.ssh`文件夹，里面有`id_rsa`和`id_rsa.pub`两个文件，这两个就是SSH Key的秘钥对，`id_rsa`是私钥，不能泄露，`id_rsa.pub`是公钥，可以公开。

   

3. 把公钥配置到github上

   ![img](https://images2015.cnblogs.com/blog/1192146/201707/1192146-20170714173851775-474031887.png)



4. 验证

   `ssh -T git@github.com`

   ![img](https://images2015.cnblogs.com/blog/1192146/201707/1192146-20170714175422400-33988795.png)



5. 配置user和email

   ```sh
   git config --global user.name "xkj"     # 用户名
   git config --global user.email  "1767822853@qq.com"  # 用户邮箱地址
   ```

6. 连接git和github

   `git remote add origin git@github.com:flora0103/example.git` 

7. 剩下的就是走代码提交流程了



> [连接流程](https://www.cnblogs.com/flora5/p/7152556.html)
>
> [push时问题](https://www.jianshu.com/p/8d26730386f3)

# 2. 代码提交流程:

1. `git add .`（后面有一个点，意思是将你本地所有修改了的文件添加到暂存区）

2. `git commit -m ""`  

   > (引号里面是你的介绍，就是你的这次的提交是什么内容，便于你以后查看，这个是将索引的当前内容与描述更改的用户和日志消息一起存储在新的提交中)

   

3. `git pull origin master` 这是下拉代码，将远程最新的代码先跟你本地的代码合并一下，如果确定远程没有更新，可以不用这个，最好是每次都执行以下，完成之后打开代码查看有没有冲突，并解决，如果有冲突解决完成以后再次执行1跟2的操作

4. `git push origin master` 将代码推至远程就可以了

# 3. 文件操作:

新增文件:  `git add a.txt`

删除文件: `git rm a.txt`