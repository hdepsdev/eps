package com.bhz.eps.codec;

import com.bhz.eps.util.PosMessageEncryption;
import com.bhz.eps.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class TPDUEncoder extends MessageToByteEncoder<Object> {


    public TPDUEncoder() {
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
    	
    	byte[] dataArr = (byte[])msg;
    	byte[] dataMac = null;
		try {
			dataMac = PosMessageEncryption.getPOSMac(dataArr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		byte[] bizDataArr = Utils.concatTwoByteArray(dataArr, dataMac);
		
		byte[] tpduHeader = Utils.genTPDUHeader(bizDataArr.length);
		byte[] responseMsg = Utils.concatTwoByteArray(tpduHeader, bizDataArr);
		
    	out.writeBytes(responseMsg);
    }
}
