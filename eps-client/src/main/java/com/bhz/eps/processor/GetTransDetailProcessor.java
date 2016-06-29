package com.bhz.eps.processor;

import java.math.BigDecimal;
import java.math.MathContext;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import com.bhz.eps.Boot;
import com.bhz.eps.annotation.BizProcessorSpec;
import com.bhz.eps.entity.NozzleOrder;
import com.bhz.eps.msg.BizMessageType;
import com.bhz.eps.pdu.TPDU;
import com.bhz.eps.service.NozzleOrderService;
import com.bhz.eps.util.Converts;

/**
 * 根据油枪编号和支付信息流水号返回交易详细信息
 * @author yangxb
 *
 */

@BizProcessorSpec(msgType=BizMessageType.GET_TRANS_DETAIL)
public class GetTransDetailProcessor extends BizProcessor{


	@Override
	public void process() {
		TPDU tpdu = (TPDU)this.msgObject;
		byte[] cnt = tpdu.getBody().getData().getContent();
		byte[] nozzleCodeArr = new byte[4];//油枪编号：U32
		byte[] orderArr = new byte[11];//支付流水号：BCD 11
		
		int idx = 0;
		System.arraycopy(cnt, idx, nozzleCodeArr, 0, nozzleCodeArr.length);
		idx += nozzleCodeArr.length;
		System.arraycopy(cnt, idx, orderArr, 0, orderArr.length);
		idx += orderArr.length;
		
		NozzleOrderService nos = Boot.appctx.getBean(
				"nozzleOrderService",NozzleOrderService.class);
		NozzleOrder no = nos.getOrderByNozzleNumberAndWorkOrder
				(Integer.toString(Converts.bytes2Int(nozzleCodeArr)), 
						Converts.bcd2Str(orderArr));
		
		if (no == null)
			return;
		
		//创建返回消息
		byte[] bizHeaderArr = tpdu.getBody().getHeader().getOriginalContent();
		byte[] price = Converts.addZeroInLeftSide(Converts.str2Bcd(
				Integer.toString(no.getPrice())), 5);
		byte[] count = Converts.addZeroInLeftSide(Converts.str2Bcd(Integer.toString(
				((new BigDecimal(no.getPrice())).multiply(no.getVolumeConsume()))
				.divide(new BigDecimal(1), 0, BigDecimal.ROUND_HALF_UP).intValue())), 8);
		byte[] amount = Converts.addZeroInLeftSide(Converts.str2Bcd(Integer.toString(
				no.getVolumeConsume().multiply(new BigDecimal(100),new MathContext(BigDecimal.ROUND_HALF_UP)).intValue())), 6);
		
		ByteBuf b = Unpooled.buffer(bizHeaderArr.length + orderArr.length + 
				1 + 1 + price.length + count.length + amount.length);
		b.writeBytes(bizHeaderArr);
		b.writeBytes(orderArr);
		b.writeByte(no.getOilType());
		b.writeByte(no.getOilCategory());
		b.writeBytes(price);
		b.writeBytes(count);
		b.writeBytes(amount);
		
		byte[] dataArr = b.array();
		b.release();
		
		this.channel.writeAndFlush(dataArr);		
	}

}
