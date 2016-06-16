package com.bhz.eps.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import com.bhz.eps.util.Converts;

public class UnlockOrderMessage extends AbstractPosMessage{
	@Override
	public byte[] generateMessage() {
		int nozzlecode = Integer.parseInt("80087938");
		
		ByteBuf bb = Unpooled.buffer();
		byte[] macByte = new byte[4];
		long size = 15;
		bb.writeBytes(genTPDUHeader(8 + size + 4));
		bb.writeBytes(genBizHeader(8));
		bb.writeInt(nozzlecode);
		bb.writeBytes(Converts.str2Bcd("2016060716553355896150"));
		bb.writeBytes(macByte);
		ByteBuf r = bb.copy(0, bb.readableBytes());
		return r.array();
	}
}
