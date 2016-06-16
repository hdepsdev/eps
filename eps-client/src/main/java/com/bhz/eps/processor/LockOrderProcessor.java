package com.bhz.eps.processor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.math.BigDecimal;
import java.util.List;

import com.bhz.eps.Boot;
import com.bhz.eps.annotation.BizProcessorSpec;
import com.bhz.eps.entity.NozzleOrder;
import com.bhz.eps.msg.BizMessageType;
import com.bhz.eps.pdu.TPDU;
import com.bhz.eps.service.GetOrderlistService;
import com.bhz.eps.service.OrderStatusService;
import com.bhz.eps.util.Converts;


/**
 * 获取油枪待支付列表
 * @author txy
 *
 */
@BizProcessorSpec(msgType=BizMessageType.LOCK_ORDER)
public class LockOrderProcessor extends BizProcessor{
	@Override
	public void process() {
		TPDU tpdu = (TPDU)this.msgObject;
		byte[] cnt = tpdu.getBody().getData().getContent();
		byte[] fpNumber = new byte[4];
		System.arraycopy(cnt, 0, fpNumber, 0, fpNumber.length);
		byte[] payInfoNum = new byte[11];
		System.arraycopy(cnt, 4, payInfoNum, 0, payInfoNum.length);
		
		//创建返回消息
		OrderStatusService oss = Boot.appctx.getBean("orderStatusService",OrderStatusService.class);
		int status = oss.GetOrderSta(Integer.toString(Converts.bytes2Int(fpNumber)), Converts.bcd2Str(payInfoNum));
		byte[] bizHeaderArr = tpdu.getBody().getHeader().getOriginalContent();//业务数据包头
		byte[] orderstatus = Converts.int2bytes(status);
		
		ByteBuf b = Unpooled.buffer(bizHeaderArr.length + orderstatus.length );
		b.writeBytes(bizHeaderArr);
		b.writeBytes(orderstatus);
		
		byte[] dataArr = b.array();
		b.release();

		this.channel.writeAndFlush(dataArr);
	}
}
