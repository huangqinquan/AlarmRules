package com.meiya.alarm.scan.processor.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meiya.alarm.pojo.AlarmData;
import com.meiya.alarm.scan.processor.DataProcessor;
import com.meiya.alarm.scan.processor.Processor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/12/19.
 */

/**
 * 处理my搜索云的数据
 */
@Processor
public class MyCloudProcessor implements DataProcessor<String>{

    private Logger LOG = Logger.getLogger(MyCloudProcessor.class);

    @Autowired
    private Gson gson;

    @Override
    public Collection<AlarmData> parseToMap(String data) {
        List<Map<String, String>> dataList;
        List<AlarmData> alarmDataList = new LinkedList<>();
        try {
            dataList = gson.fromJson(data, new TypeToken<List<Map<String, String>>>() {}.getType());
            if (dataList == null){
                LOG.error("无法解析数据" + data);
                return alarmDataList;
            }
            dataList.forEach(map -> {
                //遍历map的key全部换成小写
                map.keySet().forEach(key -> {

                });
                AlarmData alarmData = new AlarmData();
                alarmData.setDataMap(map);
                alarmDataList.add(alarmData);
            });
        }catch (Exception e){
            LOG.error("解析搜索云数据出错!", e);
        }
        return alarmDataList;
    }
}
