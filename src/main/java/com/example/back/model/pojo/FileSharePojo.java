package com.example.back.model.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@TableName("file_share")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileSharePojo {

    @TableId
    private String uuid;
    @TableField("file_uuid")
    private String fileUuid;
    @TableField("share_code")
    private String shareCode;
    @TableField("validity")
    private Integer validity;
    @TableField("create_time")
    private Date createTime;
}
