package com.bhz.eps.service;

import com.bhz.eps.entity.NozzleOrder;

public interface NozzleOrderService {
	public void addOrder(NozzleOrder order);
	public NozzleOrder getOrder();
	public NozzleOrder getOrderByNozzleNumberAndWorkOrder
			(String nozzleNumber, String workOrder);
	public void updateOrderStatus(int status, 
			String nozzleNumber, String workOrder);
}