package com.example.back.model.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@TableName("file")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilePojo {

    @TableId
    private String uuid;
    @TableField("file_owner")
    private String fileOwner;
    @TableField("father_folder")
    private String fatherFolder;
    @TableField("file_name")
    private String fileName;
    @TableField("file_url")
    private String fileURl;
    @TableField("file_size")
    private Long fileSize;
    @TableField("is_folder")
    private Integer isFolder;
    @TableField("file_updateTime")
    private Date fileUpdateTime;
    @TableField("file_status")
    private Integer fileStatus;
    @TableField("file_delete_time")
    private Date fileDeleteTime;

}
