package com.bhz.eps.dao;

import org.apache.ibatis.annotations.Param;

public interface OrderStatusDao {
	public int GetOrderSta(@Param("nozzleCode") String nozzleCode,@Param("workCode") String workCode);
}
