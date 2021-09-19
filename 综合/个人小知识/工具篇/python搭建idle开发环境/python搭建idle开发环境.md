**1：sublime下载安装**

​     https://www.sublimetext.com/3 

 

**2:安装插件Package Control(注意版本问题 2/3)**

​     crtl+` 调出shell，粘贴下列代码：

```sh
import urllib.request,os,hashlib; h = '6f4c264a24d933ce70df5dedcf1dcaee' + 'ebe013ee18cced0ef93d5f746d80ef60'; pf = 'Package Control.sublime-package'; ipp = sublime.installed_packages_path(); urllib.request.install_opener( urllib.request.build_opener( urllib.request.ProxyHandler()) ); by = urllib.request.urlopen( 'http://packagecontrol.io/' + pf.replace(' ', '%20')).read(); dh = hashlib.sha256(by).hexdigest(); print('Error validating download (got %s instead of %s), please try manual install' % (dh, h)) if dh != h else open(os.path.join( ipp, pf), 'wb' ).write(by)
```

 

 **3:退出重启sublime**

在菜单->preferences->Package Settings和package control选项，就说明安装package control成功了

 

**4:** crtl+shift+p： 输入install ，分别安装以下插件(存在无法下载错误)

AutoPep8、BracketHighligter、Trailing Spaces、

ConvertToUTF8、Anaconda、SFTP、AutoFileName、[FileDiffs](https://github.com/colinta/SublimeFileDiffs)、IMESupport 、SublimeCodeIntel

**5:**修改Preferences-->插件-->Settings-User，添加对应的设置，可以直接使用我的设置或进行修改（需要对其python路径做修改，详情见附件sublimePluginsSettings）

 

**6:**在windows下进行文件共享,使用SFTP插件

> http://blog.csdn.net/qwe1992314/article/details/52232426 