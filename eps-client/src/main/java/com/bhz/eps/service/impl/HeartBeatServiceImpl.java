package com.bhz.eps.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.bhz.eps.dao.HeartBeatDao;
import com.bhz.eps.entity.FPInfo;
import com.bhz.eps.service.HeartBeatService;
@Service("heratBeatService")
public class HeartBeatServiceImpl implements HeartBeatService{
	@Resource
	HeartBeatDao HeratBdao;
	
	@Override
	public List<FPInfo> getNozzleCode()
	{
		return HeratBdao.getNozzleCode();
	}
}
