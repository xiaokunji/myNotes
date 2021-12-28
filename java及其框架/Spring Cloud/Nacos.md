[toc]



# 前言

阿里开源的注册中心和配置中心

![nacos_map](https://nacos.io/img/nacosMap.jpg)

[Nacos 官方文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)

[Nacos 源码](https://github.com/alibaba/nacos)

# 1. 概念

**Name Space**

用于进行租户粒度的配置隔离。不同的命名空间下，可以存在相同的 Group 或 Data ID 的配置。Namespace 的常用场景之一是**不同环境的配置**的区分隔离，例如开发测试环境和生产环境的资源（如配置、服务）隔离等。

**Configuration**

配置文件

**Data ID**

Nacos 中的某个配置集的 ID。配置集 ID 是组织划分配置的维度之一。Data ID 通常用于组织划分系统的配置集。一个系统或者应用可以包含多个配置集，每个配置集都可以被一个有意义的名称标识。Data ID 通常采用类 Java 包（如 com.taobao.tc.refund.log.level）的命名规则保证全局唯一性。此命名规则非强制。

**Group**

Nacos 中的一组配置集，是组织配置的维度之一。通过一个有意义的字符串（如 Buy 或 Trade ）对配置集进行分组，从而区分 Data ID 相同的配置集。当您在 Nacos 上创建一个配置时，如果未填写配置分组的名称，则配置分组的名称默认采用 DEFAULT_GROUP 。配置分组的常见场景：不同的应用或组件使用了相同的配置类型，如 database_url 配置和 MQ_topic 配置。



![nacos_data_model](https://cdn.nlark.com/yuque/0/2019/jpeg/338441/1561217857314-95ab332c-acfb-40b2-957a-aae26c2b5d71.jpeg)



[Nacos 概念](https://nacos.io/zh-cn/docs/concepts.html)



# 2. 架构

![nacos_arch.jpg](https://cdn.nlark.com/yuque/0/2019/jpeg/338441/1561217892717-1418fb9b-7faa-4324-87b9-f1740329f564.jpeg)



![nacos-logic.jpg](https://cdn.nlark.com/yuque/0/2019/png/338441/1561217775318-6e408805-18bb-4242-b4e9-83c5b929b469.png)

















