package com.meiya.alarm.upload;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meiya.alarm.cache.CommonCacheLoader;
import com.meiya.alarm.pojo.UploadData;
import com.meiya.alarm.pojo.User;
import com.meiya.alarm.util.FtpUtil;
import com.meiya.alarm.util.NameUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.CharSet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 接收队列数据封装并上传文件
 */
@Component
public class UploadListener implements MessageListener{

    private Logger LOG = Logger.getLogger(UploadListener.class);

    @Autowired
    private Gson gson;

    @Value("${alarm.export.fileLines}")
    private long fileLines;

    @Value("${alarm.temp.path}")
    private String tempPath;

    @Value("${alarm.bak.path}")
    private String bakPath;

//    private static Map<Integer, List<Map<String, String>>> userDataMap = Collections.synchronizedMap(new HashMap<Integer, List<Map<String, String>>>());
    public static Map<Integer, List<Map<String, String>>> userDataMap = new ConcurrentHashMap<>();



    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage){
            String json = null;
            try {
                json = ((TextMessage)message).getText();
                if (StringUtils.isBlank(json)) {
                    return;
                }

                //将单条数据还原为map
                UploadData uploadData = gson.fromJson(json, new TypeToken<UploadData>() {
                }.getType());

                //用户ftp信息封装
                User user = uploadData.getUser();

                if (userDataMap.get(user.getUserid()) == null){
                    userDataMap.put(user.getUserid(), new LinkedList<>());
                }

                List list = userDataMap.get(user.getUserid());
                list.add(uploadData.getDataMap());


            }catch (Exception e){
                LOG.error("", e);
            }
        }
    }


}
