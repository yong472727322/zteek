package com.zteek.service;

import com.zteek.entity.Account;
import com.zteek.entity.PhoneLog;

import java.util.List;
import java.util.Map;

public interface AccountService {
    Account getAccount(String imei,String country);

    int registResult(Account account);


    List<Account> getCountList(Map<String,Object> param);

    void openUS();

    void closeUS();

    void openJP();

    void closeJP();

}
