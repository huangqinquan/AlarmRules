package com.meiya.alarm.scan.runner;

import com.google.gson.Gson;
import com.meiya.alarm.pojo.AlarmData;
import com.meiya.alarm.scan.processor.DataProcessor;
import com.meiya.alarm.scan.processor.Processor;
import com.meiya.alarm.scan.processor.impl.MyCloudProcessor;
import com.meiya.alarm.util.SpringContextHolder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Session;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * 1.单线程异步扫描文件目录
 * 2.转换成统一数据模型
 * 3.提交到消息队列
 * Created by Administrator on 2017/12/19.
 */
@Component
@Scope("prototype")
public class ScanRunner implements Runnable{

    private Logger LOG = Logger.getLogger(ScanRunner.class);

    @Value("${alarm.scan.path}")
    private String filePath;

    @Value("${alarm.temp.path}")
    private String tempPath;

    @Value("${alarm.scan.suffix}")
    private String fileSuffix;

    @Value("${alarm.queue.name}")
    private String queueName;

    private final static String processor = "Processor";

    /**
     * 数据种类 关联对应的处理器
     */
    @Value("${alarm.scan.dataType}") //默认MyCloud
    private String dataType;

    @Autowired
    private Gson gson;

    /*
    线程暂停时长
     */
    private long pauseMill = 1 * 1000;

    @Override
    public void run() {

        //获得对应的消息处理器
        ApplicationContext context = SpringContextHolder.getApplicationContext();
        Map<String, DataProcessor> map = context.getBeansOfType(DataProcessor.class);
        DataProcessor dataProcessor;
        if (map.get(dataType) == null){
            dataProcessor = map.get("myCloud" + processor);
        }else {
            dataProcessor = map.get(dataType + processor);
        }

        while (true){
            try {
                File fileDir = new File(filePath);
                if (!fileDir.exists()){
                    LOG.error("无法读取扫描路径,请检查扫描路径!" + filePath);
                    Thread.sleep(pauseMill);
                    continue;
                }

                File tempDir = new File(tempPath);
                if (!tempDir.exists()){
                    LOG.error("无法读取临时目录,请检查临时目录!" + filePath);
                    Thread.sleep(pauseMill);
                    continue;
                }

                Collection<File> files = FileUtils.listFiles(fileDir, new String[]{fileSuffix}, false);
                if (files.size() == 0){
                    LOG.info("目录下暂无文件!" + filePath);
                    Thread.sleep(pauseMill);
                    continue;
                }


                files.forEach(f -> {
                    try {
                        String text = FileUtils.readFileToString(f, "UTF-8");
                        Collection<AlarmData> alarmDatas = dataProcessor.parseToMap(text);
                        alarmDatas.parallelStream().forEach(alarmData -> {
                            String json = gson.toJson(alarmData);
                            JmsTemplate jmsTemplate = SpringContextHolder.getBean("jmsTemplate");
                            jmsTemplate.send(queueName, (session) -> {
                                return session.createTextMessage(json);
                            });

                        });
                    } catch (IOException e) {
                        LOG.error("读取文件解析发送出错" + f.getAbsolutePath());
                    }
                    //一个文件扫描完毕后删除
                    if (f.delete()){
                        LOG.info("文件" + f.getName() + "解析完毕删除成功");
                    }else {
                        LOG.info("文件" + f.getName() + "解析完毕删除失败");
                    }
                });

            }catch (Exception e){
                LOG.error("", e);
            }
        }
    }
}
