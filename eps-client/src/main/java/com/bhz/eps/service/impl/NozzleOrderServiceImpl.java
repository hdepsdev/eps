package com.bhz.eps.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.bhz.eps.dao.NozzleOrderDao;
import com.bhz.eps.entity.NozzleOrder;
import com.bhz.eps.service.NozzleOrderService;

@Service("nozzleOrderService")
public class NozzleOrderServiceImpl implements NozzleOrderService {
	@Resource
	NozzleOrderDao orderDao;
	@Override
	public void addOrder(NozzleOrder order) {
		orderDao.addOrder(order);
	}
	@Override
	public NozzleOrder getOrder() {
		return orderDao.getOrder();
	}
	@Override
	public NozzleOrder getOrderByNozzleNumberAndWorkOrder(String nozzleNumber, String workOrder) {
		return orderDao.getOrderByNozzleNumberAndWorkOrder(nozzleNumber, workOrder);
	}
	@Override
	public void updateOrderStatus(int status, String nozzleNumber, String workOrder) {
		orderDao.updateOrderStatus(status, nozzleNumber, workOrder);
	}
}
