[toc]

# 一、什么是Anaconda？

## 1. 简介

Anaconda（[官方网站](https://link.jianshu.com?t=https%3A%2F%2Fwww.anaconda.com%2Fdownload%2F%23macos)）就是可以便捷获取包且对包能够进行管理，同时对环境可以统一管理的发行版本。Anaconda包含了conda、Python在内的超过180个科学包及其依赖项。

> 简单的说就是提供了一个python的环境,还有很多工具包,不需要自己单独安装python了,这个工具自带python,还可以通过虚拟环境,指定不同版本的python
>
> **通过虚拟出来的环境,可以当成一个python用,要用时指定对应目录即可**

> 链接：https://www.jianshu.com/p/62f155eb6ac5
> 

## 2. Anaconda、conda、pip、virtualenv的区别

### ① Anaconda

- Anaconda是一个包含180+的科学包及其依赖项的发行版本。其包含的科学包包括：conda, numpy, scipy, ipython notebook等。

### ② conda

- conda是包及其依赖项和环境的管理工具。

- 适用语言：Python, R, Ruby, Lua, Scala, Java, JavaScript, C/C++, FORTRAN。

- 适用平台：Windows, macOS, Linux

- 用途：

  1. 快速安装、运行和升级包及其依赖项。
  2. 在计算机中便捷地创建、保存、加载和切换环境。

  > 如果你需要的包要求不同版本的Python，你无需切换到不同的环境，因为conda同样是一个环境管理器。仅需要几条命令，你可以创建一个完全独立的环境来运行不同的Python版本，同时继续在你常规的环境中使用你常用的Python版本。——[conda官方网站](https://link.jianshu.com?t=https%3A%2F%2Fconda.io%2Fdocs%2F)

- conda为Python项目而创造，但可适用于上述的多种语言。

- conda包和环境管理器包含于Anaconda的所有版本当中。

### ③ pip

- pip是用于安装和管理软件包的包管理器。
- pip编写语言：Python。
- Python中默认安装的版本：
  - Python 2.7.9及后续版本：默认安装，命令为`pip`
  - Python 3.4及后续版本：默认安装，命令为`pip3`
- pip名称的由来：pip采用的是**递归缩写**进行命名的。其名字被普遍认为来源于2处：
  - “Pip installs Packages”（“pip安装包”）
  - “Pip installs Python”（“pip安装Python”）

### ④ virtualenv

- virtualenv：用于创建一个**独立的**Python环境的工具。
- 解决问题：
  1. 当一个程序需要使用Python 2.7版本，而另一个程序需要使用Python 3.6版本，如何同时使用这两个程序？
  2. 如果将所有程序都安装在系统下的默认路径，如：`/usr/lib/python2.7/site-packages`，当不小心升级了本不该升级的程序时，将会对其他的程序造成影响。
  3. 如果想要安装程序并在程序运行时对其库或库的版本进行修改，都会导致程序的中断。
  4. 在共享主机时，无法在全局`site-packages`目录中安装包。
- virtualenv将会为它自己的安装目录创建一个环境，这并**不与**其他virtualenv环境共享库；同时也可以**选择性**地不连接已安装的全局库。

### ⑤ pip 与 conda 比较

#### → 依赖项检查

- pip：
  - **不一定**会展示所需其他依赖包。
  - 安装包时**或许**会直接忽略依赖项而安装，仅在结果中提示错误。
- conda：
  - 列出所需其他依赖包。
  - 安装包时自动安装其依赖项。
  - 可以便捷地在包的不同版本中自由切换。

#### → 环境管理

- pip：维护多个环境难度较大。
- conda：比较方便地在不同环境之间进行切换，环境管理较为简单。

#### → 对系统自带Python的影响

- pip：在系统自带Python中包的**更新/回退版本/卸载将影响其他程序。
- conda：不会影响系统自带Python。

#### → 适用语言

- pip：仅适用于Python。
- conda：适用于Python, R, Ruby, Lua, Scala, Java, JavaScript, C/C++, FORTRAN。

### ⑥ conda与pip、virtualenv的关系

- conda**结合**了pip和virtualenv的功能。



> 链接：https://www.jianshu.com/p/62f155eb6ac5
> 



# 二、Anaconda的安装步骤

## 2. Windows系统安装Anaconda

1. 前往[官方下载页面](https://link.jianshu.com?t=https%3A%2F%2Fdocs.anaconda.com%2Fanaconda%2Finstall%2Fwindows)下载。有两个版本可供选择：Python 3.6 和 Python 2.7，选择版之后根据自己操作系统的情况点击“64-Bit Graphical Installer”或“32-Bit Graphical Installer”进行下载。

> 剩下的一路下一步就行了,最后一步取消两个勾(弹广告的)

> 链接：https://www.jianshu.com/p/62f155eb6ac5
> 



## 3. Linux系统安装Anaconda

> <font style='color:red'>没测过</font>

1. 前往[官方下载页面](https://link.jianshu.com?t=https%3A%2F%2Fwww.anaconda.com%2Fdownload%2F%23linux)下载。有两个版本可供选择：Python 3.6 和 Python 2.7。
2. 启动终端，在终端中输入命令`md5sum /path/filename`或`sha256sum /path/filename`

- 注意：将该步骤命令中的`/path/filename`替换为文件的实际下载路径和文件名。其中，path是路径，filename为文件名。
- 强烈建议：
  1. 路径和文件名中不要出现空格或其他特殊字符。
  2. 路径和文件名最好以英文命名，不要以中文或其他特殊字符命名。

3. 根据Python版本的不同有选择性地在终端输入命令：

- Python 3.6：`bash ~/Downloads/Anaconda3-5.0.1-Linux-x86_64.sh`
- Python 2.7：`bash ~/Downloads/Anaconda2-5.0.1-Linux-x86_64.sh`

- 注意：
  1. 首词bash也需要输入，无论是否用的Bash shell。
  2. 如果你的下载路径是自定义的，那么把该步骤路径中的`~/Downloads`替换成你自己的下载路径。
  3. 除非被要求使用root权限，否则均选择“Install Anaconda as a user”。

4. 安装过程中，看到提示“In order to continue the installation process, please review the license agreement.”（“请浏览许可证协议以便继续安装。”），点击“Enter”查看“许可证协议”。

5. 在“许可证协议”界面将屏幕滚动至底，输入“yes”表示同意许可证协议内容。然后进行下一步。

6. 安装过程中，提示“Press Enter to accept the default install location, CTRL-C to cancel the installation or specify an alternate installation directory.”（“按回车键确认安装路径，按'CTRL-C'取消安装或者指定安装目录。”）如果接受默认安装路径，则会显示“PREFIX=/home/<user>/anaconda<2 or 3>”并且继续安装。安装过程大约需要几分钟的时间。

- 建议：直接接受默认安装路径。

7. 安装器若提示“Do you wish the installer to prepend the Anaconda<2 or 3> install location to PATH in your /home/<user>/.bashrc ?”（“你希望安装器添加Anaconda安装路径在`/home//.bashrc`文件中吗？”），建议输入“yes”。

- 注意：
  1. 路径`/home//.bash_rc`中“<user>”即进入到家目录后你的目录名。
  2. 如果输入“no”，则需要手动添加路径，否则conda将无法正常运行。

8. 当看到“Thank you for installing Anaconda<2 or 3>!”则说明已经成功完成安装。

9. 关闭终端，然后再打开终端以使安装后的Anaconda启动。或者直接在终端中输入`source ~/.bashrc`也可完成启动。

10. 验证安装结果。可选用以下任意一种方法：

1. 在终端中输入命令`condal list`，如果Anaconda被成功安装，则会显示已经安装的包名和版本号。
2. 在终端中输入`python`。这条命令将会启动Python交互界面，如果Anaconda被成功安装并且可以运行，则将会在Python版本号的右边显示“Anaconda custom (64-bit)”。退出Python交互界面则输入`exit()`或`quit()`即可。
3. 在终端中输入`anaconda-navigator`。如果Anaconda被成功安装，则Anaconda Navigator将会被启动。



> 链接：https://www.jianshu.com/p/62f155eb6ac5
> 



# 四、管理conda



## 0. 写在前面

接下来均是以**命令行模式**进行介绍，Windows用户请打开“**Anaconda Prompt**”；macOS和Linux用户请打开“Terminal”（“终端”）进行操作。

`conda --version`  测试是否正确安装

`conda update conda` 可能需要更新一下conda



# 五、管理环境

## 0. 写在前面

进入命令行后,默认在base环境下,(装好了很多包),

> 在路径最前面,环境名用括号括起来

## 1. 创建新环境

```xml
conda create --name <env_name> <package_names>
```

- 注意：

  - `<env_name>`即创建的环境名。建议以英文命名，且不加空格，名称两边不加尖括号“<>”。

  - `<package_names>`即安装在环境中的包名。名称两边不加尖括号“<>”。

    1. 如果要安装指定的版本号，则只需要在包名后面以`=`和版本号的形式执行。如：`conda create --name python2 python=2.7`，即创建一个名为“python2”的环境，环境中安装版本为2.7的python。默认与Anaconda版本一样

    2. 如果要在新创建的环境中创建多个包，则直接在``后以**空格**隔开，添加多个包名即可。如：`conda create -n python3 python=3.5 numpy pandas`，即创建一个名为“python3”的环境，环境中安装版本为3.5的python，同时也安装了numpy和pandas。默认不装包

       

  - `--name`同样可以替换为`-n`。

- 提示：默认情况下，新创建的环境将会被保存在`/Users//anaconda3/env`目录下，其中，`<user_name>`为当前用户的用户名。



> 链接：https://www.jianshu.com/p/62f155eb6ac5
> 

## 2. 切换环境

### ① Linux 或 macOS

```bash
source activate <env_name>
```

### ② Windows

```xml
activate <env_name>
```



## 3. 退出环境至root

### ① Linux 或 macOS

```bash
source deactivate
```

### ② Windows

```undefined
deactivate
```



## 4. 显示已创建环境

```undefined
# 三选一
conda info --envs
conda info -e
conda env list
```

> 结果中星号 “*” 所在行即为当前所在环境



## 5. 复制环境

```xml
conda create --name <new_env_name> --clone <copied_env_name>
```

- 注意：
  1. `<copied_env_name>`即为被复制/克隆环境名。环境名两边不加尖括号“<>”。
  2. `<new_env_name>`即为复制之后新环境的名称。环境名两边不加尖括号“<>”。
  3. 如：`conda create --name py2 --clone python2`，即为克隆名为“python2”的环境，克隆后的新环境名为“py2”。此时，环境中将同时存在“python2”和“py2”环境，且两个环境的配置相同。



## 6. 删除环境

```csharp
conda remove --name <env_name> --all
```

- 注意：`<env_name>`为被删除环境的名称。环境名两边不加尖括号“<>”。



## 3. 安装包

### ① 在指定环境中安装包

```xml
conda install --name <env_name> <package_name>
```

- > 例如：`conda install --name python2 pandas`即在名为“python2”的环境中安装pandas包。

### ② 在当前环境中安装包

```xml
conda install <package_name>
```

> 例如：`conda install pandas`即在当前环境中安装pandas包。

### ③ 使用pip安装包

#### → 使用场景

当使用`conda install`无法进行安装时，可以使用pip进行安装。例如：see包。

#### → 命令

```xml
pip install <package_name>
```

> 如：`pip install see`即安装see包。

#### → 注意

1. pip只是包管理器，无法对环境进行管理。因此如果想在指定环境中使用pip进行安装包，则需要先切换到指定环境中，再使用pip命令安装包。
2. pip无法更新python，因为pip并不将python视为包。
3. pip可以安装一些conda无法安装的包；conda也可以安装一些pip无法安装的包。因此当使用一种命令无法安装包时，可以尝试用另一种命令。

### ④ 从Anaconda.org安装包

在浏览器中输入：[http://anaconda.org](https://link.jianshu.com/?t=http%3A%2F%2Fanaconda.org)

通过界面的方式下载包



> 链接：https://www.jianshu.com/p/62f155eb6ac5
>
> [更换仓库源](https://www.zhihu.com/question/58033789/answer/254673663)
>
> [在工具中使用python](https://www.cnblogs.com/IT-LearnHall/p/9486029.html)