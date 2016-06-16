package com.bhz.eps.processor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import com.bhz.eps.Boot;
import com.bhz.eps.annotation.BizProcessorSpec;
import com.bhz.eps.entity.NozzleOrder;
import com.bhz.eps.msg.BizMessageType;
import com.bhz.eps.pdu.TPDU;
import com.bhz.eps.service.NozzleOrderService;
import com.bhz.eps.util.Converts;

@BizProcessorSpec(msgType = BizMessageType.UNLOCK_ORDER)
public class UnlockOrderProcessor extends BizProcessor{

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
		
		byte re = 0x00;
		try{
			NozzleOrderService nos = Boot.appctx.getBean(
					"nozzleOrderService",NozzleOrderService.class);
			nos.updateOrderStatus(NozzleOrder.ORDER_NOT_PAYED, 
					Converts.bcd2Str(nozzleCodeArr), Converts.bcd2Str(orderArr));
			re = 0x01;
		}
		catch(Exception e){
			
		}
		
		//创建返回消息
		byte[] bizHeaderArr = tpdu.getBody().getHeader().getOriginalContent();
		ByteBuf b = Unpooled.buffer(bizHeaderArr.length + 1);
		
		b.writeBytes(bizHeaderArr);
		b.writeByte(re);
		
		byte[] dataArr = b.array();
		b.release();
		
		this.channel.writeAndFlush(dataArr);
	}

}
