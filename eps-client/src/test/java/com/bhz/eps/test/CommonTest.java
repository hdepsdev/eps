package com.bhz.eps.test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.bhz.eps.util.Converts;
import com.bhz.eps.util.Utils;

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
	
	@Test
	public void testByte2Int(){
		byte[] x = new byte[1];
		x[0] = 0x10;
		int i = Converts.bytesToInt(x);
		System.out.println(i);
	}
	@Test
	public void testByte2Long(){
		byte[] x = new byte[3];
		x[0] = 0x01;
		x[1] = (byte) 0xe2;
		x[2] = (byte) 0x40;
		long i = Converts.U32ToLong(x);
		System.out.println(i);
	}
	
	@Test
	public void test8BitNumberUUID(){
		Set<String> a = new HashSet<String>();
		for(int i=0;i<5000;i++){
			String s = Utils.generate8BitNumberUUID();
			a.add(s);
			System.out.print( s + "\t");
			if((i +1)%10==0) System.out.print("\r");
		}
		System.out.println();
		System.out.println("Identical Amount: " + a.size());
		
	}
	
	@Test
	public void test8BitUUID(){
		Set<String> a = new HashSet<String>();
		for(int i=0;i<10000000;i++){
			String s = Utils.generate8BitUUID();
			a.add(s);
			System.out.print( s + "\t");
			if((i +1)%10==0) System.out.print("\r");
		}
		System.out.println();
		System.out.println("Identical Amount: " + a.size());
		
	}
}
