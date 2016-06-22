package com.bhz.eps.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public NozzleOrder getOrderByWorkorder(String workOrder) {
		return orderDao.getOrderByWorkorder(workOrder);
	}
	@Override
	public NozzleOrder getOrderByNozzleNumberAndWorkOrder(String nozzleNumber, String workOrder) {
		return orderDao.getOrderByNozzleNumberAndWorkOrder(nozzleNumber, workOrder);
	}
	@Override
	public void updateOrderStatus(int status, String nozzleNumber, String workOrder) {
		orderDao.updateOrderStatus(status, nozzleNumber, workOrder);
	}
	@Override
	public List<NozzleOrder> queryUnUploadOrders() {
		return orderDao.queryUnUploadOrders();
	}
	@Override
	public void updateUploadStatus(String workOrder, int uploadStatus, String uploadTime) {
		Map<String,Object> paramMap = new HashMap<String,Object>();
		paramMap.put("workOrder", workOrder);
		paramMap.put("uploadStatus", uploadStatus);
		paramMap.put("uploadTime", uploadTime);
		orderDao.updateUploadStatus(paramMap);
	}
}