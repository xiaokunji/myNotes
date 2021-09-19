# 关于Https:
https=ssl+http

证书:   

SSL证书需要向国际公认的证书证书认证机构（简称CA，Certificate Authority）申请。
CA机构颁发的证书有3种类型：

- 域名型SSL证书（DV SSL）：信任等级普通，只需验证网站的真实性便可颁发证书保护网站； 
- 企业型SSL证书（OV SSL）：信任等级强，须要验证企业的身份，审核严格，安全性更高；
- 增强型SSL证书（EV SSL）：信任等级最高，一般用于银行证券等金融机构，审核严格，安全性最高，同时可以激活绿色网址栏。

来自 <https://www.zhihu.com/question/19578422> 



也有免费的证书,比如腾讯云的DV SSL
也能自己生产证书,只是不会被其他人承认(也就是说别人访问你的网站会提示证书危险并且浏览器默认不会加载非HTTPS域名下的javascript ),jdk能生成证书,既然别人访问你都会爆红,自己做个证书也没啥用,不如申请免费的玩玩

Tomcat配置Https:   

打开 Tomcat 配置文件 conf\server.xml。取消注释，并添加三个属性 keystoreFile，keystoreType，keystorePass。
来自 <https://blog.csdn.net/gane_cheng/article/details/53001846> 
http://lixor.iteye.com/blog/1532655


```
<Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
    maxThreads="150" scheme="https" secure="true"
 clientAuth="false" sslProtocol="TLS" keystoreFile="/你的磁盘目录/订单号.pfx"
keystoreType="PKCS12"
keystorePass="订单号" />
```


注:还未实践,暂时只为记录!
