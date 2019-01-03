package com.zteek.mapper;

import com.zteek.entity.User;
import com.zteek.entity.VmCount;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface VmCountMapper {

    /**
     * 各虚拟机执行情况
     * @return
     */
    List<VmCount> getVmList(Map<String, Object> param);

    int insertVm(VmCount vmCount);

    void updateBeforeCount();

    void updateVmCount(@Param("asin") String asin, @Param("userName") String userName);

    void updateCountToNull();

    void updateVmStatus(@Param("vmStatus")Integer vmStatus, @Param("userName")String userName,@Param("message")String message,@Param("keyword")String keyword);

    void updateStatus(String userName);

    VmCount findVmForId(Long id);

    void vmCountUpdate(VmCount vmCount);

    void restart(Long id);

    void updateWar(Long id);

    VmCount getCountryAndTaskName(String userName);

    void updateRestartStatus(String userName);

    void updateWarStatus(String userName);
}
