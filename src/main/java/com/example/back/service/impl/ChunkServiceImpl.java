package com.example.back.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back.mapper.ChunkMapper;
import com.example.back.model.dto.front2backDTO.ChunkDTO;
import com.example.back.model.pojo.ChunkPojo;
import com.example.back.service.ChunkService;
import com.example.back.util.ResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ChunkServiceImpl extends ServiceImpl<ChunkMapper, ChunkPojo> implements ChunkService {

    private static final Logger logger = LoggerFactory.getLogger(ChunkServiceImpl.class);

    @Value("${file.chunkLocation}")
    private String chunkLocation;

    @Override
    public boolean checkChunkIsExist(String identifier, Integer chunkNumber) {
        QueryWrapper<ChunkPojo> chunkPojoQueryWrapper = new QueryWrapper<>();
        chunkPojoQueryWrapper.eq("identifier",identifier).eq("chunk_number",chunkNumber);
        List<ChunkPojo> chunkPojoList = list(chunkPojoQueryWrapper);
        return chunkPojoList.size() != 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil saveChunk(ChunkDTO chunkDTO) {
        MultipartFile file = chunkDTO.getFile();
        // 如果chunk已经上传则直接返回true
        if(checkChunkIsExist(chunkDTO.getIdentifier(), chunkDTO.getChunkNumber())){
            return ResultUtil.ok();
        }
        try{
            // 创建保存该chunkFolder文件夹目录便于管理
            StringBuilder chunkFolderPath = new StringBuilder();
            chunkFolderPath.append(chunkLocation).append('/').append(chunkDTO.getIdentifier());
            if (!Files.isWritable(Paths.get(chunkFolderPath.toString()))) {
                logger.info("path not exist,create path: {}", chunkFolderPath.toString());
                try {
                    Files.createDirectories(Paths.get(chunkFolderPath.toString()));
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }

            // 将文件保存到该chunkFolder下
            String chunkPath = chunkFolderPath.append("/").append(chunkDTO.getChunkNumber()).toString();
            ChunkPojo chunkPojo = new ChunkPojo(IdUtil.getSnowflakeNextIdStr(),chunkDTO);
            chunkPojo.setRelativePath(chunkPath);
            save(chunkPojo);
            byte[] bytes = file.getBytes();
            Path path = Paths.get(chunkPath);
            Files.write(path,bytes);
        } catch (IOException e) {
            logger.error(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResultUtil.fail(e.getMessage());
        }
        return ResultUtil.ok();
    }

    /**
     * 文件合并
     *
     * @param targetFile
     * @param folder
     */
    public ResultUtil merge(String targetFile, String folder, String filename) {
        try {
            Files.createFile(Paths.get(targetFile));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        try (Stream<Path> stream = Files.list(Paths.get(folder))) {
            stream.filter(path -> !path.getFileName().toString().equals(filename))
                    .sorted((o1, o2) -> {
                        String p1 = o1.getFileName().toString();
                        String p2 = o2.getFileName().toString();
                        int i1 = p1.lastIndexOf("-");
                        int i2 = p2.lastIndexOf("-");
                        return Integer.valueOf(p2.substring(i2)).compareTo(Integer.valueOf(p1.substring(i1)));
                    })
                    .forEach(path -> {
                        try {
                            //以追加的形式写入文件
                            Files.write(Paths.get(targetFile), Files.readAllBytes(path), StandardOpenOption.APPEND);
                            //合并后删除该块
                            Files.delete(path);
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                    });
        } catch (IOException e) {
            logger.error(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResultUtil.fail(e.getMessage());
        }
        return ResultUtil.ok();
    }
}
