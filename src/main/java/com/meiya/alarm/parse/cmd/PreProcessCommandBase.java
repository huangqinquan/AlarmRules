package com.meiya.alarm.parse.cmd;


import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 实际执行预处理操作的基类
 * 
 * @author xiegh
 * 
 */
public abstract class PreProcessCommandBase implements Command {
	/* log tool */
	protected Logger log = LoggerFactory.getLogger(getClass());
	
	private long totalNum = 0;//总执行次数
	private long totalTime = 0;//总执行时间

	/**
	 * 通过chain模式执行预处理操作
	 * 
	 * @param ctx
	 */
	public boolean execute(Context ctx) throws Exception {
		long begin = System.currentTimeMillis();
		try {// 此处强制捕获所有预处理过程中出现的异常信息，避免一个环节出现异常而所有流程结束
			if (needProcess(ctx)) {// 需要执行此项预处理操作
				return executePreprocess(ctx);
			}
		} catch (Throwable e) {// 此处捕获Exception及可能出现的各种Error，避免因为一个Command失败而影响所有其他Command的执行.
			log.error("--->执行本项预处理出现异常", e);
		} finally {// 计算单项预处理任务消耗时间
			if(log.isInfoEnabled()) {
				long cost = System.currentTimeMillis() - begin;
				totalNum ++;
				totalTime += cost;
				// 统计每个 cmd 累计执行次数、累积执行时间、平均执行时间
				if(totalNum % 100 == 0) {
					log.info("totalCmds\t" + totalNum + "\ttotalTimes\t" + totalTime + "\tavgTimes\t" + (float) totalTime / totalNum + "\tcost\t" + cost);
				}
			}
		}
		return false;// 返回false以便下一个Command继续以chain形式执行
	}

	/**
	 * 判断是否要执行此项预处理操作
	 * 
	 * @param ctx
	 * @return
	 */
	protected abstract boolean needProcess(Context ctx);

	/**
	 * 执行预处理操作
	 * 
	 * @param ctx
	 */
	protected abstract boolean executePreprocess(Context ctx);


}
