package com.zteek.service;

import com.zteek.entity.Account;

public interface AccountService {
    Account getAccount(String imei);

    int registResult(Account account);

    /**
     * 保存手机日志信息
     * @param imei
     * @param message
     * @return
     */
    int recordLog(String imei, String message);
}
