package com.bhz.eps.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.bhz.eps.entity.NozzleOrder;

public interface GetOrderlistDao {
	public List<NozzleOrder> getOrderlist(@Param("nozzlecode") String nozzlecode);
}
