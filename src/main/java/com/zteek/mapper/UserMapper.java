package com.zteek.mapper;

import com.zteek.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    /**
     * 根据用户名查找用户
     * @param username
     * @return
     */
    User findUserByUsername(@Param("username") String username);
}
