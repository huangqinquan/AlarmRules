package com.meiya.alarm.test;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by huangqq on 2017/12/25.
 */
public class TestRedis extends BaseJunit4Test{

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testRedis(){
        List<Long> list = new ArrayList<>();
        list.add(1l);
        list.add(2l);
        list.add(3l);

        list.parallelStream().forEach(num -> {
            long n = redisTemplate.opsForValue().increment("Alarm:num", num);
            System.out.println("n=" +n);
        });
//        Integer v = (Integer) ;
        System.out.println(redisTemplate.opsForValue().get("Alarm:num"));
//        redisTemplate.delete("num");
    }

    @Test
    public void keys(){
        Set keyset = redisTemplate.keys("Alarm:*");
        System.out.println(keyset.size());
        keyset.forEach(key -> {
            System.out.println(key);
        });
    }
}
