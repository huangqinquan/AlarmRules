package com.meiya.alarm.util;

import org.joda.time.DateTime;

import java.io.File;

/**
 * Created by Administrator on 2017/12/22.
 */
public class NameUtil {
    public static String base = "Base";
    public static String areacode = "350200";
//    public static String currentTimeStr = new DateTime();
    public static DateTime joda = new DateTime();
//    public static String timeStamp = System.currentTimeMillis() + "";
    public static String suffix = ".json";
    public static String seprator = "_";
    public static final String timeFormat = "yyyyMMddHHmmss";

    /**
     * 返回文件命名
     * @return
     */
    public static String name(){
        String currentTimeStr = joda.toString(timeFormat);
        String timeStamp = System.currentTimeMillis() + "";
        String name = base + seprator
                + areacode + seprator
                + timeStamp + seprator
                + currentTimeStr
                + suffix;
        return name;
    }

    /**
     * 带上文件路径 返回文件全路径
     * @param path
     * @return
     */
    public static String name(String path){
        String currentHourStr = joda.toString("yyyy") + File.separator + joda.toString("MM") + File.separator + joda.toString("dd");
        if (path.endsWith(File.separator)){
            return path + currentHourStr + File.separator +  name();
        }else {
            return path + File.separator + currentHourStr + File.separator + name();
        }
    }

    /**
     * 创建ftp目录
     * @param path
     * @return
     */
    public static String nameFtp(String path){
        String currentHourStr = joda.toString("yyyy") + File.separator + joda.toString("MM") + File.separator + joda.toString("dd");
        if (path.endsWith(File.separator)){
            return path + currentHourStr;
        }else {
            return path + File.separator + currentHourStr;
        }
    }
}


