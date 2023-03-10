package com.example.back.util;

import java.util.HashMap;
import java.util.Map;

public class ThreadLocalUtil  {

    private static final ThreadLocal<Map<String,Object>> THREAD_LOCAL = new ThreadLocal<>();

    public static Map<String,Object> getLocalMap(){
        Map<String,Object> map = THREAD_LOCAL.get();
        if(map == null){
            map = new HashMap<>();
            THREAD_LOCAL.set(map);
        }
        return map;
    }

    public static void set(String key,Object value){
        Map<String,Object> map = getLocalMap();
        map.put(key, value);
    }

    public static Object get(String key){
        Map<String,Object> map = getLocalMap();
        return map.get(key);
    }
}
