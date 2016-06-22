package com.bhz.eps.service;

import java.util.List;

import com.bhz.eps.entity.NozzleOrder;

public interface NozzleOrderService {
	public void addOrder(NozzleOrder order);
	public NozzleOrder getOrderByWorkorder(String workOrder);
	public NozzleOrder getOrderByNozzleNumberAndWorkOrder(String nozzleNumber, String workOrder);
	public void updateOrderStatus(int status,String nozzleNumber, String workOrder);
	public List<NozzleOrder> queryUnUploadOrders();
	public void updateUploadStatus(String workOrder,int uploadStatus,String uploadTime);
}