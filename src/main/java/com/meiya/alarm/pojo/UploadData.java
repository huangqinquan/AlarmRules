package com.meiya.alarm.pojo;

import java.util.Map;

/**
 * Created by Administrator on 2017/12/22.
 */
public class UploadData {

    public UploadData(Map<String, String> dataMap, User user) {
        this.dataMap = dataMap;
        this.user = user;
    }

    /**
     * 数据map
     */
    private Map<String, String> dataMap;

    /**
     * 布控他的用户
     */
    private User user;

    public Map<String, String> getDataMap() {
        return dataMap;
    }

    public User getUser() {
        return user;
    }
}
