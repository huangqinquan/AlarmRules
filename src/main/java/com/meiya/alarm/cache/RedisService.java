package com.meiya.alarm.cache;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by huangqq on 2017/12/26.
 */
@Service
public class RedisService {
    private Logger LOG = Logger.getLogger(RedisService.class);

    public final static String redisKey = "Alarm:";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 记录用户布控的数据条数
     * @param userId
     * @param dataNum
     */
    public void userIncr(String userId, Long dataNum){
        try {
            redisTemplate.opsForValue().increment(redisKey + userId, dataNum);
        }catch (Exception e){
            LOG.error("", e);
        }
    }

    public Long getUserExportNum(Integer userId){
        Long exportNum = 0l;
        try {
            String tempNumStr = (String) redisTemplate.opsForValue().get(redisKey + userId);
            exportNum += Long.parseLong(tempNumStr);
        }catch (Exception e){
            LOG.error("", e);
        }
        return exportNum;
    }

    /**
     * 获取缓存中的用户
     * @return
     */
    public Set getAlarmUserKeys(){
        Set keyset = new HashSet<>();
        try {
            keyset = redisTemplate.keys(redisKey + "*");
        }catch (Exception e){
            LOG.error("", e);
        }
        return keyset;
    }

    /**
     * 删除key
     */
    public void deleteAlarmUser(String key){
        redisTemplate.delete(key);
    }
}
