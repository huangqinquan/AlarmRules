package com.meiya.alarm.quartz;

import com.google.gson.Gson;
import com.meiya.alarm.cache.CommonCacheLoader;
import com.meiya.alarm.cache.RedisService;
import com.meiya.alarm.pojo.User;
import com.meiya.alarm.upload.UploadListener;
import com.meiya.alarm.util.FtpUtil;
import com.meiya.alarm.util.NameUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by huangqq on 2017/12/25.
 * 定时扫描内存并提交到ftp
 */
@Component
public class UploadSchema {

    @Autowired
    private Gson gson;

    @Value("${alarm.temp.path}")
    private String tempPath;

    @Value("${alarm.bak.path}")
    private String bakPath;

    @Autowired
    private RedisService redisService;

    private Logger LOG = Logger.getLogger(UploadSchema.class);

    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    static final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    static final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    /**
     * 定时提交缓存
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void uplodaSchema(){
        LOG.info("定时任务:提交缓存");
        //写锁
        writeLock.lock();
        Map<Integer, List<Map<String, String>>> userDataMap = new ConcurrentHashMap<>(UploadListener.userDataMap);
        //深拷贝后清空缓存
        UploadListener.userDataMap.clear();
        //放开写锁
        writeLock.unlock();
        if (MapUtils.isEmpty(userDataMap)){
            LOG.info("==本次提交缓存没有数据==");
            return;
        }
        userDataMap.forEach((userid,dataList) -> {
            User user = CommonCacheLoader.userMap.get(userid);
            if (user == null){
                LOG.error("无法获取userid=" + userid + "的用户");
                return;
            }
            LOG.info("用户" + userid + "本次布控到" + dataList.size() + "数据");
            //同时写入缓存统计数字

            redisService.userIncr(String.valueOf(userid), Integer.toUnsignedLong(dataList.size()));

            String resultJson = "";
            try {
                resultJson = gson.toJson(dataList);
                //生成结果字符串后清空缓存
                dataList.clear();
                File file = new File(NameUtil.name(tempPath));
                if (!file.exists()){
                    //如果父目录不存在则创建父目录
                    if (!file.getParentFile().exists()){
                        file.getParentFile().mkdirs();
                    }
                    file.createNewFile();
                }
                FileUtils.writeStringToFile(file, resultJson, "UTF-8");
                //生成完毕然后上传到ftp
                FtpUtil.upload(user.getFtphost(), user.getFtpport(), 0, user.getFtpusername(), user.getFtppassword(), NameUtil.nameFtp(user.getFtpdir()), file);

            }catch (Exception e){
                try {
                    LOG.error("上传发生错误!", e);
                    String userBakPath = bakPath + File.separator + user.getUserid();
                    File file = new File(NameUtil.name(userBakPath));
                    if (!file.exists()){
                        if (!file.getParentFile().exists()){
                            file.getParentFile().mkdirs();
                        }
                        file.createNewFile();
                    }
                    FileUtils.writeStringToFile(file, resultJson, "UTF-8");
                } catch (IOException e1) {
                    LOG.error("", e);
                    return;
                }

            }

        });
    }

}
