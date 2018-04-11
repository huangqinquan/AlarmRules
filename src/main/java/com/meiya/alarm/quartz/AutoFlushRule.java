package com.meiya.alarm.quartz;


import com.meiya.alarm.cache.CommonCacheLoader;
import com.meiya.alarm.util.SpringContextHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by huangqq on 2017/12/23.
 */
@Component
public class AutoFlushRule{

    @Scheduled(cron = "0 */1 * * * ?")
    public void doFlushEveryMinute(){
        CommonCacheLoader commonCacheLoader = SpringContextHolder.getBean("commonCacheLoader");
        commonCacheLoader.init();
    }
}
