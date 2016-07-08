package com.bhz.eps.test;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.bhz.eps.msg.ManageMessageProto;
import com.bhz.eps.msg.ManageMessageProto.MsgType;
import com.bhz.eps.util.Converts;
import com.bhz.eps.util.Utils;
import com.google.protobuf.ByteString;

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
	
	@Test
	public void getServerTimeServer(){
		System.out.println(Utils.getServerTime());
	}
	
	@Test
	public void testArrayConcat(){
		byte[] b1 = "hello".getBytes();
		byte[] b2 = "world".getBytes();
		byte[] c = Utils.concatTwoByteArray(b1, b2);
		for(byte b:c){
			System.out.print((char)b);
		}
	}
	
	@Test
	public void testZhUTF8(){
		String s = new String("1早班   2中班   3晚班   ");
		try {
			byte[] sb = s.getBytes("utf-8");
			System.out.println(sb.length);
			System.out.println(Converts.bytesToHexString(sb));
//			for(byte b:sb){
//				System.out.print(Converts.bytesToHexString(bArray)b + "\t");
//			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testShow(){
		byte[] s = new byte[]{0x39,0x35,0x23,(byte) 0xE6,(byte) 0xB1,(byte)0xBD,(byte)0xE6,(byte)0xB2,(byte)0xB9,0x20,0x20,0x20,0x20,0x20,0x20};
		try {
			System.out.println(new String(s,"utf-8").trim());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void printEnum(){
		ManageMessageProto.ManageMessage.Builder mb = ManageMessageProto.ManageMessage.newBuilder();
		ManageMessageProto.Request.Builder rb = ManageMessageProto.Request.newBuilder();
		ManageMessageProto.LoginRequest.Builder lrb = ManageMessageProto.LoginRequest.newBuilder();
		lrb.setUsername("yaoh");
		lrb.setPassword(ByteString.copyFrom("cc".getBytes()));
		rb.setLoginRequest(lrb.build());
		mb.setRequest(rb.build());
		mb.setType(MsgType.Login_Response);
		mb.setSeqence(123);
		ManageMessageProto.ManageMessage mm = mb.build();
		
		System.out.println(mm.getType().getNumber());
	}
}
