package com.meiya.alarm.dao;

import com.meiya.alarm.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator on 2017/12/22.
 */
@Repository
public interface UserDao {

    public List<User> getUserList();

    public Long updateExportNum(@Param("userId")Integer userId, @Param("exportNum")Long exportNum);
}
