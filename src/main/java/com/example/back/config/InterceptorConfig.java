package com.example.back.config;

import com.example.back.interceptor.TokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TokenInterceptor()).addPathPatterns(include()).excludePathPatterns(exclude());
    }

    public String[] include(){
        System.out.println("进入addPathPatterns");
        return new String[]{
                "/**"
        };
    }

    public String[] exclude(){
        System.out.println("进入excludePathPatterns");
        return new String[]{
                "/login"
        };
    }
}
