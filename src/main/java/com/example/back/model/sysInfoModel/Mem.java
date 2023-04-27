package com.example.back.model.sysInfoModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Mem
{
    /**
     * 内存总量
     */
    private double total;

    /**
     * 已用内存
     */
    private double used;

    /**
     * 剩余内存
     */
    private double free;

    public double getTotal()
    {
        return total/(1024 * 1024 * 1024);
    }

    public double getUsed(){
        return used/(1024 * 1024 * 1024);
    }

    public double getFree(){
        return free/(1024 * 1024 * 1024);
    }
}