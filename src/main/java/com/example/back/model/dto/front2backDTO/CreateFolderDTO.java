package com.example.back.model.dto.front2backDTO;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class CreateFolderDTO {

    @Pattern(regexp = "^[^\\\\/:*?\"<>|]{1,255}$", message = "文件名不合法！")
    private String fileName;
    private String currentFolder;
}
