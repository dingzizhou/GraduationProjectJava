package com.example.back.model.vo;

import com.example.back.model.pojo.FilePojo;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Path;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInfoVO {

    /**
     * 文件夹或文件名
     */
    private String fileName;
    /**
     * 文件相对路径
     */
    private String filePath;
    /**
     * 文件大小
     */
    private String fileSize;
    /**
     * 文件类型 0为文件 1为文件夹
     */
    private Integer fileType;
    /**
     * 更新时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public FileInfoVO(File file,String filePath){
        this.fileName = file.getName();
        this.filePath = filePath;
        this.fileSize = convertFileSize(file.length());
        this.fileType = file.isDirectory() ? 1 : 0;
        this.updateTime = new Date(file.lastModified());
    }

    public FileInfoVO(FilePojo file){
//        this.uuid = file.getUuid();
//        this.fileName = file.getFileName();
//        this.fatherFolder = file.getFatherFolder();
//        this.fileSize = convertFileSize(file.getFileSize());
//        this.fileType = file.getIsFolder()==1 ? 1 : 0;
//        this.updateTime = file.getFileUpdateTime();
    }

    public String convertFileSize(long size)
    {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (size >= gb)
        {
            return String.format("%.1f GB", (float) size / gb);
        }
        else if (size >= mb)
        {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        }
        else if (size >= kb)
        {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        }
        else
        {
            return String.format("%d B", size);
        }
    }
}
