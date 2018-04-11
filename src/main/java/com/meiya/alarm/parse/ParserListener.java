package com.meiya.alarm.parse;

import com.google.gson.Gson;
import com.meiya.alarm.parse.core.ChainsCatalogLoader;
import com.meiya.alarm.pojo.AlarmData;
import com.meiya.alarm.pojo.AlarmKey;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ContextBase;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashMap;

/**
 * Created by Administrator on 2017/12/20.
 */
@Component
public class ParserListener implements MessageListener{

    private Logger LOG = Logger.getLogger(ParserListener.class);

    @Autowired
    private Gson gson;

    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage){
            String json = null;
            try {
                json = ((TextMessage)message).getText();
                if (StringUtils.isBlank(json)) {
                    return;
                }
                AlarmData alarmData = gson.fromJson(json, AlarmData.class);

                //这里使用线程池去执行任务链
                Context context = new ContextBase();
                context.put(AlarmKey.alarm_data, alarmData);
                ChainsCatalogLoader.getChains(AlarmKey.alarm_chain).execute(context);

            } catch (JMSException e) {
                LOG.error("JMS接收消息出错", e);
            } catch (Exception e) {
                LOG.error("", e);
            }

        }
    }
}
