package com.bhz.eps.msg;

public class BizMessageType {
	public static final short CONN = 0x01;
	public static final short HEARTBEAT = 0x02;
	public static final short FP_INFO = 0x03;
	public static final short FP_ORDERLIST = 0x04;
	public static final short LOCK_ORDER = 0x05;
	public static final short GET_TRANS_DETAIL = 0x06;
	public static final short ORDER_PAY_COMPLETE = 0x07;
	public static final short UNLOCK_ORDER = 0x08;
	
	public static final short BPOS_ORDER = 0x70;
}
