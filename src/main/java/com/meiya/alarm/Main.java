package com.meiya.alarm;

import com.meiya.alarm.scan.runner.ScanRunner;
import com.meiya.alarm.util.SpringContextHolder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Future;

/**
 * 布控程序的总入口
 * 布控程序分为解析和布控两个主要模块
 * Created by Administrator on 2017/12/19.
 */

public class Main {
    private static Logger LOG = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        ApplicationContext springContext = new ClassPathXmlApplicationContext("spring-common.xml");
        LOG.info("加载spring完毕");
        //加载scan线程池和线程
        ScanRunner scanRunner = SpringContextHolder.getBean("scanRunner");
        ThreadPoolTaskExecutor pool = SpringContextHolder.getBean("scanThreadPool");
        Future<?> submit = pool.submit(scanRunner);

    }
}
