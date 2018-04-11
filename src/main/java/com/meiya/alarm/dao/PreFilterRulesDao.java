package com.meiya.alarm.dao;

import com.meiya.alarm.pojo.PreFilterRules;
import org.apache.ibatis.annotations.Param;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator on 2017/12/20.
 */

@Repository
public interface PreFilterRulesDao {

    /**
     * 查询所有enable=1的规则
     * @return
     */
    public List<PreFilterRules> getRulesList();


}
