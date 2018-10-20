package com.zteek.service;

import com.zteek.entity.Account;
import com.zteek.entity.PhoneLog;

public interface AccountService {
    Account getAccount(String imei);

    int registResult(Account account);


}
