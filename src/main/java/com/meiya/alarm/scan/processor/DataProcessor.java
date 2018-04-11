package com.meiya.alarm.scan.processor;

import com.meiya.alarm.pojo.AlarmData;

import java.util.Collection;
import java.util.Map;

/**
 * 数据处理器统一接口
 * 1.将传入的单条原始数据解析成键值对
 * Created by Administrator on 2017/12/19.
 */
public interface DataProcessor<e> {

    Collection<AlarmData> parseToMap(e data);


}
