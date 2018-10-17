package com.zteek.service;

import com.zteek.entity.Account;

public interface AccountService {
    Account getAccount(String imei);

    int registResult(Account account);
}
