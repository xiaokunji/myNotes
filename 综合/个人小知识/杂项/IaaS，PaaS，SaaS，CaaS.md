很明显，这五者之间主要的区别在于第一个单词，而aaS都是as-a-service（即服务）的意思，这五个模式都是近年来兴起的，且这五者都是云计算的落地产品.

**IaaS** 

=> Infrastructure-as-a-Service 基础设施即是服务, 简单的不科学说法就是，人家买了一堆电脑租给你用。这是第一层。解决了之前企业要买服务器，装修机房，还要随时防止你没交物业给你停电的问题。服务器都在供应商那， 你不用管啥时候维护。牛逼的供应商很多，阿里云，网易，Azure，AWS-EC2，AWS中国（中国的AWS跟米国的不是一回事，很多服务没有哦） 

**PaaS** 

=>这个词不好理解，Platform-as-a-Service 平台即是服务, 简单的不科学说法就是，人家帮你装好操作系统了和基础软件啦。供应商不仅帮你装好了操作系统还有很多基础软件，例如JAVA/Python的环境，让你上传代码就可以启动服务了。 牛逼的供应商很多，GAE/BAE/SAE，Heroku，JCS（oracle JAVA-Cloud-Service）。PaaS 系统提供很多OOTB的功能，例如编译环境，CI/CD，SCM，LB，DNS，Log， DB， Cache。其实我发现很多时候SAAS和PAAS大家分也分不清楚，随缘啦。

**SaaS** 

=> Software-as-a-Service 软件即是服务, 简单的不科学说法就是，人家帮你装好word啦.这里是Application层，相当发达，咱们小公司，还是在这里机会更多。例如你想要短信服务，阿里云去买。你想要直播服务，优酷去买。你想要CDN服务，各个大厂商都在卖（七牛，阿里云，AWS-S3）。

**CaaS** 

=>这个次比较新，Container-as-a-Service 容器即是服务, 简单的不科学且不形象的说法（我也不知道怎么比喻）就是，人家帮你在你mac上装了10个IE（注意，CaaS 有很多中说法，有人说是Communications，有人说是Commerce还有人说是Cloud, 这里只讨论容器服务)。随着容器的迅速发展，开始有大量厂商提供容器服务，让企业成本（现金成本和管理成本）更低，他们一般都提供容器编排服务。流行度还没那么高，目测很多企业还是在ISSA的基础上使用容器编排软件来搞。比较流行的是Docker,Rancher,Kubernetes和Mesos。

**IaaS VS PaaS**

简单点说IaaS提供虚拟机，而PaaS提供了IDE和测试环境。用了IaaS你就可以装很多乱七八糟的软件了，而PaaS可能对你的行为进行限制，例如你在BAE上部署一个spring项目，你想读取磁盘的一个文件。PaaS会说NO，它已经帮你弄好了编译/运行时环境，限制也大，但是简单。如果你用IaaS，这是你的虚拟机，你可做任何你想要的，rm -rf / 也可以。应用级别的软件就要自己装自己搞了。

**PaaS VS CaaS**

PaaS针对特定的应用，提供了整套的解决方案，但是对于个性化需求限制太大，随着DevOps/Micro-Service的迅速发展，越来越多的公司期望通过环境的统一解决Dev/Test/Production的一致性问题，让部署更加自动化与统一化。容器来了，特别随着Docker的牛逼，这个神器让复杂的Micro-service问题降低了很大的门槛。当然CaaS也更便宜。

听起来，Xxxx-as-a-Service 似乎感觉是继承关系。确实是，但不是绝对的。

> 链接：https://www.jianshu.com/p/b27a9f4686f6
>
> 更通俗的解释: https://www.zhihu.com/question/21641778/answer/62523535?from=groupmessage