package com.bhz.eps.test;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bhz.eps.entity.NozzleOrder;
import com.bhz.eps.service.NozzleOrderService;
import com.bhz.eps.util.Utils;

public class NozzleOrderServiceTest {
	
	ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{"conf/application-context.xml"});
	@Before
	public void init(){
		
	}
	
	@Test
	public void addOrderTest(){
		NozzleOrderService nos = ctx.getBean("nozzleOrderService",NozzleOrderService.class);
		NozzleOrder no = new NozzleOrder();
		no.setWorkOrder("order2");
		no.setNozzleNumber("e-14");
		no.setOilType(1);
		no.setOilCategory(10);
		no.setPrice(569);
		no.setVolumeConsume(new BigDecimal(34.50));
		no.setStationCode("1000020000");
		no.setUploadStatus(NozzleOrder.UN_UPLOAD);
		nos.addOrder(no);
	}
	
	@Test
	public void getOrderTest(){
		NozzleOrderService nos = ctx.getBean("nozzleOrderService",NozzleOrderService.class);
		NozzleOrder no = nos.getOrderByWorkorder("order1");
		System.out.println(no.getPrice() + "\t" + no.getVolumeConsume());
	}
	
	@Test
	public void t1(){
		System.out.println(Runtime.getRuntime().availableProcessors());
	}
	
	@Test
	public void queryUnUploadOrdersTest(){
		NozzleOrderService nos = ctx.getBean("nozzleOrderService",NozzleOrderService.class);
		List<NozzleOrder> result = nos.queryUnUploadOrders();
		for(NozzleOrder no:result){
			System.out.println(no.getPrice() + "\t" + no.getVolumeConsume());
		}
	}
	
	@Test
	public void updateUploadStatusTest(){
		NozzleOrderService nos = ctx.getBean("nozzleOrderService",NozzleOrderService.class);
		nos.updateUploadStatus("order1", NozzleOrder.UPLOADED, Utils.getServerTime());
	}
}
