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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
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
    public ResultUtil getChildrenFiles(String path) {
        List<FileInfoVO> fileInfoVOList = new ArrayList<>();
        path = fileLocation+"/"+CurrentUser.getCurrentUser().getUuid()+path;
        File file = new File(path);
        if(!file.isDirectory()){
            return ResultUtil.fail("该路径不是文件夹");
        }
        for(File item : file.listFiles()){
            if(item.isHidden()) continue;
            fileInfoVOList.add(new FileInfoVO(item,item.getPath().substring(fileLocation.length())));
        }
        return ResultUtil.ok(fileInfoVOList);
    }

    @Override
    public ResultUtil goBack(String path) {
        if(path.equals("")) return ResultUtil.notFound();
        String basicPath = fileLocation + "/" + CurrentUser.getCurrentUser().getUuid();
        path = basicPath+path;
        File file = new File(path);
        if(!file.exists()) return ResultUtil.fail("当前目录无效");
        File parentFile = file.getParentFile();
        if(parentFile==null) return ResultUtil.fail("找不到父文件夹");
        String parentPath = parentFile.getPath().substring(basicPath.length());
        ResultUtil res = getChildrenFiles(parentPath);
        if(res.getStatus()!=200) return res;
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("path",parentPath);
        resMap.put("fileList",res.getData());
        return ResultUtil.ok(resMap);
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
    public Boolean isFileExist(String path) {
        File file = new File(path);
        if(file.exists()){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil createFolder(CreateFolderDTO createFolderDTO) {
        String path;
        if(!createFolderDTO.getCurrentFolder().isEmpty()){
            path = fileLocation+'/'+CurrentUser.getCurrentUser().getUuid()+createFolderDTO.getCurrentFolder()+'/'+createFolderDTO.getFileName();
        }
        else{
            path = fileLocation+'/'+CurrentUser.getCurrentUser().getUuid()+'/'+createFolderDTO.getFileName();
        }
        if(isFileExist(path)){
            return ResultUtil.fail("存在重复的文件夹");
        }

        try{
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil renameFile(String filePath, String newName) {
        String uuid = filePath.substring(filePath.indexOf("/",1),filePath.indexOf("/",2));
        if(!uuid.equals(CurrentUser.getCurrentUser().getUuid())){
            return ResultUtil.forbidden("不是你的文件");
        }
        String path = fileLocation + filePath;
        String newPath = path.substring(0,path.lastIndexOf("/")) + newName;

        File oldFile = new File(path);
        File newFile = new File(newPath);
        if(newFile.exists()){
            return ResultUtil.fail("存在重复的文件夹名");
        }
        if(oldFile.renameTo(newFile)){
            return ResultUtil.ok();
        }
        else {
            return ResultUtil.fail("重命名文件名失败");
        }
    }

    @Override
    public ResultUtil deleteFile(String path) {
        path = fileLocation + "/" + CurrentUser.getCurrentUser().getUuid() + path;
        File file = new File(path);
        if(!file.exists()){
            return ResultUtil.notFound();
        }
        if(file.delete()){
            return ResultUtil.ok();
        }
        else {
            logger.error("文件删除失败");
            return ResultUtil.fail("");
        }
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
