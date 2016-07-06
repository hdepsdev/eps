package com.bhz.eps.service;

import java.util.List;
import com.bhz.eps.entity.OilInfo;

public interface OilInfoService {
	public List<OilInfo> getOilTypeList();
	public List<OilInfo> getOilCategoryList();
}
