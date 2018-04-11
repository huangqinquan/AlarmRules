package com.meiya.alarm.bean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by huangqq on 2017/12/25.
 */
@Configuration
public class JavaConfig {

    @Bean(name = "gson")
    public Gson getGson(){
        return new GsonBuilder().disableHtmlEscaping().create();
    }
}
