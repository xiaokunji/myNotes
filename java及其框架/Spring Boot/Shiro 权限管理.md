工作原理是通过过滤器拦截,做一些操作,来达到身份认证和权限管理的作用

有拦截功能,跳转功能  

shiro自带拦截器,也有相应实现,具体实现不需要我们操作,只需做在什么情况下跳转什么页面(或者操作),比如:当密码错误时,要做什么(自己写操作) ; 没有登录时,做什么等等

> 详情: https://blog.csdn.net/ityouknow/article/details/73836159


```java
 @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
        logger.info("-------------getShiroFilterFactoryBean()+++++++++++");
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 必须设置 SecurityManager  
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        
        // 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
        shiroFilterFactoryBean.setLoginUrl("/login");
        
//        // 登录成功后要跳转的连接
//        shiroFilterFactoryBean.setSuccessUrl("/user");
//        shiroFilterFactoryBean.setUnauthorizedUrl("/403");
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
        // authc：该过滤器下的页面必须验证后才能访问，它是Shiro内置的一个拦截器org.apache.shiro.web.filter.authc.FormAuthenticationFilter

    // anon：它对应的过滤器里面是空的,什么都没做
    filterChainDefinitionMap.put("/getUser", "anon");// 这里为了测试，只限制/user，实际开发中请修改为具体拦截的请求规则


        logger.info("##################从数据库读取权限规则，加载到shiroFilter中##################");
        filterChainDefinitionMap.put("/user/edit/**", "authc,perms[user:edit]");// 这里为了测试，固定写死的值，也可以从数据库或其他配置中读取

        filterChainDefinitionMap.put("/hello", "authc");
    
        filterChainDefinitionMap.put("/**", "anon");//anon 可以理解为不拦截

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }
```