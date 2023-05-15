package com.example.back.controller;

import com.example.back.model.dto.CurrentUser;
import com.example.back.service.FileService;
import com.example.back.util.ResultUtil;
import com.example.back.util.ThreadLocalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
public class PDFController {

    private static final Logger logger = LoggerFactory.getLogger(PDFController.class);

    @Value("${file.basicLocation}")
    private String fileLocation;

    @Resource
    private FileService fileService;

    @GetMapping("/onlinePDF")
    public ResultUtil download(@RequestParam("token") String token, @RequestParam("uuid") String uuid, HttpServletResponse response) {
        response.reset();
        ObjectMapper objectMapper = new ObjectMapper();
        CurrentUser currentUser = objectMapper.convertValue(ThreadLocalUtil.get("currentUser"),CurrentUser.class);
        String path = fileLocation+'/'+currentUser.getUuid()+fileService.getFileRelativePath(uuid);
        try (InputStream inputStream = new FileInputStream(path)) {
            response.reset();
            response.setContentType("application/pdf");
            ServletOutputStream servletOutputStream = response.getOutputStream();
            byte[] bytes = new byte[1024];
            int len;
            while ((len = inputStream.read(bytes)) > 0) {
                servletOutputStream.write(bytes, 0, len);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
            return ResultUtil.fail(e.getMessage());
        }
        return ResultUtil.ok();
    }
}
