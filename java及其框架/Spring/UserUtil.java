package com.xkj.java.javademo.util;

import cn.hutool.json.JSONUtil;
import com.xkj.java.javademo.entity.Person;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * @author xkj
 * @createTime 2021/12/12
 */
@Component
public class UserUtil {

    @Autowired
    HttpServletRequest request;

    @Bean
    public RequestInterceptor requestInterceptor(){
        // //feign远程调用丢失请求头问题 (可以放到别的地方)
        // https://www.cnblogs.com/wwjj4811/p/13977073.html
        return template -> UserUtil.getHttpHeader("user").ifPresent(user -> template.header("Cookie", user));
    }

    /**
     * 获取当前请求session
     * <p> https://www.cnblogs.com/wwjj4811/p/13977073.html</p>
     * <pre> 1. feign调用时会丢失header数据 => 在feign拦截器中设置header
     * <pre> 2. 异步调用时会丢失header数据 => 在异步代码里手动设置header
     *
     * @return request
     */
    public static Optional<HttpServletRequest> getHttpServletRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .map(s -> (ServletRequestAttributes) s)
                .map(ServletRequestAttributes::getRequest);
    }

    /**
     * 获取当前请求指定的header值
     *
     * @param name 指定key
     * @return 对应的值
     */
    public static Optional<String> getHttpHeader(String name) {
        return getHttpServletRequest().map(request -> request.getHeader(name));
    }

    public  static Person getLoginPerson(){
        String user = getHttpHeader("user").orElseThrow(IllegalAccessError::new);
        return JSONUtil.toBean(RedisUtil.get(user), Person.class);
    }




}
