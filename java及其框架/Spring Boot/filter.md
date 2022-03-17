> 类似于spring的过滤器,只是引用方式不同,  
>
> **spring:**  引用过滤器使用xml方式,在web.xml中引用,过滤器越先引用(写在前面)优先级越高  
>
> **spring boot:** 使用一个FilterRegistrationBean类来引用过滤器



过滤器书写与spring一致:  
```java
public class MyFilter implements Filter {
//写了一个MyFilter 过滤器
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest srequest, ServletResponse sresponse, FilterChain filterChain)
        throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) srequest;
        //可以做登录验证等等…..

        logger.info("this is MyFilter,url :"+request.getRequestURI());
        filterChain.doFilter(srequest, sresponse);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }
  }

```

使用 过滤器:
```java
@Bean
public FilterRegistrationBean<MyFilter> testFilterRegistration() {

    FilterRegistrationBean<MyFilter> registration = new FilterRegistrationBean<MyFilter>();
    registration.setFilter(new MyFilter());
    registration.addUrlPatterns("/*"); //设置拦截url
    registration.addInitParameter("paramName", "paramValue");
    registration.setName("MyFilter");
    registration.setOrder(1);//设置优先级
    return registration;
}
```