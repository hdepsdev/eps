package com.bhz.eps.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.bhz.eps.entity.NozzleOrder;

public interface NozzleOrderDao {
	public void addOrder(NozzleOrder order);
	public NozzleOrder getOrderByWorkorder(@Param("workOrder")String workOrder);
	public NozzleOrder getOrderByNozzleNumberAndWorkOrder(@Param("nozzleNumber") String nozzleNumber,@Param("workOrder") String workOrder);
	public void updateOrderStatus(@Param("status") int status, @Param("nozzleNumber") String nozzleNumber,@Param("workOrder") String workOrder);
	public List<NozzleOrder> queryUnUploadOrders();
	public void updateUploadStatus(Map<String,Object> paramMap);
	
}
