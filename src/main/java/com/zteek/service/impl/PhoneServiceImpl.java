package com.zteek.service.impl;

import com.zteek.entity.PhoneLog;
import com.zteek.mapper.PhoneMapper;
import com.zteek.service.PhoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 *
 * @author leo
 * @date 2018/10/20 09:47
 */
@Service
public class PhoneServiceImpl implements PhoneService {

    @Autowired
    private PhoneMapper phoneMapper;

    @Override
    public int recordLog(String imei, String message, Date createdTime) {
        if(null == createdTime){
            createdTime = new Date();
        }
        return phoneMapper.recordLog(imei,message,createdTime);
    }

    @Override
    public PhoneLog getNewLogByImei(String imei) {
        List<PhoneLog> logs = phoneMapper.getNewLogByImei(imei, null);
        if(null != logs && logs.size() > 0){
            return logs.get(0);
        }
        return null;
    }

    @Override
    public List<PhoneLog> getNewLogs(String imei, Long id) {
        return phoneMapper.getNewLogByImei(imei, id);
    }

    @Override
    public List<PhoneLog> getRecentlyLog(int recentlyMinute) {
        return phoneMapper.getRecentlyLog(recentlyMinute);
    }


}
