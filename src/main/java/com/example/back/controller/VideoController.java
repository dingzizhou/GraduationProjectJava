package com.example.back.controller;

import com.example.back.model.dto.CurrentUser;
import com.example.back.util.ResultUtil;
import com.example.back.util.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@AllArgsConstructor
public class VideoController {

    private final ResourceHttpRequestHandler resourceHttpRequestHandler;

    @GetMapping("/video")
    public void getVideoByUrl(HttpServletRequest request, HttpServletResponse response,
                             @RequestParam("token") String token, @RequestParam("url") String url) throws Exception {
//        System.out.println(url);
        if(token==null) {
            ObjectMapper mapper = new ObjectMapper();
            String mapJackson = mapper.writeValueAsString(new ResultUtil(HttpStatus.UNAUTHORIZED.value(),"token失效，请重新登录"));
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(mapJackson);
            return ;
        }
        CurrentUser currentUser = TokenUtil.verify(token);
        if(null == currentUser.getUuid()||null == currentUser.getUsername()){
            ObjectMapper mapper = new ObjectMapper();
            String mapJackson = mapper.writeValueAsString(new ResultUtil(HttpStatus.UNAUTHORIZED.value(),"token失效，请重新登录"));
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(mapJackson);
            return ;
        }
        //sourcePath 是获取resources文件夹的绝对地址
        //realPath 即是视频所在的磁盘地址
//        String sourcePath = ClassUtils.getDefaultClassLoader().getResource("").getPath().substring(1);
//        String realPath = "D:\\迅雷下载\\FC2 PPV 1293958 (UNCENSORED 1280x720p).mp4";
        String realPath = url;

        Path filePath = Paths.get(realPath);
        if (Files.exists(filePath)) {
            String mimeType = Files.probeContentType(filePath);
            if (!StringUtils.isEmpty(mimeType)) {
                response.setContentType(mimeType);
            }
            request.setAttribute("NON-STATIC-FILE", filePath);
            resourceHttpRequestHandler.handleRequest(request,response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        }
    }

//    @GetMapping("/video")
//    public void getVideoById(HttpServletRequest request, HttpServletResponse response,
//                             @RequestParam("token") String token, @RequestParam("id") String url) throws Exception{
//        if(token==null) {
//            ObjectMapper mapper = new ObjectMapper();
//            String mapJackson = mapper.writeValueAsString(new ResultUtil(HttpStatus.UNAUTHORIZED.value(),"token失效，请重新登录"));
//            response.setContentType("application/json;charset=UTF-8");
//            response.getWriter().write(mapJackson);
//            return ;
//        }
//        CurrentUser currentUser = TokenUtil.verify(token);
//        if(null == currentUser.getUuid()||null == currentUser.getUsername()){
//            ObjectMapper mapper = new ObjectMapper();
//            String mapJackson = mapper.writeValueAsString(new ResultUtil(HttpStatus.UNAUTHORIZED.value(),"token失效，请重新登录"));
//            response.setContentType("application/json;charset=UTF-8");
//            response.getWriter().write(mapJackson);
//            return ;
//        }
//    }


}
