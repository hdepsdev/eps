package com.bhz.eps.dao;

import org.apache.ibatis.annotations.Param;

import com.bhz.eps.entity.StationPsamInfo;

public interface StationPsamInfoDao {
	public StationPsamInfo checkStationPsam(@Param("code")String code);
}
