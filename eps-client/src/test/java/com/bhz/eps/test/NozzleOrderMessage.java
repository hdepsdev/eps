package com.bhz.eps.test;

import com.bhz.eps.util.Converts;
import com.bhz.eps.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class NozzleOrderMessage extends AbstractPosMessage {

	@Override
	public byte[] generateMessage() {
		int nozzleCode = Integer.parseInt(Utils.generate8BitNumberUUID());
		ByteBuf bb = Unpooled.buffer();
		
		byte[] macByte = new byte[4];
		long size = 36;
		bb.writeBytes(genTPDUHeader(8 + size + 4));
		bb.writeBytes(genBizHeader(0x70));
		bb.writeInt(nozzleCode);
		bb.writeBytes(Converts.str2Bcd("20160607165533" + Utils.generate8BitNumberUUID()));
		bb.writeByte(1);
		bb.writeByte(1);
		bb.writeBytes(Converts.str2Bcd("0000000597"));
		bb.writeBytes(Converts.str2Bcd("000000003056"));
		bb.writeBytes(Converts.str2Bcd("0000000000018244"));
		bb.writeBytes(macByte);
		ByteBuf r = bb.copy(0, bb.readableBytes());
		return r.array();
	}

}

