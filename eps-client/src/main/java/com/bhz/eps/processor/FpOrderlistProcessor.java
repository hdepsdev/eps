package com.bhz.eps.processor;

import java.math.BigDecimal;
import java.util.List;

import com.bhz.eps.Boot;
import com.bhz.eps.annotation.BizProcessorSpec;
import com.bhz.eps.entity.NozzleOrder;
import com.bhz.eps.msg.BizMessageType;
import com.bhz.eps.pdu.TPDU;
import com.bhz.eps.service.GetOrderlistService;
import com.bhz.eps.util.Converts;

import io.netty.buffer.ByteBuf;		
import io.netty.buffer.Unpooled;

/**
 * 获取油枪待支付列表
 * @author txy
 *
 */
@BizProcessorSpec(msgType=BizMessageType.FP_ORDERLIST)
public class FpOrderlistProcessor extends BizProcessor{
	@Override
	public void process() {
		TPDU tpdu = (TPDU)this.msgObject;
		byte[] cnt = tpdu.getBody().getData().getContent();
		byte[] fpNumber = new byte[4];
		System.arraycopy(cnt, 0, fpNumber, 0, fpNumber.length);
		
		//创建返回消息
		GetOrderlistService gols = Boot.appctx.getBean("getOrderlistService",GetOrderlistService.class);
		List<NozzleOrder> nozzleorder = gols.getOrderlist(Integer.toString(Converts.bytes2Int(fpNumber)));
		byte[] bizHeaderArr = tpdu.getBody().getHeader().getOriginalContent();//业务数据包头
		byte[] countToPay = Converts.long2U32(nozzleorder.size());//待支付信息数量
		
		ByteBuf b = Unpooled.buffer(bizHeaderArr.length + fpNumber.length + countToPay.length + nozzleorder.size()*15);
		b.writeBytes(bizHeaderArr);
		b.writeBytes(fpNumber);
		b.writeBytes(countToPay);
		
		for(NozzleOrder order:nozzleorder){
			BigDecimal price = new BigDecimal(order.getPrice());
			BigDecimal volume = order.getVolumeConsume();
			BigDecimal payment =  price.multiply(volume);
			byte[] nozzlecode = Converts.str2Bcd(order.getWorkOrder());
			b.writeBytes(nozzlecode);//支付流水号
			byte[] totalCount = Converts.long2U32(payment.longValue());
			b.writeBytes(totalCount);//支付金额	
		}
		
		byte[] dataArr = b.array();
		b.release();

		this.channel.writeAndFlush(dataArr);
	}
}
