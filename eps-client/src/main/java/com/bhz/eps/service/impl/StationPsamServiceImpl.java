package com.bhz.eps.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.bhz.eps.dao.StationPsamInfoDao;
import com.bhz.eps.entity.StationPsamInfo;
import com.bhz.eps.service.StationPsamInfoService;;

@Service("stationPsamService")
public class StationPsamServiceImpl implements StationPsamInfoService {
	@Resource
	StationPsamInfoDao stationInfoDao;
	@Override
	public StationPsamInfo checkStationPsam(String code){
		return stationInfoDao.checkStationPsam(code);
	}
}
