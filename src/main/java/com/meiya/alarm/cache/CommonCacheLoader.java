package com.meiya.alarm.cache;

import com.meiya.alarm.dao.PreFilterRulesDao;
import com.meiya.alarm.dao.UserDao;
import com.meiya.alarm.pojo.PreFilterRules;
import com.meiya.alarm.pojo.User;
import com.meiya.alarm.util.SpringContextHolder;
import org.ahocorasick.trie.Trie;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.io.IOException;
import java.util.*;

/**
 * Created by Administrator on 2017/12/20.
 */
@Component
public class CommonCacheLoader {

    private Logger LOG = Logger.getLogger(CommonCacheLoader.class);

    @Autowired
    private PreFilterRulesDao preFilterRulesDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private FilterRulesSvc filterRulesSvc;

    //原始规则缓存
    public static volatile List<PreFilterRules> preFilterRuleseList = new LinkedList<>();

    //用户缓存
    public static volatile Map<Integer, User> userMap = new HashMap<>();

    //关键词规则
    public static volatile Map<String,Map<String,List<PreFilterRules>>> keyRulesMap = new HashMap<>();

    //规则型规则
    public static volatile Map<String,Map<String,List<PreFilterRules>>> otherRulesMap = new HashMap<>();

    //导出字段缓存
    public static Set<String> exportKeySet = new HashSet<>();

    //不同数据源对应字段
    public static Set<String> fieldSet = new HashSet<>();

    //AC树
    public static Trie trie;

    public static long maxExportCount = 100000l;
    public static String dataType = "myCloud";
    public static String queueName = "AlarmQueue";

    @PostConstruct
    public void init(){
        LOG.info("====开始加载缓存====");
        //加载配置文件
        initProperties();
        //加载布控规则
        initFilterRules();


    }

    public void initOneMinute(){
        LOG.info("====定时刷新缓存====");
        //加载布控规则
        initFilterRules();

    }


    public void initProperties(){
        try {
            PropertiesConfiguration pcfg = new PropertiesConfiguration("alarm.properties");
            maxExportCount = pcfg.getLong("alarm.parse.maxExportCount");
            dataType = pcfg.getString("alarm.scan.dataType");
            queueName = pcfg.getString("alarm.queue.name");
            String filedstr = pcfg.getString("alarm.parse.field");
            Arrays.stream(StringUtils.split(filedstr, "|")).forEach(field -> {
                fieldSet.add(field);
            });
            filedstr = pcfg.getString("alarm.export.field");
            Arrays.stream(StringUtils.split(filedstr, "|")).forEach(field -> {
                exportKeySet.add(field);
            });
        } catch (ConfigurationException e) {
            LOG.error("读取配置文件出错", e);
        }
    }

    /**
     * 加载用户和规则 addUserId<->userid
     */
    public void initFilterRules(){
        List<PreFilterRules> list = preFilterRulesDao.getRulesList();
        preFilterRuleseList = new LinkedList<>(list);
        list = null;

        List<User> tempList = userDao.getUserList();
        tempList.forEach(user -> {
            userMap.put(user.getUserid(), user);
        });
        tempList = null;

        CommonCacheLoader.keyRulesMap = filterRulesSvc.loadKeywordMap(preFilterRuleseList);
        CommonCacheLoader.otherRulesMap = filterRulesSvc.loadOtherRulesMap(preFilterRuleseList);
        CommonCacheLoader.trie = filterRulesSvc.constructKeywordFinder(CommonCacheLoader.keyRulesMap.keySet());
    }




}
