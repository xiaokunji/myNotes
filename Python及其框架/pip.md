pip 使用:

1. 导出各种包: 

`pip freeze > requirments.txt`

2. 从文件中安装各种包 

`pip install -r requirments.txt`

**virtualenv`**

(1).安装: 

`pip install virtualenv `

(2).创建一个隔离环境 

`virtualenv myproject `

(3)使用这个隔离环境 

`source venv/bin/activate `

(4)退出 

`deactivate`

**virtualenvwrapper**

**(1).安装:** 

linux: `pip install virtualenvwrapper `

win: `pip install virtualenvwrapper-win`

**(2).配置:** 

在~/.bashrc写入以下内容(在其他配置文件也是可以的) 

`export WORKON_HOME=~/Envs`

`source /usr/local/bin/virtualenvwrapper.sh`

> 第一行：virtualenvwrapper存放虚拟环境目录 
>
> 第二行：virtrualenvwrapper会安装到python的bin目录下，所以该路径是python安装目录下bin/virtualenvwrapper.sh

读入配置文件，立即生效 

`source ~/.bashrc `

 

**(3).使用** 

①.创建虚拟环境: 

`mkvirtualenv venv `

②.查看当前虚拟环境目录 

`workon `

③.切换虚拟环境: 

`workon venv `

④.退出: 

`deactivate `

⑤.删除虚拟环境 

`rmvirtualenv venv`