package com.bhz.eps.test;

import java.math.BigDecimal;

import org.junit.Test;

import com.bhz.eps.util.Converts;

public class CommonTest {
	@Test
	public void testInt2Byte(){
		int i=256;
		byte[] b = Converts.intToByte(i);
		for(byte b1:b){
			System.out.println(b1);
		}
	}
	
	@Test
	public void testGenTpduHeaderLength(){
		PosConnectMessage pcm = new PosConnectMessage();
		byte[] a1 = pcm.generateMessage();
		System.out.println(a1.length);
	}
	
	@Test
	public void testNumber(){
		BigDecimal b = new BigDecimal(3456);
		BigDecimal rb = b.divide(new BigDecimal(100));
		System.out.println(rb.toString());
	}
}
