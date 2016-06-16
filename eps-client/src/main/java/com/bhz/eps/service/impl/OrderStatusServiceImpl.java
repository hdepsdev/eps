package com.bhz.eps.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.bhz.eps.dao.OrderStatusDao;
import com.bhz.eps.service.OrderStatusService;

@Service("orderStatusService")
public class OrderStatusServiceImpl implements OrderStatusService{
	@Resource
	OrderStatusDao orderstatus;
	@Override
	public int GetOrderSta(String nozzleCode, String workCode){
		return orderstatus.GetOrderSta(nozzleCode, workCode);
	}
}
