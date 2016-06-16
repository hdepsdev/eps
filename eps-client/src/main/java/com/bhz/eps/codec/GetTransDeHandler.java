package com.bhz.eps.codec;

import com.bhz.eps.Boot;
import com.bhz.eps.entity.PosRegInfo;
import com.bhz.eps.pdu.TPDU;
import com.bhz.eps.service.PosRegService;
import com.bhz.eps.util.Converts;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler;

@ChannelHandler.Sharable
public class GetTransDeHandler extends ChannelHandlerAdapter {

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println(this.getClass().getName());
		TPDU pdu = (TPDU)msg;
		byte[] cnt = pdu.getBody().getData().getContent();
		byte[] fpNumber = new byte[4];
		System.arraycopy(cnt, 0, fpNumber, 0, fpNumber.length);
		byte[] payInfoNum = new byte[11];
		System.arraycopy(cnt, 4, payInfoNum, 0, payInfoNum.length);
		ctx.writeAndFlush(pdu.getBody().getData().getContent());
	}

}
