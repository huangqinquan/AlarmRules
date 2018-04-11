package com.meiya.alarm.cache;

import com.greenpineyu.fel.FelEngine;
import com.greenpineyu.fel.FelEngineImpl;
import com.greenpineyu.fel.context.FelContext;
import com.meiya.alarm.pojo.PreFilterRules;
import org.ahocorasick.trie.Trie;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**   
 * @author duansx
 * @version 1.0   
 * @date 2016年5月18日
 * @description
 */

@Component
public class FilterRulesSvc {
	private static final Logger logger = Logger.getLogger(FilterRulesSvc.class);

	@Value("${alarm.parse.maxExportCount}")
	private long maxExportCount;

	private static FilterRulesSvc instance = null; //持有自身,单例模式
	
	/**
	 * 私有构造函数,单例模式
	 */
	private FilterRulesSvc(){
	}

	/**
	 * 获取实例
	 * @return 自身
	 */
	public static synchronized FilterRulesSvc getInstance(){
		if (instance==null)
			instance = new FilterRulesSvc(); //初始化本身
		return instance;
	}

	/**
	 * 构造AhoCorasick查询器
	 * @param keywords 用于构造AhoCorasick查询器的关键词
	 * @return 返回AhoCorasick查询器
	 */
	public Trie constructKeywordFinder(Set<String> keywords){

		long begin = System.currentTimeMillis();

		Trie.TrieBuilder builder = Trie.builder();
		keywords.forEach(keyword -> {
			builder.addKeyword(keyword);
		});
		Trie acTree = builder.build();

		long end = System.currentTimeMillis();

		logger.info("AhoCorasick查询器构造完成,耗时:" + (end - begin));

		return acTree;
	}
	
	
	
	/**
	 * 读入关键词map
	 * 存放关键字及其该关键字对应的所有规则信息
	 *  Map<String,Map<...>>,key为关键字如:郭奉奉,value为该关键字对应的所有的规则map
	 *  Map<String,List<...>>,key为关键字表达式,如:(郭奉奉 or 杨奉奉) not 陈奉奉
	 *                             value为该表达式对应的规则集合
	 */
	public Map<String,Map<String,List<PreFilterRules>>> loadKeywordMap(List<PreFilterRules> rulesExp){
		
		long begin = System.currentTimeMillis();
		
		Map<String,Map<String,List<PreFilterRules>>> keywordMap = new HashMap<>();
		
		int totalRecord = 0;  //所有记录
		int validRecord = 0;  //正确记录
		
		//构造表达式引擎
		FelEngine fel= new FelEngineImpl();
		FelContext ctx = fel.getContext();
		ctx.set("V", false);
		
		for (PreFilterRules r:rulesExp){  //遍历规则关键词列表

		
			//过滤不合法的规则
			if (!rulesFilter(r)) {
				logger.info("规则" + r.getId() + "不合法 或者已超过最大布控数据量" + CommonCacheLoader.maxExportCount + " 忽略");
				continue;
			}
			
			totalRecord++;
			
			String keyExp = r.getKeyExp(); //取出关键词表达式,形如:"(郭奉奉||杨奉奉)&&(!陈奉奉)"
			//如果规则为空,则继续下一条
			if (keyExp==null||keyExp.equals(""))
				continue;
			
			//替换掉所有的"(",")"
			//这里注意 一定要用 && || ! 来表示与或非，不能使用 AND OR NOT！
			String[] keyExpSplits = keyExp.replace("(", "").replace(")", "").split("&&|\\|\\||!");
			
			String verifyExp = keyExp;  //用于验证的表达式
			for (String key:keyExpSplits){ //将关键字全部替换为false
				if (key.trim().equals(""))
					continue;
				verifyExp = verifyExp.replace(key,"false");
			}
			
			//对表达式进行验证
			try{
				fel.eval(verifyExp,ctx);
			}catch (Exception e){
				logger.error("表达式不正确，请检查规则是否包含&& || !以外的符号,该数据跳过:" +r.getKeyExp());
				continue; //继续下一条记录
			}
			
			int prefixCount = 0;   //变量前缀累加器
			
			for (String key:keyExpSplits){  //对分割出的每一个关键词
				key = key.trim();
				
				if (key.equals(""))  //关键字为空串
					continue;
				
				prefixCount++;
				String varName = "V" + prefixCount; //变量名
				
				r.getKeyVarName().put(key,varName); //压入变量映射
				r.getKeyDefValue().put(varName,false); //默认均为false
				
				//判断集合是否包含该关键词
				if (keywordMap.containsKey(key)){
					Map<String,List<PreFilterRules>> expMap = keywordMap.get(key); //取出表达式map
					if (expMap.containsKey(keyExp)){ //包含表达式
						List<PreFilterRules> ruleList = expMap.get(keyExp); //取出规则列表
						
						boolean match = false; //ruleid排重使用
						for (PreFilterRules rr:ruleList){  //对集合中已经存在的ruleId进行匹配
							if (rr.getId()==r.getId()){
								match = true; //找到一个相同的
								break;
							}
						}
						
						if (!match)
							expMap.get(keyExp).add(r); //加入规则
					}else{  //不包含
						List<PreFilterRules> ruleKeywords = new ArrayList<>(); //重新构造一个集合
						ruleKeywords.add(r); //把规则加入集合
						expMap.put(keyExp,ruleKeywords); // 以关键字表达式为key,value为该表达式对应的所有规则对象
					}
				}else{ //不包含
					List<PreFilterRules> ruleKeywords = new ArrayList<>(); //重新构造一个集合
					ruleKeywords.add(r); //把规则加入集合
					Map<String,List<PreFilterRules>> expMap = new HashMap<>(); //构造表达式map
					expMap.put(keyExp,ruleKeywords); //以关键字表达式为key,value为该表达式对应的所有规则对象
					keywordMap.put(key,expMap);
				}
			}
			
			Set<String> varNames = r.getKeyVarName().keySet();
			for (String keyword:varNames){  //替换表达式
				try {
					keyExp = keyExp.replace(keyword,r.getKeyVarName().get(keyword));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			r.setKeyExpReplace(keyExp);
			
			validRecord++; //有效记录
		
		} //end of  for (RuleKeyword r:records)
		
		long end = System.currentTimeMillis();
		
		logger.info("共读取规则:" + totalRecord + ",其中有效规则(表达式正确):" +
				validRecord + ",构造keywordMap耗时:" + (end - begin));
		
		return keywordMap;
	}
	
	/**
	 * 读入关键词map
	 * 存放关键字及其该关键字对应的所有规则信息
	 *  Map<String,Map<...>>,key为关键字如:郭奉奉,value为该关键字对应的所有的规则map
	 *  Map<String,List<...>>,key为关键字表达式,如:(郭奉奉 or 杨奉奉) not 陈奉奉
	 *                             value为该表达式对应的规则集合
	 */
	public Map<String,Map<String,List<PreFilterRules>>> loadOtherRulesMap(List<PreFilterRules> rulesExp){

		long begin = System.currentTimeMillis();
		

		Map<String,Map<String,List<PreFilterRules>>> keywordMap = new HashMap<>();

		int totalRecord = 0;  //所有记录
		int validRecord = 0;  //正确记录

		//构造表达式引擎
		FelEngine fel= new FelEngineImpl();
		FelContext ctx = fel.getContext();
		ctx.set("V", false);

		for (PreFilterRules r:rulesExp){  //遍历规则关键词列表
			
			//过滤不合法的规则
			if (!rulesFilter(r)) {
				logger.info("规则" + r.getId() + "不合法 或者已超过最大布控数据量" + CommonCacheLoader.maxExportCount + " 忽略");
				continue;
			}
		
			totalRecord++;

			String keyExp = r.getExp(); //取出关键词表达式,形如:"(郭奉奉||杨奉奉)&&(!陈奉奉)"
			//如果规则为空,则继续下一条
			if (keyExp==null||keyExp.equals(""))
				continue;

			//替换掉所有的"(",")",并按照and,or,not进行分割
			String[] keyExpSplits = keyExp.replace("(", "").replace(")", "").split("&&|\\|\\||!");

			String verifyExp = keyExp;  //用于验证的表达式
			for (String key:keyExpSplits){ //将关键字全部替换为false
				if (key.trim().equals(""))
					continue;
				verifyExp = verifyExp.replace(key,"false");
			}

			//对表达式进行验证
			try{
				fel.eval(verifyExp,ctx);
			}catch (Exception e){
				logger.error("表达式不正确,该数据跳过:" +r);
				continue; //继续下一条记录
			}

			int prefixCount = 0;   //变量前缀累加器

			for (String key:keyExpSplits){  //对分割出的每一个关键词
				key = key.trim();

				if (key.equals(""))  //关键字为空串
					continue;

				prefixCount++;
				String varName = "V" + prefixCount; //变量名

				r.getVarName().put(key,varName); //压入变量映射
//				System.out.println("key:"+key+",varName:"+varName+".");
				r.getDefValue().put(varName,false); //默认均为false

				//判断集合是否包含该关键词
				if (keywordMap.containsKey(key)){
					Map<String,List<PreFilterRules>> expMap = keywordMap.get(key); //取出表达式map
					if (expMap.containsKey(keyExp)){ //包含表达式
						List<PreFilterRules> ruleList = expMap.get(keyExp); //取出规则列表

						boolean match = false; //ruleid排重使用
						for (PreFilterRules rr:ruleList){  //对集合中已经存在的ruleId进行匹配
							if (rr.getId()==r.getId()){
								match = true; //找到一个相同的
								break;
							}
						}

						if (!match)
							expMap.get(keyExp).add(r); //加入规则
					}else{  //不包含
						List<PreFilterRules> ruleKeywords = new ArrayList<>(); //重新构造一个集合
						ruleKeywords.add(r); //把规则加入集合
						expMap.put(keyExp,ruleKeywords); // 以关键字表达式为key,value为该表达式对应的所有规则对象
					}
				}else{ //不包含
					List<PreFilterRules> ruleKeywords = new ArrayList<>(); //重新构造一个集合
					ruleKeywords.add(r); //把规则加入集合
					Map<String,List<PreFilterRules>> expMap = new HashMap<>(); //构造表达式map
					expMap.put(keyExp,ruleKeywords); //以关键字表达式为key,value为该表达式对应的所有规则对象
					keywordMap.put(key,expMap);
				}
			}

			Set<String> varNames = r.getVarName().keySet();
			for (String keyword:varNames){  //替换表达式
				try {
					String replacement = r.getVarName().get(keyword);
					keyExp = keyExp.replace(keyword,replacement);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			r.setExpReplace(keyExp);

			validRecord++; //有效记录

		} //end of  for (RuleKeyword r:records)

		long end = System.currentTimeMillis();

		logger.info("共读取规则:" + totalRecord + ",其中有效规则(表达式正确):" +
				validRecord + ",构造keywordMap耗时:" + (end - begin));

		return keywordMap;
	}
	
	/**
	 * 过滤两个都是TRUE的规则
	 */
	public static boolean rulesFilter(PreFilterRules rule) {
		boolean right = true;
		//System.out.println(rule.getExportCount());
		if (rule.getExp().equals("TRUE") && rule.getKeyExp().equals("TRUE")) {
			return false;
		}
		if (rule.getExportCount() > CommonCacheLoader.maxExportCount) {
			return false;
		}
		return right;
	}
	
//	public static void main(String[] args) {
//		String str="PUBLISHTIME:[20160517000000 TO 20160518000000]||((V1&&V2)||(V3))&&PUBLISHTIME:[20160517000000 TO 20160518000000]||PUBLISHTIME:[20160517000000 TO 20160518000000]";
//		String src="PUBLISHTIME:[20160517000000 TO 20160518000000]";
//		String replace = "V4";
//		System.out.println(str.replace(src, replace));
//	}


}
