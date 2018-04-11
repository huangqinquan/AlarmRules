package com.meiya.alarm.parse.cmd;

import com.google.gson.Gson;
import com.greenpineyu.fel.FelEngine;
import com.greenpineyu.fel.context.MapContext;
import com.meiya.alarm.cache.CommonCacheLoader;
import com.meiya.alarm.pojo.*;
import com.meiya.alarm.util.SpringContextHolder;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.apache.commons.chain.Context;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.io.File;
import java.util.*;

/**
 * Created by Administrator on 2017/12/20.
 */
public class AlarmCommand extends SynchonizedPreprocessCommand{

    private Logger LOG = Logger.getLogger(AlarmCommand.class);

    @Autowired
    private Gson gson = SpringContextHolder.getBean("gson");

    @Override
    protected boolean syncExecute(Context ctx) {
        try {
            //获得ac树
            Trie trie = CommonCacheLoader.trie;
            //获得fel引擎
            FelEngine fel = SpringContextHolder.getBean("felEngine");
            AlarmData alarmData = (AlarmData)ctx.get(AlarmKey.alarm_data);
            //获得数据map
//            final Map<String, String> dataMap = alarmData.getDataMap();
            //修改成大小写无关的map
            final Map<String, String> dataMap = new LinkedCaseInsensitiveMap();
            dataMap.putAll(alarmData.getDataMap());
            //将map所有内容合并过AC
            StringBuffer sb = new StringBuffer();
            Set<String> fieldSet = CommonCacheLoader.fieldSet;
            //不配置的情况默认全部字段
            if (CollectionUtils.isEmpty(fieldSet)){
                dataMap.forEach((k, v) -> {
                    sb.append(v);
                });
            }else {
                fieldSet.forEach(field -> {
                    sb.append(dataMap.get(field) == null ? "":dataMap.get(field));
                });
            }

            //为了匹配联合规则
            sb.append("TRUEtrue");

            //命中关键词规则
            Map<String,Boolean> hitKeyMap = new HashMap<>(); //命中关键字集合
            Map<String, PreFilterRules> hitKeyRules = getHitKeywordRules(sb.toString(), trie, hitKeyMap);
            Map<String,PreFilterRules> matchKeyRules = matchKeyRules(fel,hitKeyMap, hitKeyRules);
            //命中其他规则
            Map<String,Boolean> hitOtherMap = new HashMap<>(); //命中其它规则集合
            Map<String,PreFilterRules> hitOtherRules = getHitOtherRules(dataMap,hitOtherMap);
            Map<String,PreFilterRules> matchOtherRules  = matchOtherRules(fel, hitOtherMap, hitOtherRules);

            String data_ruleid="";
            List<PreFilterRules> matchRulesList = new ArrayList<PreFilterRules>();

            //<addUserId, 比对次数> 同一个用户不同规则命中同一条数据只需要发送一次
            Set<Integer> distinctSet = new HashSet<>();

            for(String ruleid:matchKeyRules.keySet()){
                //首次比对肯定不会因为这个跳过 addUserId也是和用户id是对应关系
                if (distinctSet.contains(matchKeyRules.get(ruleid).getAddUserId())){
                    continue;
                }
				if( matchOtherRules.containsKey(ruleid)){
                    //确定已经命中了一条整个的规则 记录该规则命中次数
                    Integer addUserId = matchKeyRules.get(ruleid).getAddUserId();
                    distinctSet.add(addUserId);

					LOG.info("命中联合规则：" + ruleid);
                    data_ruleid+= ruleid+",";
                    matchRulesList.add(matchKeyRules.get(ruleid));
				}
            }

            matchRulesList.forEach(rule -> {
                Integer userId = rule.getAddUserId();
                User user = CommonCacheLoader.userMap.get(userId);
                if (user == null){
                    LOG.info("规则" + rule.getId() + "在用户表中没有对应的用户id" + userId);
                    return;
                }
                //整理出要导出的字段
                Set<String> keys = CommonCacheLoader.exportKeySet;
                String resultJson = "";
                if (keys.size() != 0){
                    //如果有配置字段则按照配置的字段导出数据
                    Map<String, String> tempDataMap = new HashMap<String, String>();
                    keys.forEach(key -> {
                        if (!dataMap.containsKey(key)){
                            LOG.error("配置文件中存在数据没有的key=" + key);
                            return;
                        }
                        tempDataMap.put(key, dataMap.get(key));
                    });
                    UploadData uploadData = new UploadData(tempDataMap, user);
                    resultJson = gson.toJson(uploadData);
                }else {
                    //如果没有配置字段就所有字段都导出
                    UploadData uploadData = new UploadData(dataMap, user);
                    resultJson = gson.toJson(uploadData);
                }

                //生成临时文件到临时目录 在扫描程序中已经对目录是否存在做了验证
                JmsTemplate jmsTemplate = SpringContextHolder.getBean("jmsTemplate");
                final String finalResultJson = resultJson;
                jmsTemplate.send("Temp" + CommonCacheLoader.queueName, (session) -> {
                    return session.createTextMessage(finalResultJson);
                });

            });
        }catch (Exception e){
            LOG.error("alarm出错", e);
        }
        return false;
    }


    public Map<String, PreFilterRules> getHitKeywordRules(String text,Trie trie ,Map<String, Boolean> hitKeyMap) {
        Map<String,PreFilterRules> hitKeyRules = new HashMap<String ,PreFilterRules>(); //命中规则集合
        if (trie==null){
            LOG.info("关键字查询器(AhoCorasick)为空");
            return hitKeyRules;
        }
        if (CommonCacheLoader.keyRulesMap ==null||CommonCacheLoader.keyRulesMap .size()==0){
            LOG.debug("关键字规则为空");
            return hitKeyRules;
        }
        if (StringUtils.isBlank(text)){
            LOG.error("TEXT为空，跳过");
            return hitKeyRules;
        }
        Collection<Emit> result = trie.parseText(text);
        result.forEach(emit -> {
            String hitKey = emit.getKeyword();
            //如果已经包含该关键字
            if (hitKeyMap.containsKey(hitKey)){
                return;
            }
            hitKeyMap.put(hitKey,true); //加入命中关键字集合
            if (CommonCacheLoader.keyRulesMap.containsKey(hitKey)){  //如果包含该关键字,则取出规则
//						logger.info("关键字Map中包含该关键字:" + hitKeyword);
                //根据关键字取出表达式map
                Map<String,List<PreFilterRules>> keyRulesMap = CommonCacheLoader.keyRulesMap.get(hitKey);
                for (String keyExp:keyRulesMap.keySet()){  //遍历该key对应的每一个规则
                    String ruleid ="";
                    for (PreFilterRules keyRule:keyRulesMap.get(keyExp)){
                        ruleid = keyRule.getId();
                        hitKeyRules.put(ruleid, keyRule);
                    }
                }
            }
        });
        return hitKeyRules;
    }

    public Map<String,PreFilterRules> matchKeyRules(FelEngine felEngine, Map<String, Boolean> hitKeyMap, Map<String, PreFilterRules> hitKeyRules) {
        Map<String,PreFilterRules> matchKeyRules = new HashMap<String,PreFilterRules>(); //符合规则的集合
        //遍历命中的表达式集合
        for (Map.Entry<String,PreFilterRules> entry : hitKeyRules.entrySet()) {
            PreFilterRules keyRule = entry.getValue();
            String ruleid =entry.getKey();
            //取出默认参数值,需要深度拷贝
            Map<String,Object> keyValCopy = new HashMap<>(keyRule.getKeyDefValue());   //V1-false

            Map<String,String> keyVarNameMap = keyRule.getKeyVarName(); //变量名对照表  李孵孵-V1
            Set<String> keyVarNames = keyVarNameMap.keySet(); //变量名集合
            for (String keyVar:keyVarNames){  //遍历表达式变量名(关键字)
                if (hitKeyMap.containsKey(keyVar)) //如果命中的关键字中在表达式的关键字
                    keyValCopy.put(keyVarNameMap.get(keyVar),true);   //取出关键字对应的变量名作为key入库
            }

            try {
                boolean match = (Boolean)felEngine.eval(keyRule.getKeyExpReplace(),new MapContext(keyValCopy));
                if (match)  {//符合规则加入集合
                    if(!keyRule.getKeyExp().toLowerCase().equals("true")){
                        LOG.info("命中关键词规则["+ruleid+"]:" + keyRule.getKeyExp());
                    }
                    matchKeyRules.put(ruleid,keyRule);
                }
            } catch (Exception e) {
                LOG.error("表达式不符合规范：" + keyRule.getKeyExp());
            }
        }
        return matchKeyRules;
    }


    public void getHitOtherRulesCommon(Map<String, Boolean> hitOtherMap, Map<String, PreFilterRules> hitOtherRules, String rulesName) {
        if (!hitOtherMap.containsKey(rulesName)){
            hitOtherMap.put(rulesName,true);
            if (CommonCacheLoader.otherRulesMap.containsKey(rulesName)){
                Map<String,List<PreFilterRules>> keyRulesMap = CommonCacheLoader.otherRulesMap.get(rulesName);
                for (String keyExp:keyRulesMap.keySet()){
                    String ruleid ="";
                    for (PreFilterRules keyRule:keyRulesMap.get(keyExp)){
                        ruleid = keyRule.getId();
                        hitOtherRules.put(ruleid, keyRule);
                    }
                }
            }
        }
    }

    public Map<String,PreFilterRules> getHitOtherRules(Map<String, String> data,Map<String,Boolean> hitOtherMap) {
        Map<String,PreFilterRules> hitOtherRules = new HashMap<String ,PreFilterRules>(); //命中规则集合
        for(Map.Entry<String,Map<String,List<PreFilterRules>>> entry: CommonCacheLoader.otherRulesMap.entrySet()){
            String rulesName = entry.getKey();
            if(rulesName.toLowerCase().equals("true")){
                getHitOtherRulesCommon(hitOtherMap, hitOtherRules, rulesName);
            }

            if (rulesName.contains(":")) {
                String key = rulesName.substring(0, rulesName.indexOf(":"));
                //数据库规则的value
                String rules_value = rulesName.substring(rulesName.indexOf(":") + 1, rulesName.length());
                //数据的value
                String value = data.get(key);
                if (null != value && value.equals(rules_value)) {
                    getHitOtherRulesCommon(hitOtherMap, hitOtherRules, rulesName);
                }
            }

        }
        return hitOtherRules;
    }

    public Map<String,PreFilterRules> matchOtherRules(FelEngine felEngine, Map<String, Boolean> hitOtherMap, Map<String, PreFilterRules> hitOtherRules) {
        Map<String,PreFilterRules> matchOtherRules = new HashMap<String,PreFilterRules>(); //符合规则的集合
        //遍历命中的表达式集合
        for (Map.Entry<String,PreFilterRules> entry : hitOtherRules.entrySet()) {
            PreFilterRules rule = entry.getValue();
            String ruleid =entry.getKey();
            //取出默认参数值,需要深度拷贝
            Map<String,Object> valCopy = new HashMap<>(rule.getDefValue());   //V1-false

            Map<String,String> varNameMap = rule.getVarName(); //变量名对照表  李孵孵-V1
            Set<String> varNames = varNameMap.keySet(); //变量名集合
            for (String var:varNames){  //遍历表达式变量名(关键字)
                if (hitOtherMap.containsKey(var)) //如果命中的关键字中在表达式的关键字
                    valCopy.put(varNameMap.get(var),true);   //取出关键字对应的变量名作为key入库
            }

            //logger.info(keyRule.getExp() + "----------------------" + keyRule.getKeywordExp());
            try {
                boolean match = (Boolean)felEngine.eval(rule.getExpReplace(),new MapContext(valCopy));
                if (match)  {//符合规则加入集合
                    if(!rule.getExp().toLowerCase().equals("true")){
                        LOG.info("命中其它规则[" + ruleid + "]:" + rule.getExp());
                    }
                    matchOtherRules.put(ruleid,rule);
                }
            } catch (Exception e) {
                LOG.error("表达式不符合规范："+rule.getExp());
            }
        }
        return matchOtherRules;
    }
}
