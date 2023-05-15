package com.example.back.model.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.back.model.dto.front2backDTO.ChunkDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("file_chunk")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChunkPojo {

    @TableId
    private String uuid;
    @TableField("chunk_number")
    private Integer chunkNumber;
    @TableField("chunk_size")
    private Long chunkSize;
    @TableField("current_chunk_size")
    private Long currentChunkSize;
    @TableField("total_size")
    private Long totalSize;
    @TableField("identifier")
    private String identifier;
    @TableField("filename")
    private String filename;
    @TableField("relative_path")
    private String relativePath;
    @TableField("total_chunks")
    private Integer totalChunks;
    @TableField("type")
    private String type;

    public ChunkPojo(String uuid, ChunkDTO chunkDTO) {
        this.uuid = uuid;
        this.chunkNumber=chunkDTO.getChunkNumber();
        this.chunkSize=chunkDTO.getChunkSize();
        this.currentChunkSize=chunkDTO.getCurrentChunkSize();
        this.totalSize=chunkDTO.getTotalSize();
        this.identifier=chunkDTO.getIdentifier();
        this.filename=chunkDTO.getFilename();
        this.relativePath=chunkDTO.getRelativePath();
        this.totalChunks=chunkDTO.getTotalChunks();
        this.type=chunkDTO.getType();
    }
}
