package com.example.back.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back.mapper.FileMapper;
import com.example.back.model.dto.CurrentUser;
import com.example.back.model.dto.front2backDTO.CreateFolderDTO;
import com.example.back.model.dto.front2backDTO.FileInfoDTO;
import com.example.back.model.pojo.FilePojo;
import com.example.back.model.vo.FileInfoVO;
import com.example.back.service.FileService;
import com.example.back.util.ResultUtil;
import com.example.back.util.ThreadLocalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, FilePojo> implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Value("${file.basicLocation}")
    private String fileLocation;
    @Value("${file.chunkLocation}")
    private String chunkLocation;

    @Autowired
    private FileMapper fileMapper;

    @Override
    public List<FileInfoVO> getChildrenFilesByUuid(String fatherFolder) {
        ObjectMapper objectMapper = new ObjectMapper();
        CurrentUser currentUser = objectMapper.convertValue(ThreadLocalUtil.get("currentUser"),CurrentUser.class);
        QueryWrapper<FilePojo> filePojoQueryWrapper = new QueryWrapper<>();
        filePojoQueryWrapper.eq("father_folder",fatherFolder).ne("file_status",0).eq("file_owner",currentUser.getUuid());
        List<FilePojo> filePojoList =list(filePojoQueryWrapper);
        List<FileInfoVO> fileInfoVOList = new ArrayList<>();
        for(FilePojo item : filePojoList){
            fileInfoVOList.add(new FileInfoVO(item));
        }
        return fileInfoVOList;
    }

    @Override
    public Boolean isFileExist(String fatherFolderUuid, String fileName, String fileOwner) {
        QueryWrapper<FilePojo> filePojoQueryWrapper = new QueryWrapper<>();
        filePojoQueryWrapper.eq("father_folder",fatherFolderUuid).eq("file_name",fileName).eq("file_owner",fileOwner);
        List<FilePojo> filePojoList = list(filePojoQueryWrapper);
        if(filePojoList.size()!=0){
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil createFolder(CreateFolderDTO createFolderDTO) {
        ObjectMapper objectMapper = new ObjectMapper();
        CurrentUser currentUser = objectMapper.convertValue(ThreadLocalUtil.get("currentUser"),CurrentUser.class);
        if(isFileExist(createFolderDTO.getCurrentFolder(), createFolderDTO.getFileName(),currentUser.getUuid())){
            return ResultUtil.fail("存在重复的文件夹");
        }

        try{
            FilePojo filePojo = new FilePojo();
            filePojo.setUuid(IdUtil.getSnowflakeNextIdStr());
            filePojo.setFileOwner(currentUser.getUuid());
            filePojo.setFatherFolder(createFolderDTO.getCurrentFolder());
            filePojo.setFileName(createFolderDTO.getFileName());
            filePojo.setFileSize(0L);
            filePojo.setIsFolder(1);
            filePojo.setFileUpdateTime(new Date());
            save(filePojo);
            String path;
            if(!createFolderDTO.getCurrentFolder().isEmpty()){
                path = fileLocation+'/'+currentUser.getUuid()+getFileRelativePath(createFolderDTO.getCurrentFolder())+'/'+createFolderDTO.getFileName();
            }
            else{
                path = fileLocation+'/'+currentUser.getUuid()+'/'+createFolderDTO.getFileName();
            }
            File file = new File(path);
            if(file.mkdir()){

            }
            else{
                throw new Exception("创建文件加失败");
            }
        }
        catch (Exception e){
            logger.error(e.getMessage(),e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResultUtil.fail(e.getMessage());
        }
        return ResultUtil.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil mergeFile(FileInfoDTO fileInfoDTO) {
        ObjectMapper objectMapper = new ObjectMapper();
        CurrentUser currentUser = objectMapper.convertValue(ThreadLocalUtil.get("currentUser"),CurrentUser.class);
        String targetFilePath;
        String chunkFolder = chunkLocation+'/'+fileInfoDTO.getIdentifier();
        if(!fileInfoDTO.getFatherFolder().isEmpty()){
            targetFilePath = fileLocation+'/'+currentUser.getUuid()+getFileRelativePath(fileInfoDTO.getFatherFolder())+'/'+fileInfoDTO.getFilename();
        }
        else{
            targetFilePath = fileLocation+'/'+currentUser.getUuid()+'/'+fileInfoDTO.getFilename();
        }
        try {
            // 保存文件信息
            FilePojo filePojo = new FilePojo();
            filePojo.setUuid(IdUtil.getSnowflakeNextIdStr());
            filePojo.setFileOwner(currentUser.getUuid());
            filePojo.setFatherFolder(fileInfoDTO.getFatherFolder());
            filePojo.setFileName(fileInfoDTO.getFilename());
            filePojo.setFileSize(fileInfoDTO.getTotalSize());
            filePojo.setIsFolder(0);
            filePojo.setFileUpdateTime(new Date());
            save(filePojo);

            // 创建文件 如果存在相同的文件 说明是之前创建失败的 则删除
            File checkIfExistsFile = new File(targetFilePath);
            if(checkIfExistsFile.exists()){
                checkIfExistsFile.delete();
            }
            Files.createFile(Paths.get(targetFilePath));

            // 将chunk依次写进新文件
            Stream<Path> pathList = Files.list(Paths.get(chunkFolder));
            pathList.sorted((file1,file2)->{
                        Integer num1 = Integer.valueOf(file1.getFileName().toString());
                        Integer num2 = Integer.valueOf(file2.getFileName().toString());
                        return num1.compareTo(num2);
                    })
                    .forEach(path ->{
                        try {
                            // 以追加的形式写入文件
                            Files.write(Paths.get(targetFilePath),Files.readAllBytes(path), StandardOpenOption.APPEND);
                        } catch (Exception e) {
                            logger.error(e.getMessage(),e);
//                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//                            return ResultUtil.fail(e.getMessage());
                        }
                    });
        }
        catch (Exception e){
            logger.error(e.getMessage(),e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResultUtil.fail(e.getMessage());
        }
        return ResultUtil.ok();
    }

    public String getFileRelativePath(String uuid){
//        List<String> res = fileMapper.getFileRelativePath(uuid);
//        StringBuilder relativePath = new StringBuilder();
//        for(String item : res){
//            relativePath.append('/').append(item);
//        }
//        return relativePath.toString();
        String path = "";
        FilePojo filePojo = getById(uuid);
//        System.out.println(filePojo.toString());
//        System.out.println(uuid);
        while(filePojo!=null){
            path = "/"+filePojo.getFileName()+path;
            filePojo = getById(filePojo.getFatherFolder());
        }
        return path;
    }
}
