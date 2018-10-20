package com.zteek.mapper;

import com.zteek.entity.Account;
import com.zteek.entity.PhoneLog;
import org.apache.ibatis.annotations.Param;

public interface AccountMapper {

    Account getAccountByImei();

    int updateImeiById(@Param("id") Long id,@Param("imei") String imei);

    int updateResultById(Account account);


}
