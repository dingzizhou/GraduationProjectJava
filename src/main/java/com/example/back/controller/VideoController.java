package com.example.back.controller;

import cn.hutool.core.util.StrUtil;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.io.File;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class VideoController {

//    private final ResourceHttpRequestHandler resourceHttpRequestHandler;

    @Value("${file.basicLocation}")
    private String fileLocation;

    @Resource
    private FileService fileService;
//    @GetMapping("/video")
//    public void getVideoByUrl(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //sourcePath 是获取resources文件夹的绝对地址
        //realPath 即是视频所在的磁盘地址
//        String sourcePath = ClassUtils.getDefaultClassLoader().getResource("").getPath().substring(1);
//        String realPath = "D:\\迅雷下载\\FC2 PPV 1293958 (UNCENSORED 1280x720p).mp4";
//        String realPath = "A:\\file\\05.mp4";
//        System.out.println(realPath);
//        Path filePath = Paths.get(realPath);
//        if (Files.exists(filePath)) {
//            String mimeType = Files.probeContentType(filePath);
//            if (!StringUtils.isEmpty(mimeType)) {
//                response.setContentType(mimeType);
//            }
//            request.setAttribute("NON-STATIC-FILE", filePath);
//            resourceHttpRequestHandler.handleRequest(request,response);
//        } else {
//            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
//        }
//    }

    @GetMapping("/video")
    public void test(@RequestParam("token") String token, @RequestParam("uuid") String uuid, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.reset();
        ObjectMapper objectMapper = new ObjectMapper();
        CurrentUser currentUser = objectMapper.convertValue(ThreadLocalUtil.get("currentUser"),CurrentUser.class);
        String filePath = fileLocation+'/'+currentUser.getUuid()+fileService.getFileRelativePath(uuid);
        File file = new File(filePath);
        long fileLength = file.length();
        // 随机读文件
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");

        //获取从那个字节开始读取文件
        String rangeString = request.getHeader("Range");
        long range=0;
        if (StrUtil.isNotBlank(rangeString)) {
            range = Long.valueOf(rangeString.substring(rangeString.indexOf("=") + 1, rangeString.indexOf("-")));
        }
        //获取响应的输出流
        OutputStream outputStream = response.getOutputStream();
        //设置内容类型
        response.setHeader("Content-Type", "video/mp4");
        //返回码需要为206，代表只处理了部分请求，响应了部分数据
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

        // 移动访问指针到指定位置
        randomAccessFile.seek(range);
        // 每次请求只返回1MB的视频流
        byte[] bytes = new byte[1024 * 1024];
        int len = randomAccessFile.read(bytes);
        //设置此次相应返回的数据长度
//                response.setContentLength(len);
        //设置此次相应返回的数据范围
        response.setHeader("Content-Range", "bytes "+range+"-"+(fileLength-1)+"/"+fileLength);
        // 将这1MB的视频流响应给客户端
        outputStream.write(bytes, 0, len);
        outputStream.close();
        randomAccessFile.close();

        System.out.println("返回数据区间:【"+range+"-"+(range+len)+"】");

    }


}
