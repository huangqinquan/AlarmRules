package com.meiya.alarm.util;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.net.URL;

/**
 * 一些无法区分该放置在哪里的工具类
 * 
 * @author xiegh
 * 
 */
public class Utilities {
	/**
	 * 将时间串转化成solr存储的时间格式，否则存入solr的时间将少8小时
	 * 
	 * @param time
	 * @return
	 */
	public static String convertToSolrDate(String time) {
		if (time != null && !"".equals(time)) {
			time = time.replace(" ", "T") + "Z";
		}
		return time;
	}

	/**
	 * 字符串转字节
	 * 
	 * @param str
	 * @return
	 */
	public static byte[] StringToByte(String str) {
		if (str != null && !"".equals(str) && str != "null") {
			return str.getBytes();
		} else {
			return null;
		}
	}

	/**
	 * 取得classpath下某个资源文件
	 * 
	 * @param fileName
	 *            获取某个资源文件信息
	 * @return
	 */
	public static URL getClasspathResource(String fileName) {
		// 取得classpath的路径地址
		String pathFile = fileName;
		if (!StringUtils.startsWith(fileName, "/")) {
			pathFile = "/" + fileName;
		}

		URL url = Utilities.class.getResource(pathFile);
		return url;
	}

	/**
	 * 取得classpath下某个资源文件。可以取得jar内的资源
	 * 
	 * @param fname
	 *            获取某个资源文件信息
	 * @return
	 */
	public static File getClasspathFile(String fname) {
		// 取得classpath的路径地址
		PropertiesConfiguration pc = getClasspathProps(fname);

		return pc.getFile();
	}

	/**
	 * 取得classpath下的某个properties文件
	 * 
	 * @param fname
	 * @return
	 */
	public static PropertiesConfiguration getClasspathProps(String fname) {
		// 取得classpath的路径地址
		PropertiesConfiguration pc = null;
			try {
				pc = new PropertiesConfiguration(fname);
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return pc;

	}

	/**
	 * 判断所给的字符串是否与数组中的任意一个字符串相同
	 * 
	 * @param source
	 * @param compares
	 * @return
	 */
	public static boolean equalsAnyone(String[] source, String compares) {
		if (source == null || source.length <= 0 || compares == null) {
			return false;
		}

		for (String str : source) {
			if (StringUtils.equals(str, compares)) {
				return true;
			}
		}

		return false;
	}


	/**
	 * 判断是否所有的字符串内容都为空...
	 * 
	 * @param strs
	 * @return 所有字符串内容都为null或kong时返回true，否则false
	 */
	public static boolean allStringBlank(String... strs) {
		if (null == strs || 0 >= strs.length) {// 字符串数组为空或null，则认为所有内容都为空
			return true;
		}

		for (String str : strs) {
			if (StringUtils.isNotBlank(str)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 判断是否有字符串内容为空
	 * 
	 * @param strs
	 * @return 数组中任意字符串为null或空时返回true，否则false
	 */
	public static boolean anyBlank(String... strs) {
		if (null == strs || 0 >= strs.length) {// 字符串数组为空或null，则认为所有内容都为空
			return true;
		}

		for (String str : strs) {
			if (StringUtils.isBlank(str)) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * 去除字符串首尾空格
	 * @param s
	 * @return
	 */
	public static String stringTrim(String s){
		String result = null;
		if(null!=s){
			result = s.replaceAll("^[\\s\\p{Zs}]+", "").replaceAll("[\\s\\p{Zs}]+$", "").replaceAll("", "");
		}
		return result;
	}


}
