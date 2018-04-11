package com.meiya.alarm.test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.greenpineyu.fel.FelEngine;
import com.greenpineyu.fel.FelEngineImpl;
import com.meiya.alarm.cache.CommonCacheLoader;
import com.meiya.alarm.cache.RedisService;
import com.meiya.alarm.dao.PreFilterRulesDao;
import com.meiya.alarm.dao.UserDao;
import com.meiya.alarm.parse.cmd.AlarmCommand;
import com.meiya.alarm.pojo.PreFilterRules;
import com.meiya.alarm.pojo.User;
import com.meiya.alarm.scan.processor.DataProcessor;
import com.meiya.alarm.scan.processor.Processor;
import com.meiya.alarm.util.FtpUtil;
import com.meiya.alarm.util.NameUtil;
import com.meiya.alarm.util.SpringContextHolder;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.LinkedCaseInsensitiveMap;
import sun.net.ftp.FtpClient;

import javax.sound.midi.Soundbank;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/12/19.
 */
public class TestAC extends BaseJunit4Test{

    @Autowired
    public PreFilterRulesDao preFilterRulesDao;

    @Autowired
    public UserDao userDao;

    @Autowired
    public Gson gson;

    public static int i = 0;

    @Value("${alarm.parse.maxExportCount}")
    private long maxExportCount;

    @Value("${alarm.export.fileLines}")
    private long fileLines;

    /**
     * AC自动机测试
     */
    @Test
    public void testAC(){
        String text = "中华人民共和国";
        Trie trie = Trie.builder()
                .addKeyword("中华")
                .addKeyword("人民")
                .addKeyword("中华")
                .build();
        Collection<Emit> result = trie.parseText(text);
        System.out.println(result);

    }

    @Test
    public void testTryCatch(){
        try {
            try {
                throw new RuntimeException("aaa");
            }catch (Exception e){
                System.out.println("内嵌层");
            }
        }catch (Exception e){
            System.out.println("最外层");
        }
    }

    @Test
    public void testAnno(){
        ApplicationContext context = SpringContextHolder.getApplicationContext();
        Map<String, Object> map = context.getBeansWithAnnotation(Processor.class);
        System.out.println(map);
        Map<String, DataProcessor> map1 = context.getBeansOfType(DataProcessor.class);

        Set<Map.Entry<String, DataProcessor>> entries = map1.entrySet();
        for(Map.Entry<String, DataProcessor> entry :entries){
            DataProcessor value = entry.getValue();
            Processor annotation = value.getClass().getAnnotation(Processor.class);
//            String name = annotation.name();
//            System.out.println(name);
        }
//        map1.get("myCloudProcessor")
    }

    @Test
    public void testSql(){
        List<PreFilterRules> rulesList = preFilterRulesDao.getRulesList();
        System.out.println(rulesList.size());
    }

    @Test
    public void testValue(){
//        List<Integer> nums = new ArrayList<Integer>() {{
//            add(1);
//        }};
        Set<String> sets = new HashSet<String>(){{
            add("aaa");
            add("bbb");
        }};
    }

    @Test
    public void testCache(){
//       CommonCacheLoader.preFilterRuleseList.forEach(rule -> {
//           System.out.println(rule.getAddUserId());
//       });
        System.out.println(NameUtil.name("F://MyData//temp"));

    }

    @Test
    public void testThreadPool(){
        ThreadPoolTaskExecutor pool = SpringContextHolder.getBean("scanThreadPool");


        pool.submit(new Runnable() {
            @Override
            public void run() {
                while (true){
//                    System.out.println("我是线程");
                    System.out.println(i++);
                    try {
                        Thread.sleep(1 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        try {
            Thread.sleep( 10 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testParse(){
        try {
            String data = FileUtils.readFileToString(new File("F:\\MyData\\test.json"), "UTF-8");
            Gson gson = SpringContextHolder.getBean("gson");
            List<Map<String, String>> dataList = gson.fromJson(data, new TypeToken<List<Map<String, String>>>() {
            }.getType());
            System.out.println(dataList.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testStringUtils(){
        String key = "title:厦门";
        System.out.println(StringUtils.substringAfter(key, ":"));
    }

    @Test
    public void testAlarmKeyword(){
        try {
            String text = FileUtils.readFileToString(new File("F:\\MyData\\text.txt"), "UTF-8");
            FelEngine fel = new FelEngineImpl();
//            System.out.println(text);
            Map<String,Boolean> hitKeyMap = new HashMap<>(); //命中关键字集合
            AlarmCommand cmd = new AlarmCommand();
            Map<String, PreFilterRules> hitKeywordRules = cmd.getHitKeywordRules(text, CommonCacheLoader.trie, hitKeyMap);
//            System.out.println(hitKeywordRules.size());
            Map<String, PreFilterRules> matchKeyRules = cmd.matchKeyRules(fel, hitKeyMap, hitKeywordRules);
            System.out.println(matchKeyRules);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testAlarmOther(){
        try {
            Gson gson = new Gson();
            FelEngine fel = new FelEngineImpl();
            AlarmCommand cmd = new AlarmCommand();
            String text = FileUtils.readFileToString(new File("F:\\MyData\\test.json"), "UTF-8");
            List<Map<String, String>> dataList = gson.fromJson(text, new TypeToken<List<Map<String, String>>>() {}.getType());
            dataList.forEach(dataMap -> {
                Map<String,Boolean> hitOtherMap = new HashMap<>(); //命中其它规则集合
                Map<String,PreFilterRules> hitOtherRules = cmd.getHitOtherRules(dataMap, hitOtherMap);
                Map<String,PreFilterRules> matchOtherRules  = cmd.matchOtherRules(fel, hitOtherMap, hitOtherRules);
                System.out.println(matchOtherRules);
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testFtp(){
        User user = CommonCacheLoader.userMap.get(1);
        List<File> fileList = Arrays.stream(new File("F:\\MyData\\ftp").listFiles()).collect(Collectors.toList());
//        fileList.forEach(file -> {
//            System.out.println(file.getName());
//        });
        try {
            FtpUtil.upload(user.getFtphost(), 21, 0 , user.getFtpusername(), user.getFtppassword(), user.getFtpdir(), fileList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testName(){
//        String path = "F://MyData//temp";
//        System.out.println(NameUtil.name(path));
        System.out.println(fileLines);
    }

    @Test
    public void testError(){
        System.out.println(new Gson().toJson(null));
    }

    @Test
    public void testQuartz(){
        System.out.println("aaa");
        try {
            Thread.sleep(15 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testForEach(){
        Set<String> sets = new HashSet<>();
        sets.add("1");
        sets.add("2");
        sets.forEach(s -> {
            if (s.equals("1")){
                return;
            }
            System.out.println(s);
        });
    }

    @Test
    public void testFtpDir(){
        try {
            FTPClient ftpClient = FtpUtil.getFTPClient("172.16.1.140", 21, 0, "ftpadmin", "my2018", "test111");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGson(){
        System.out.println(gson.toJson("aaa"));
    }

    @Test
    public void testMap(){
        System.out.println(NameUtil.nameFtp("/home/alarmftp"));
    }

    @Test
    public void testUpdate(){
//        System.out.println(userDao.updateExportNum(1, 999));
    }

    @Test
    public void testSub(){
        String key = "Alarm:aaa";
        System.out.println(StringUtils.substringAfter(key, RedisService.redisKey));
    }

}
