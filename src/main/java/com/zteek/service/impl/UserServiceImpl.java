package com.zteek.service.impl;

import com.zteek.entity.User;
import com.zteek.exception.BusinessException;
import com.zteek.mapper.UserMapper;
import com.zteek.service.UserService;
import com.zteek.utils.MD5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper mapper;

    @Override
    public User findUserByUsername(String username) {
        return null;
    }

    @Override
    public User login(String username, String password) throws BusinessException {
        User user = mapper.findUserByUsername(username);
        if(null == user){
            throw new BusinessException("账号不存在");
        }else {
            //验证 密码
            String s = MD5.MD5(password);
            if(!user.getPassword().equalsIgnoreCase(s)){
                throw new BusinessException("用户名或密码错误");
            }
            //验证 状态
            if(0 == user.getStatus()){
                throw new BusinessException("账号失效");
            }
            return user;
        }
    }
}
