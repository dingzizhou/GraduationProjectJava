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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * 根据父文件夹查找子文件
     * @param folder 父级目录的uuid
     * @return
     */
    @GetMapping("/getChildrenFilesByFolder")
    public ResultUtil getChildrenFilesByUuid(@RequestParam("folder") String folder){
        List<FileInfoVO> files = fileService.getChildrenFilesByUuid(folder);
        return ResultUtil.ok(files);
    }

    /**
     * 根据当前目录获取上一级目录
     * @param currentFolder 当前目录的uuid
     * @return
     */
    @GetMapping("/goBackByCurrentFolder")
    public ResultUtil goBackByCurrentFolder(@RequestParam("currentFolder") String currentFolder){
        FilePojo filePojo = fileService.getById(currentFolder);
        if(filePojo == null){
            return ResultUtil.notFound();
        }
        Map<String,Object> map = new HashMap<>();
        List<FileInfoVO> fileInfoVOList = fileService.getChildrenFilesByUuid(filePojo.getFatherFolder());
        map.put("fileList",fileInfoVOList);
        map.put("folder",filePojo.getFatherFolder());
        return ResultUtil.ok(map);
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
     * 文件下载（未完成）
     * @param response
     * @throws IOException
     */
    @CrossOrigin
    @GetMapping("/download")
    public void download(@RequestParam("token") String token, @RequestParam("uuid") String uuid,HttpServletResponse response) throws IOException{
        response.reset();
        ObjectMapper objectMapper = new ObjectMapper();
        CurrentUser currentUser = objectMapper.convertValue(ThreadLocalUtil.get("currentUser"),CurrentUser.class);
        String path = fileLocation+'/'+currentUser.getUuid()+fileService.getFileRelativePath(uuid);
        InputStream inputStream = new FileInputStream(path);
        response.reset();
        response.setContentType("application/octet-stream");
        String filename = new File(path).getName();
        response.addHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(filename,"UTF-8"));
        ServletOutputStream servletOutputStream = response.getOutputStream();
        byte[] bytes = new byte[1024];
        int len;
        while((len=inputStream.read(bytes))>0){
            servletOutputStream.write(bytes,0,len);
        }
        inputStream.close();
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
        fileService.mergeFile(fileInfoDTO);
        return ResultUtil.ok();
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
