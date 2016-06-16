package com.bhz.eps.service;

import java.util.List;

import com.bhz.eps.entity.NozzleOrder;

public interface GetOrderlistService {
	public List<NozzleOrder> getOrderlist(String nozzlecode);
}
