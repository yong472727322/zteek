package com.zteek.service;

import com.zteek.entity.User;
import com.zteek.exception.BusinessException;

public interface UserService {
    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    User findUserByUsername(String username);

    /**
     * 用户登陆
     * @param username
     * @param password
     * @return 成功返回用户，失败抛异常
     */
    User login(String username, String password) throws BusinessException;
}
