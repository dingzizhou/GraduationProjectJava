package com.example.back.controller;

import cn.hutool.core.util.IdUtil;
import com.example.back.model.dto.CurrentUser;
import com.example.back.model.dto.front2backDTO.ChunkDTO;
import com.example.back.model.dto.front2backDTO.CreateFolderDTO;
import com.example.back.model.dto.front2backDTO.FileInfoDTO;
import com.example.back.model.pojo.ChunkPojo;
import com.example.back.model.pojo.FilePojo;
import com.example.back.model.vo.FileInfoVO;
import com.example.back.service.ChunkService;
import com.example.back.service.FileService;
import com.example.back.service.FileShareService;
import com.example.back.util.ResultUtil;
import com.example.back.util.ThreadLocalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

@RestController
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Value("${file.basicLocation}")
    private String fileLocation;

    @Resource
    private FileService fileService;
    @Resource
    private ChunkService chunkService;
    @Resource
    private FileShareService fileShareService;
    /**
     * 根据父文件夹查找子文件
     * @param path
     * @return
     */
    @GetMapping("/getChildrenFiles")
    public ResultUtil getChildrenFiles(@RequestParam("path") String path){
        return fileService.getChildrenFiles(path);
    }

    /**
     * 根据当前目录获取上一级目录
     * @param path
     * @return
     */
    @GetMapping("/goBack")
    public ResultUtil goBack(@RequestParam("path") String path){
        return fileService.goBack(path);
    }

    /**
     * 创建文件夹目录
     * @param createFolderDTO
     * @return
     */
    @PostMapping("/createFolder")
    public ResultUtil createFolder(@Valid CreateFolderDTO createFolderDTO){
        return fileService.createFolder(createFolderDTO);
    }

    /**
     * 文件下载
     * @param response
     * @throws IOException
     */
    @CrossOrigin
    @GetMapping("/download")
    public void download(@RequestParam("token") String token, @RequestParam("uuid") String uuid,HttpServletResponse response){
        response.reset();
        ObjectMapper objectMapper = new ObjectMapper();
        CurrentUser currentUser = objectMapper.convertValue(ThreadLocalUtil.get("currentUser"),CurrentUser.class);
        String path = fileLocation+'/'+currentUser.getUuid()+fileService.getFileRelativePath(uuid);
        try (InputStream inputStream = new FileInputStream(path)) {
            response.reset();
            response.setContentType("application/octet-stream");
            String filename = new File(path).getName();
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            ServletOutputStream servletOutputStream = response.getOutputStream();
            byte[] bytes = new byte[1024];
            int len;
            while ((len = inputStream.read(bytes)) > 0) {
                servletOutputStream.write(bytes, 0, len);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
    }

    /**
     * 上传分段文件
     * @param chunkDTO
     * @return
     */
    @PostMapping("/uploadFile")
    public ResultUtil uploadFile(ChunkDTO chunkDTO){
//        System.out.println("uploadFile:"+chunkDTO.toString());
//        return ResultUtil.ok();
        return chunkService.saveChunk(chunkDTO);
    }

    /**
     * 用于检查该文件块是否已经上传的接口（已弃用）
     * @param fileInfoDTO
     * @return
     */
//    @GetMapping("/uploadFile")
//    public ChunkDTO checkChunk(ChunkDTO chunkDTO,HttpServletResponse response){
//        System.out.println("checkChunk:"+chunkDTO.toString());
//        if(!chunkService.checkChunkIsExist(chunkDTO.getIdentifier(),chunkDTO.getChunkNumber())){
//            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//        }
//        return chunkDTO;
//    }

    @PostMapping("/mergeFile")
    public ResultUtil mergeFile(FileInfoDTO fileInfoDTO){
//        System.out.println("mergeFile:"+fileInfoDTO.toString());
        return fileService.mergeFile(fileInfoDTO);
//        return ResultUtil.ok();
    }
    @PostMapping("/shareFile")
    public ResultUtil shareFile(String uuid,Integer time){
        return fileShareService.shareFile(uuid, time);
    }

    @GetMapping("/getSharedFile")
    public ResultUtil getSharedFile(String shareCode){
        return fileShareService.getSharedFile(shareCode);
    }

    @PostMapping("/renameFile")
    public ResultUtil renameFile(String uuid,String newName){
        return fileService.renameFile(uuid, newName);
    }

    @PostMapping("/deleteFile")
    public ResultUtil deleteFile(String uuid){
        return fileService.deleteFile(uuid);
    }

    @GetMapping("/getFileMD5")
    public String getFileMD5(String url) throws IOException {
        return DigestUtils.md5DigestAsHex(new FileInputStream(url));
    }

    @GetMapping("/getFileRelativePath")
    public ResultUtil getFileRelativePath(String uuid){
        fileService.getFileRelativePath(uuid);
        return ResultUtil.ok();
    }

}
