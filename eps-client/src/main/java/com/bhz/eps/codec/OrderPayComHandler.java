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
public class OrderPayComHandler extends ChannelHandlerAdapter {

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
		byte[] payType = new byte[1];
		System.arraycopy(cnt, 15, payType, 0, payType.length);
		byte[] discountType = new byte[2];
		System.arraycopy(cnt, 16, discountType, 0, discountType.length);
		byte[] integralInfo = new byte[6];
		System.arraycopy(cnt, 18, integralInfo, 0, integralInfo.length);
		byte[] amonutBeforeDis = new byte[8];
		System.arraycopy(cnt, 24, amonutBeforeDis, 0, amonutBeforeDis.length);
		byte[] amonutAfterDis = new byte[8];
		System.arraycopy(cnt, 32, amonutAfterDis, 0, amonutAfterDis.length);
		ctx.writeAndFlush(pdu.getBody().getData().getContent());
	}

}

