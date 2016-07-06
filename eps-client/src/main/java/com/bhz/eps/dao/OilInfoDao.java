package com.bhz.eps.dao;

import java.util.List;
import com.bhz.eps.entity.OilInfo;

public interface OilInfoDao {
	public List<OilInfo> getOilTypeList();
	public List<OilInfo> getOilCategoryList();
}
