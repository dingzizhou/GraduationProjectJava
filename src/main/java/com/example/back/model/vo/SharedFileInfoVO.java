package com.example.back.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SharedFileInfoVO {

    private String uuid;

    private String fileName;

    private String updateTime;

    private String fileType;

}
