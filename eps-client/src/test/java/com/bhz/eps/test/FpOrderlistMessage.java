package com.bhz.eps.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class FpOrderlistMessage extends AbstractPosMessage{
	@Override
	public byte[] generateMessage() {
		int nozzlecode = Integer.parseInt("80087937");
		ByteBuf bb = Unpooled.buffer();
		byte[] macByte = new byte[4];
		long size = 4;
		bb.writeBytes(genTPDUHeader(8 + size + 4));
		bb.writeBytes(genBizHeader(4));
		bb.writeInt(nozzlecode);
		bb.writeBytes(macByte);
		ByteBuf r = bb.copy(0, bb.readableBytes());
		return r.array();
	}
}
