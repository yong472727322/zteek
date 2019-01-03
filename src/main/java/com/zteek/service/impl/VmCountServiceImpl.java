package com.zteek.service.impl;

import com.zteek.entity.User;
import com.zteek.entity.VmCount;
import com.zteek.mapper.VmCountMapper;
import com.zteek.service.VmCountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
@Service
public class VmCountServiceImpl implements VmCountService {
    @Autowired
    private VmCountMapper vmCountMapper;


    @Override
    public List<VmCount> getVmList(Map<String, Object> param) {
        return vmCountMapper.getVmList(param);
    }

    @Override
    public int insertVm(VmCount vmCount) {
        return vmCountMapper.insertVm(vmCount);
    }

    @Override
    public void updateVmCount(String asin, String userName) {
        vmCountMapper.updateVmCount(asin,userName);
    }

    @Override
    public void updateVmStatus(Integer vmStatus, String userName,String message,String keyword) {
        vmCountMapper.updateVmStatus(vmStatus,userName,message,keyword);
    }
    //将异常的虚拟机标记为使用中
    @Override
    public void updateStatus(String userName) {
        vmCountMapper.updateStatus(userName);
    }

    @Override
    public VmCount findVmForId(Long id) {
        return vmCountMapper.findVmForId(id);
    }

    @Override
    public void vmCountUpdate(VmCount vmCount) {
        vmCountMapper.vmCountUpdate(vmCount);
    }

    @Override
    public void restart(Long id) {
        vmCountMapper.restart(id);
    }

    @Override
    public void updateWar(Long id) {
        vmCountMapper.updateWar(id);
    }

    @Override
    public VmCount getCountryAndTaskName(String userName) {
        return vmCountMapper.getCountryAndTaskName(userName);
    }

    @Override
    public void updateRestartStatus(String userName) {
        vmCountMapper.updateRestartStatus(userName);
    }

    @Override
    public void updateWarStatus(String userName) {
        vmCountMapper.updateWarStatus(userName);
    }
}
