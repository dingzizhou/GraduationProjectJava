package com.example.back.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back.mapper.FileShareMapper;
import com.example.back.model.pojo.FilePojo;
import com.example.back.model.pojo.FileSharePojo;
import com.example.back.model.vo.FileInfoVO;
import com.example.back.service.FileService;
import com.example.back.service.FileShareService;
import com.example.back.util.ResultUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Date;
import java.util.List;

@Service
public class FileShareServiceImpl extends ServiceImpl<FileShareMapper, FileSharePojo> implements FileShareService {

    private static final Logger logger = LoggerFactory.getLogger(FileShareServiceImpl.class);

    @Resource
    private FileService fileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil shareFile(String uuid, Integer time) {
        String randomNumber = RandomUtil.randomNumbers(4);
        QueryWrapper<FileSharePojo> fileSharePojoQueryWrapper = new QueryWrapper<>();
        fileSharePojoQueryWrapper.eq("share_code",randomNumber);
        while (list(fileSharePojoQueryWrapper).size()!=0){
            randomNumber = RandomUtil.randomNumbers(4);
            fileSharePojoQueryWrapper = new QueryWrapper<>();
            fileSharePojoQueryWrapper.eq("share_code",randomNumber);
        }
        try {
            FileSharePojo fileSharePojo = new FileSharePojo();
            fileSharePojo.setUuid(IdUtil.getSnowflakeNextIdStr());
            fileSharePojo.setFileUuid(uuid);
            fileSharePojo.setValidity(time);
            fileSharePojo.setShareCode(randomNumber);
            fileSharePojo.setCreateTime(new Date());
            save(fileSharePojo);
            return ResultUtil.ok(randomNumber);
        }
        catch (Exception e){
            logger.error(e.getMessage(),e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResultUtil.fail(e.getMessage());
        }
    }

    @Override
    public ResultUtil getSharedFile(String shareCode) {
        QueryWrapper<FileSharePojo> fileSharePojoQueryWrapper = new QueryWrapper<>();
        fileSharePojoQueryWrapper.eq("share_code",shareCode);
        try {
            FileSharePojo fileSharePojo = getOne(fileSharePojoQueryWrapper);
            if(fileSharePojo == null) return ResultUtil.fail("该分享码无效");
            Date now = new Date();
            System.out.println((now.getTime()-fileSharePojo.getCreateTime().getTime())/24/60/60/1000);
            if(fileSharePojo.getValidity()<(now.getTime()-fileSharePojo.getCreateTime().getTime())/24/60/60/1000) return ResultUtil.fail("分享码过期");
            FilePojo filePojo = fileService.getById(fileSharePojo.getFileUuid());
            System.out.println(filePojo.toString());
            return ResultUtil.ok(new FileInfoVO(filePojo));
        }
        catch (Exception e){
            return ResultUtil.fail(e.getMessage());
        }
    }
}
