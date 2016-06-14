package com.bhz.eps.test;

import java.util.List;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bhz.eps.entity.FPInfo;
import com.bhz.eps.service.FPInfoService;

public class FPInfoServiceTest {
	ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{"conf/application-context.xml"});
	
	@Test
	public void testShowFPInfo(){
		FPInfoService fpis = ctx.getBean("fpInfoService",FPInfoService.class);
		List<FPInfo> result = fpis.getAllNozzle();
		for(FPInfo info:result){
			System.out.println(info.getNozzleNumber());
		}
	}
}
