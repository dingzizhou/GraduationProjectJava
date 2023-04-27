package com.example.back.controller;

import com.example.back.model.dto.CurrentUser;
import com.example.back.util.ResultUtil;
import com.example.back.util.ThreadLocalUtil;
import com.example.back.util.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.util.GraphicsRenderingHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

@RestController
@Slf4j
public class TumbnailController {

    @Value("${file.basicLocation}")
    private String fileLocation;

    @Resource
    private HttpServletResponse response;

    @GetMapping(value = "/thumbnail/img")
    public void getImageThumbnail(@RequestParam("token") String token, @RequestParam("url") String url) throws IOException {
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
        String type = url.substring(url.indexOf(".")+1);
        OutputStream os = null;
        try {
//            读取图片
            BufferedImage image = ImageIO.read(new FileInputStream(new File(fileLocation+url)));
            response.setContentType("image/"+type);
            os = response.getOutputStream();

            if (image != null) {
                ImageIO.write(image, type, os);
            }
        } catch (IOException e) {
            if(!(e instanceof FileNotFoundException)){
                log.error("获取图片异常{}",e.getMessage());
            }
        } finally {
            if (os != null) {
                os.flush();
                os.close();
            }
        }
    }

    @GetMapping(value = "/thumbnail/pdf")
    public void getPDFImageThumbnail(@RequestParam("token") String token, @RequestParam("url") String url)throws IOException{
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
        OutputStream os = null;
        try {
            Document document = new Document();
            document.setFile(fileLocation+url);
            float rotation = 0f;
            // 缩略图显示倍数，1表示不缩放，0.5表示缩小到50%
            float zoom = 0.8f;
            BufferedImage p_w_picpath = (BufferedImage) document.getPageImage(0, GraphicsRenderingHints.SCREEN, Page.BOUNDARY_CROPBOX,rotation,zoom);
            response.setContentType("image/jpeg");
            os = response.getOutputStream();
            if(p_w_picpath!=null){
                ImageIO.write(p_w_picpath,"jpeg",os);
            }
        } catch (Exception e) {
            if(!(e instanceof FileNotFoundException)){
                log.error("获取PDF的缩略图异常{}",e.getMessage());
            }
        } finally {
            if (os != null) {
                os.flush();
                os.close();
            }
        }
    }

}
