package com.meiya.alarm.parse.cmd;

import com.meiya.alarm.pojo.AlarmData;
import com.meiya.alarm.pojo.AlarmKey;
import org.apache.commons.chain.Context;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;


/**
 * 用于同步操作源Map的同步处理指令
 * 
 * @author xiegh
 * 
 */
public abstract class SynchonizedPreprocessCommand extends
		PreProcessCommandBase {

	/**
	 * 判断是否要执行此项预处理操作-此处仅简单的判断原始数据Map是否含有数据..
	 * 
	 * @param ctx
	 * @return 原始数据中含有数据则返回true
	 */
	@SuppressWarnings("rawtypes")
	protected boolean needProcess(Context ctx) {
		Map source = ((AlarmData)ctx.get(AlarmKey.alarm_data)).getDataMap();
		return MapUtils.isNotEmpty(source);
	}

	/**
	 * 执行预处理操作
	 * 
	 * @param ctx
	 * @return true-不往下继续执行其它chain；false继续执行chain-config.xml配置的其他在它之后的chain
	 */
	protected boolean executePreprocess(Context ctx) {
//		synchronized (source) {// 在执行该预处理流程过程中，全程同步源数据信息
			return syncExecute(ctx);
//		}
	}

	/**
	 * 同步执行该项预处理操作.
	 * 
	 * @param ctx
	 * @return true-不往下执行chain中其它Command；false-执行chain中其他Command
	 */
	protected abstract boolean syncExecute(Context ctx);
	


}