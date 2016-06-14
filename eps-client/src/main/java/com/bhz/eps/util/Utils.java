package com.bhz.eps.util;

import java.text.SimpleDateFormat;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Utils {
	
	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	
	// UUID Dictionary (Alpha + Number)
	public static String[] chars = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
			"o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8",
			"9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z" };
	// UUID Dictionary (Only Number)
	public static String[] uuidNumbers = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };

	public static String generate8BitNumberUUID() {
		StringBuffer shortBuffer = new StringBuffer();
		String uuid = UUID.randomUUID().toString().replace("-", "");
		for (int i = 0; i < 8; i++) {
			String str = uuid.substring(i * 4, i * 4 + 4);
			int x = Integer.parseInt(str, 16);
			shortBuffer.append(uuidNumbers[x % 0x0A]);
		}
		return shortBuffer.toString();
	}
	
	public static String generate8BitUUID(){
		StringBuffer shortBuffer = new StringBuffer();
		String uuid = UUID.randomUUID().toString().replace("-", "");
		for (int i = 0; i < 8; i++) {
			String str = uuid.substring(i * 4, i * 4 + 4);
			int x = Integer.parseInt(str, 16);
			shortBuffer.append(chars[x % 0x3E]);
		}
		return shortBuffer.toString();
	}
	
	public static byte getSysVersion(){
		return 0x01;
	}
	
	public static String getServerTime(){
		return sdf.format(System.currentTimeMillis());
	}
	
	public static byte[] genTPDUHeader(long tpduLength,byte crc8Value){
		ByteBuf bb = Unpooled.buffer(10);
		bb.writeByte(0x10).writeByte(0x10);
		bb.writeInt((int)tpduLength);
		bb.writeByte(0x01);
		bb.writeShort(0x0000);
		bb.writeByte(0);
		return bb.array();
	}
	
	public static byte[] concatTwoByteArray(byte[] b1,byte[] b2){
		byte[] result = new byte[b1.length + b2.length];
		System.arraycopy(b1, 0, result, 0, b1.length);
		System.arraycopy(b2, 0, result, b1.length, b2.length);
		return result;
	}
}
