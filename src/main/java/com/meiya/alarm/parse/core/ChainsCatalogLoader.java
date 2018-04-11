package com.meiya.alarm.parse.core;


import com.meiya.alarm.util.Utilities;
import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.CatalogFactory;
import org.apache.commons.chain.Chain;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.config.ConfigParser;
import org.apache.commons.chain.generic.LookupCommand;
import org.apache.commons.chain.impl.CatalogFactoryBase;
import org.apache.commons.chain.impl.ChainBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 加载配置在classpath下的chain的xml配置信息。在使用xml配置时有用到
 * 
 * @author xiegh
 * 
 */
public final class ChainsCatalogLoader {
	/** 配置各种职责链的文件信息 */
	private final static String confFile = "chain-config.xml";

	/** log tools */
	private final static Logger log = LoggerFactory
			.getLogger(ChainsCatalogLoader.class);

	/** 配置的chain信息 */
	private static Catalog chains = null;

	/** 每个Chain中含有的Class集合映射Map */
	private static Map<String, List<Class<?>>> chainCommands = new HashMap<String, List<Class<?>>>(
			10);

	/**
	 * 使用xml内的chain名获取某个chain用于执行
	 * 
	 * @param chainname
	 * @return
	 */
	public static Command getChains(String chainname) {
			if (null == chains) {
			synchronized (ChainsCatalogLoader.class) {
					try {
						new ConfigParser().parse(getConfigFile(confFile));
						chains = CatalogFactoryBase.getInstance().getCatalog();
					} catch (Exception e) {
						log.error("---?从配置文件{}加载配置的chain信息失败..", confFile, e);
						throw new RuntimeException("加载配置的chain信息失败", e);
					}
				}
			}

		return chains.getCommand(chainname);
	}

	/**
	 * 从classpath加载特定的配置信息
	 * 
	 * @param fileName
	 */
	private static URL getConfigFile(String fileName) {
		try {// 返回配置文件对应的URL信息
			return Utilities.getClasspathFile(fileName).toURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 通过反射初始化职责连信息
	 * 
	 * @param chainName
	 * @return
	 */
	public static Chain getChainByName(String chainName) {
		if (!chainCommands.containsKey(chainName)) {
			// 读取chain配置信息
			CatalogFactory fac = CatalogFactoryBase.getInstance();
			Catalog cata = fac.getCatalog();

			List<Class<?>> clzs = findCommands(chainName, cata);
			chainCommands.put(chainName, clzs);
		}

		// 初始化职责链信息
		ChainBase newChain = new ChainBase();
		List<Class<?>> clzs = chainCommands.get(chainName);
		if (clzs != null && !clzs.isEmpty()) {
			for (Class<?> clz : clzs) {
				Command cmd = try2initializeCommand(clz);
				newChain.addCommand(cmd);
			}
		}

		// log.debug("--->返回职责链{}成功完成..", newChain.getClass());
		return newChain;
	}

	/**
	 * 尝试初始化一个可供执行的职责类
	 * 
	 * @param clz
	 */
	private static Command try2initializeCommand(Class<?> clz) {
		try {
			Command cmd = (Command) clz.newInstance();
			return cmd;
		} catch (Exception e) {
			log.error("--->尝试初始化Chain-Command对象失败", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 反射加载该chain下的所有command信息
	 * 
	 * @param chainName
	 * @param cata
	 * @return
	 */
	private static Command[] loadChainConfigsCommands(String chainName,
			Catalog cata) {
		try {
			ChainBase chain = (ChainBase) cata.getCommand(chainName);
			Field f = ChainBase.class.getDeclaredField("commands");
			f.setAccessible(true);
			return (Command[]) f.get(chain);
		} catch (Exception e) {
			log.error("--->尝试从配置文件中加载chain {}对应的类信息失败...", chainName, e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 加载该chain对应的所有Command类信息
	 * @param cata
	 * @return
	 */
	private static List<Class<?>> findCommands(String chainName, Catalog cata) {
		// 获取到该chain配置的所有Command类信息
		Command[] cmds = loadChainConfigsCommands(chainName, cata);
		if (cmds == null || cmds.length <= 0) {// chain长度为0
			return null;
		}

		List<Class<?>> res = new java.util.ArrayList<Class<?>>(15);
		for (Command cmd : cmds) {// 遍历整个chain
			Class<?> clz = cmd.getClass();// 获取chain中的command的类名信息
			if (clz.isAssignableFrom(LookupCommand.class)) {// LookupCommand是Chains中用于获取下级chain中的command
				LookupCommand lc = (LookupCommand) cmd;
				// System.out.println("--->Lookup : " + lc.getName());
				List<Class<?>> list = findCommands(lc.getName(), cata);
				if (null != list && !list.isEmpty()) {
					res.addAll(list);
				}
			} else {// 具体的Command子类
				res.add(clz);
			}
		}

		return res;
	}
}
