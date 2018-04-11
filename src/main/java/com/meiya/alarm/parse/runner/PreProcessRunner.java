package com.meiya.alarm.parse.runner;

import com.meiya.alarm.parse.core.ChainsCatalogLoader;
import com.meiya.alarm.pojo.AlarmData;
import com.meiya.alarm.pojo.AlarmKey;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ContextBase;
import org.apache.log4j.Logger;

/**
 * Created by Administrator on 2017/12/20.
 */
public class PreProcessRunner implements Runnable{

    private Logger LOG = Logger.getLogger(PreProcessRunner.class);

    private AlarmData alarmData;

    private final String chainName = "alarm";

    public PreProcessRunner(AlarmData alarmData){
        this.alarmData = alarmData;
    }

    @Override
    public void run() {

        //这里加一个对alarmData的判空处理
        try {
            Context context = new ContextBase();
            context.put(AlarmKey.alarm_data, alarmData.getDataMap());

            ChainsCatalogLoader.getChains(chainName).execute(context);
        }catch (Exception e){
            LOG.error("" ,e);
        }
    }
}
