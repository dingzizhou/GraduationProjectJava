package com.example.back.model.dto.front2backDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInfoDTO {

    private String filename;

    private String identifier;

    private Integer totalChunks;

    private Long totalSize;

    private String type;

    private String fatherFolder;
}
