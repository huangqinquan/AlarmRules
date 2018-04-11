package com.meiya.alarm.quartz;

import com.meiya.alarm.cache.RedisService;
import com.meiya.alarm.dao.PreFilterRulesDao;
import com.meiya.alarm.dao.UserDao;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Created by huangqq on 2017/12/26.
 */
@Component
public class RedisToMysql {
    private Logger LOG = Logger.getLogger(RedisToMysql.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private RedisService redisService;

    @Scheduled(cron = "0 */1 * * * ?")
    public void commitMysqlFromRedis(){
        LOG.info("定时任务:提交exportNum到mysql");
        Set keySet = redisService.getAlarmUserKeys();
        keySet.forEach(key -> {
            try {
                //从缓存中读取完毕
                Integer userId = Integer.parseInt(StringUtils.substringAfter((String) key, RedisService.redisKey));
                Long exportNum = redisService.getUserExportNum(userId);
                //写入mysql
                userDao.updateExportNum(userId, exportNum);
            }catch (Exception e){
                LOG.error("", e);
            }
            redisService.deleteAlarmUser((String) key);

        });
    }
}
