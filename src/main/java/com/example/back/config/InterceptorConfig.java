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
        return new String[]{
                "/**"
        };
    }

    public String[] exclude(){
        return new String[]{
                "/login"
//                "/video/**",
//                "/video",
//                "/video2",
//                "/thumbnail/**",
//                "/uploadFile",
//                "/download"
        };
    }
}
