package com.zteek.service.impl;

import com.zteek.entity.Account;
import com.zteek.entity.PhoneLog;
import com.zteek.mapper.AccountMapper;
import com.zteek.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountMapper accountMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Account getAccount(String imei,String country) {
        //取一个账号
        Account account = accountMapper.getAccountByImei(country);
        if(null == account.getId()){
            //没有取到，再获取一次
            account = accountMapper.getAccountByImei(country);
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

    @Override
    public List<Account> getCountList(Map<String, Object> param) {
        return accountMapper.getCountList(param);
    }

    @Override
    public void openUS() {
        accountMapper.openUS();
    }

    @Override
    public void closeUS() {
        accountMapper.closeUS();
    }

    @Override
    public void openJP() {
        accountMapper.openJP();
    }

    @Override
    public void closeJP() {
        accountMapper.closeJP();
    }


}
