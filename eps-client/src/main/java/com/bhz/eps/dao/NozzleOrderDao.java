package com.bhz.eps.dao;

import org.apache.ibatis.annotations.Param;

import com.bhz.eps.entity.NozzleOrder;

public interface NozzleOrderDao {
	public void addOrder(NozzleOrder order);
	public NozzleOrder getOrder();
	public NozzleOrder getOrderByNozzleNumberAndWorkOrder(@Param("nozzleNumber") String nozzleNumber,
			@Param("workOrder") String workOrder);
	public void updateOrderStatus(@Param("status") int status, @Param("nozzleNumber") String nozzleNumber,
			@Param("workOrder") String workOrder);
}
