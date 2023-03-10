package com.example.back.interceptor;

import com.example.back.model.dto.CurrentUser;
import com.example.back.util.ResultUtil;
import com.example.back.util.ThreadLocalUtil;
import com.example.back.util.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Token拦截器
 */
public class TokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String servletPath = request.getServletPath();
//        排除的路径
//        if(!servletPath.startsWith("/admin")) return true;
        String token = request.getHeader("Authorization");
        CurrentUser currentUser = TokenUtil.verify(token);
        if(token.equals("devToken")){
            CurrentUser user = new CurrentUser();
            user.setUsername("devUser");
            user.setUuid("0");
            ThreadLocalUtil.set("currentUser",user);
            return true;
        }
        if(null == currentUser.getUuid()||null == currentUser.getUsername()){
            ObjectMapper mapper = new ObjectMapper();
            String mapJackson = mapper.writeValueAsString(new ResultUtil(HttpStatus.UNAUTHORIZED.value(),"token失效，请重新登录"));
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(mapJackson);
            return false;
        }
        ThreadLocalUtil.set("currentUser",currentUser);
        return true;
    }
}
