package com.zteek.mapper;

import com.zteek.entity.Account;
import com.zteek.entity.PhoneLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface AccountMapper {

    Account getAccountByImei(String country);

    int updateImeiById(@Param("id") Long id,@Param("imei") String imei);

    int updateResultById(Account account);


    List<Account> getCountList(Map<String, Object> param);

    void openUS();

    void closeUS();

    void openJP();

    void closeJP();
}
