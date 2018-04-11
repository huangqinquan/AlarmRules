package com.meiya.alarm.pojo;

import java.util.Collection;
import java.util.Map;

/**
 * 统一解析成的比对对象模型
 * 数据统一抽象为键值对
 * Created by Administrator on 2017/12/19.
 */
public class AlarmData {

    /**
     * 数据键值对
     */
    private Map<String, String> dataMap;

    public Map<String, String> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, String> dataMap) {
        this.dataMap = dataMap;
    }
//    /**
//     * 要用来做比对的key集合
//     */
//    private Collection<String> alarmKeys;
}
