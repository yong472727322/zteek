package com.zteek.service;

import com.zteek.entity.AmazonTaskRun;
import com.zteek.entity.User;
import com.zteek.entity.VmCount;
import com.zteek.exception.BusinessException;

import java.util.List;
import java.util.Map;

public interface VmCountService {
    /**
     * 查询各虚拟机执行情况列表
     */
    List<VmCount> getVmList(Map<String,Object> param);

    int insertVm(VmCount vmCount);

    void updateVmCount(String asin, String userName);

    void updateVmStatus(Integer vmStatus, String userName,String message,String keyword);

    void updateStatus(String userName);

    VmCount findVmForId(Long id);

    void vmCountUpdate(VmCount vmCount);

    void restart(Long id);

    void updateWar(Long id);

    VmCount getCountryAndTaskName(String userName);

    void updateRestartStatus(String userName);

    void updateWarStatus(String userName);
}
