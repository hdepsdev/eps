package com.bhz.eps.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.bhz.eps.dao.ShiftInfoDao;
import com.bhz.eps.entity.ShiftInfo;
import com.bhz.eps.service.GetShiftlistService;

@Service("getShiftlistService")
public class GetShiftlistServiceImpl implements GetShiftlistService {
	@Resource
	ShiftInfoDao shiftInfoDao;
	@Override
	public List<ShiftInfo> getShiftInfos() {
		return shiftInfoDao.queryShiftInfos();
	}

}
