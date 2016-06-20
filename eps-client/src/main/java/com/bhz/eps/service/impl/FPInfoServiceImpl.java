package com.bhz.eps.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.bhz.eps.dao.FPInfoDao;
import com.bhz.eps.entity.FPInfo;
import com.bhz.eps.service.FPInfoService;

@Service("fpInfoService")
public class FPInfoServiceImpl implements FPInfoService {

	@Resource
	FPInfoDao fpDao;
	
	@Override
	public List<FPInfo> getAllNozzle() {
		return fpDao.showFPInfo();
	}
	
}
