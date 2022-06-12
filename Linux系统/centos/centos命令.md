centos

    6. 更换yum源
    
    #下载wget
    yum install wget -y
    
    #备份原来的yum
    mv /etc/yum.repos.d/CentOS-Base.repo /etc/yum.repos.d/CentOS-Base.repo.backup
    
    #下载yum
    wget http://mirrors.163.com/.help/CentOS7-Base-163.repo
    
    #阿里的yum
    wget -O /etc/yum.repos.d/CentOS-Base.repo http://mirrors.aliyun.com/repo/Centos-7.repo
    #更新缓存
     yum makecache 
    
    #查询源
    yum -y update

> 来自 <https://www.cnblogs.com/xjh713/p/7458437.html> 
> 
> #安装自动提示包

`yum install -y bash-completion`

​    
​    
