package com.bhz.eps.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.bhz.eps.dao.GetOrderlistDao;
import com.bhz.eps.entity.NozzleOrder;
import com.bhz.eps.service.GetOrderlistService;
@Service("getOrderlistService")
public class GetOrderlistServiceImpl implements GetOrderlistService{
	@Resource
	GetOrderlistDao orderList;
	@Override
	public List<NozzleOrder> getOrderlist(String nozzlecode){
		return orderList.getOrderlist(nozzlecode);
	}
}
