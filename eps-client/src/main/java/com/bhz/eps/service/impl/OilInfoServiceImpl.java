package com.bhz.eps.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.bhz.eps.dao.OilInfoDao;
import com.bhz.eps.entity.OilInfo;
import com.bhz.eps.service.OilInfoService;

@Service("oilInfoService")
public class OilInfoServiceImpl implements OilInfoService {
	@Resource
	private OilInfoDao oilInfoDao;
	
	@Override
	public List<OilInfo> getOilTypeList() {
		return oilInfoDao.getOilTypeList();
	}

	@Override
	public List<OilInfo> getOilCategoryList() {
		return oilInfoDao.getOilCategoryList();
	}

}
