package com.example.back.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.back.model.dto.front2backDTO.ChunkDTO;
import com.example.back.model.pojo.ChunkPojo;
import com.example.back.util.ResultUtil;

public interface ChunkService extends IService<ChunkPojo> {

    /**
     * 检查文件块是否存在
     *
     * @param identifier
     * @param chunkNumber
     * @return
     */
    boolean checkChunkIsExist(String identifier, Integer chunkNumber);

    ResultUtil saveChunk(ChunkDTO chunkDTO);

    ResultUtil merge(String targetFile, String folder, String filename);
}
