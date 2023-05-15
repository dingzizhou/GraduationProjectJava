package com.example.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.back.model.pojo.FilePojo;
import com.example.back.model.vo.FileInfoVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface FileMapper extends BaseMapper<FilePojo> {
    List<String> getFileRelativePath(@Param("uuid") String uuid);
}
