package com.bhz.eps.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.bhz.eps.dao.FPInfoDao;
import com.bhz.eps.entity.FPInfo;
import com.bhz.eps.service.FPInfoService;

@Service("fpInfoService")
public class FPInfoServiceImpl implements FPInfoService {
	private static final Logger logger = LogManager.getLogger(FPInfoServiceImpl.class);
	@Resource
	FPInfoDao fpDao;
	
	@Override
	public List<FPInfo> getAllNozzle() {
		return fpDao.showFPInfo();
	}
	
}
