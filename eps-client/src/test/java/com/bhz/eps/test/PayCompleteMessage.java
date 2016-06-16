package com.bhz.eps.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import com.bhz.eps.util.Converts;

public class PayCompleteMessage extends AbstractPosMessage{
	@Override
	public byte[] generateMessage() {
		int nozzlecode = Integer.parseInt("80087938");
		ByteBuf bb = Unpooled.buffer();
		byte[] macByte = new byte[4];
		long size = 40;
		bb.writeBytes(genTPDUHeader(8 + size + 4));
		bb.writeBytes(genBizHeader(7));
		bb.writeInt(nozzlecode);//油枪编号
		bb.writeBytes(Converts.str2Bcd("2016060716553355896150"));//支付信息流水号
		bb.writeByte(1);//支付方式
		bb.writeByte(0);
		bb.writeByte(0);//优惠方式
		bb.writeBytes(Converts.str2Bcd("000000000000"));//积分信息
		bb.writeBytes(Converts.str2Bcd("0000000000032050"));//折前金额
		bb.writeBytes(Converts.str2Bcd("0000000000032050"));//折后金额
		bb.writeBytes(macByte);
		ByteBuf r = bb.copy(0, bb.readableBytes());
		return r.array();
	}
}
