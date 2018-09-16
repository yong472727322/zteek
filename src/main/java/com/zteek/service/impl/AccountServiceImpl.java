package com.zteek.service.impl;

import com.zteek.entity.Account;
import com.zteek.mapper.AccountMapper;
import com.zteek.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountMapper accountMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Account getAccount(String imei) {
        //取一个账号
        Account account = accountMapper.getAccountByImei();
        if(null == account.getId()){
            //没有取到，再获取一次
            account = accountMapper.getAccountByImei();
        }
        //记录使用手机，更新账号状态
        accountMapper.updateImeiById(account.getId(),imei);

        return account;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int registResult(Account account) {
        accountMapper.updateResultById(account);
        return 1;
    }
}
