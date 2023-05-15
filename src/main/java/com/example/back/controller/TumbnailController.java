package com.example.back.controller;

import com.example.back.model.dto.CurrentUser;
import com.example.back.service.FileService;
import com.example.back.util.ResultUtil;
import com.example.back.util.ThreadLocalUtil;
import com.example.back.util.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@Slf4j
public class TumbnailController {

    @Value("${file.basicLocation}")
    private String fileLocation;

    @Resource
    private HttpServletResponse response;
    @Resource
    private FileService fileService;

    @GetMapping(value = "/thumbnail/img")
    public void getImageThumbnail(@RequestParam("token") String token, @RequestParam("uuid") String uuid) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        CurrentUser currentUser = objectMapper.convertValue(ThreadLocalUtil.get("currentUser"),CurrentUser.class);
        String filePath = fileLocation+'/'+currentUser.getUuid()+fileService.getFileRelativePath(uuid);
        String type = filePath.substring(filePath.indexOf(".")+1);
        OutputStream os = null;
        try {
            File file = new File(filePath);
            if(!file.exists()){
                throw new Exception("图片丢失了！");
            }
//            读取图片
            BufferedImage image = ImageIO.read(new FileInputStream(file));
            response.setContentType("image/"+type);
            os = response.getOutputStream();

            if (image != null) {
                ImageIO.write(image, type, os);
            }
        } catch (Exception e) {
            if(!(e instanceof FileNotFoundException)){
                log.error("获取图片异常{}",e.getMessage());
            }
            response.setStatus(404);
        } finally {
            if (os != null) {
                os.flush();
                os.close();
            }
        }
    }

    @GetMapping(value = "/thumbnail/pdf")
    public void getPDFImageThumbnail(@RequestParam("token") String token, @RequestParam("uuid") String uuid)throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        CurrentUser currentUser = objectMapper.convertValue(ThreadLocalUtil.get("currentUser"),CurrentUser.class);
        String filePath = fileLocation+'/'+currentUser.getUuid()+fileService.getFileRelativePath(uuid);
//        System.out.println(filePath);
        OutputStream os = null;
        try {
            Document document = new Document();
            document.setFile(filePath);
            float rotation = 0f;
            // 缩略图显示倍数，1表示不缩放，0.5表示缩小到50%
            float zoom = 0.8f;
            BufferedImage p_w_picpath = (BufferedImage) document.getPageImage(0, GraphicsRenderingHints.SCREEN, Page.BOUNDARY_CROPBOX,rotation,zoom);
            response.setContentType("image/jpeg");
            os = response.getOutputStream();
            if(p_w_picpath!=null){
                ImageIO.write(p_w_picpath,"jpeg",os);
            }
            else{
                throw new Exception("404");
            }
        } catch (Exception e) {
            if(!(e instanceof FileNotFoundException)){
                log.error("获取PDF的缩略图异常{}",e.getMessage());
                response.setStatus(500);
            }
            else{
                response.setStatus(404);
            }
        } finally {
            if (os != null) {
                os.flush();
                os.close();
            }
        }
    }

}
