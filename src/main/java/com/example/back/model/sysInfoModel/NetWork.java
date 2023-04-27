package com.example.back.model.sysInfoModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NetWork {

    /**
     * 上行速度
     */
    private String txPercent ;
    /**
     * 下行速度
     */
    private String rxPercent ;

}
