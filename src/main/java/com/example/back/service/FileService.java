package com.example.back.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.back.model.dto.front2backDTO.CreateFolderDTO;
import com.example.back.model.dto.front2backDTO.FileInfoDTO;
import com.example.back.model.pojo.FilePojo;
import com.example.back.model.vo.FileInfoVO;
import com.example.back.util.ResultUtil;

import java.util.List;

public interface FileService extends IService<FilePojo>  {

    List<FileInfoVO> getChildrenFilesByUuid(String fatherFolder);

    Boolean isFileExist(String fatherFolderUuid,String fileName,String fileOwner);

    ResultUtil createFolder(CreateFolderDTO createFolderDTO);

    ResultUtil mergeFile(FileInfoDTO fileInfoDTO);

    String getFileRelativePath(String uuid);
}
