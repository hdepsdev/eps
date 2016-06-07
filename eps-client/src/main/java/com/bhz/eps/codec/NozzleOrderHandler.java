package com.bhz.eps.codec;

import java.math.BigDecimal;

import com.bhz.eps.Boot;
import com.bhz.eps.entity.NozzleOrder;
import com.bhz.eps.pdu.TPDU;
import com.bhz.eps.service.NozzleOrderService;
import com.bhz.eps.util.Converts;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class NozzleOrderHandler extends ChannelHandlerAdapter{
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		TPDU pdu = (TPDU)msg;
		byte[] cnt = pdu.getBody().getData().getContent();
		byte[] nozzleCodeArr = new byte[4];//油枪编号：U32
		byte[] orderArr = new byte[11];//支付流水号：BCD 11
		byte[] oilTypeArr = new byte[1];//油品类别：byte 1
		byte[] oilCategoryArr = new byte[1];//油品编号：byte 1
		byte[] priceArr = new byte[5];//单价 BCD 5
		byte[] volumeArr = new byte[6];//消费升数 BCD 6
		byte[] totalAmountArr = new byte[8];//总价 BCD 8
		int idx = 0;
		System.arraycopy(cnt, idx, nozzleCodeArr, 0, nozzleCodeArr.length);
		idx += nozzleCodeArr.length;
		System.arraycopy(cnt, idx, orderArr, 0, orderArr.length);
		idx += orderArr.length;
		System.arraycopy(cnt, idx, oilTypeArr, 0, oilTypeArr.length);
		idx += oilTypeArr.length;
		System.arraycopy(cnt, idx, oilCategoryArr, 0, oilCategoryArr.length);
		idx += oilCategoryArr.length;
		System.arraycopy(cnt, idx, priceArr, 0, priceArr.length);
		idx += priceArr.length;
		System.arraycopy(cnt, idx, volumeArr, 0, volumeArr.length);
		idx += volumeArr.length;
		System.arraycopy(cnt, idx, totalAmountArr, 0, totalAmountArr.length);
		
		NozzleOrder no = new NozzleOrder();
		no.setNozzleNumber(Long.toString(Converts.U32ToLong(nozzleCodeArr)));
		no.setWorkOrder(Converts.bcd2Str(orderArr));
		no.setOilType(Converts.bytesToInt(oilTypeArr));
		no.setOilCategory(Converts.bytesToInt(oilCategoryArr));
		no.setPrice(Integer.parseInt(Converts.bcd2Str(priceArr)));
		BigDecimal volume = new BigDecimal(Converts.bcd2Str(volumeArr));
		no.setVolumeConsume(volume.divide(new BigDecimal(100)));
		no.setOrderStatus(1);
		
		NozzleOrderService nos = Boot.appctx.getBean("nozzleOrderService",NozzleOrderService.class);
		nos.addOrder(no);
		ctx.writeAndFlush(pdu.getBody().getData().getContent());
	}
}

