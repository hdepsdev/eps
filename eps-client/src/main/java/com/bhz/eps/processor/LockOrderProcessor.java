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


/**
 * 锁定待支付订单
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
		
		byte re = 0x01;
		try{
			NozzleOrderService nos = Boot.appctx.getBean(
					"nozzleOrderService",NozzleOrderService.class);
			
			NozzleOrder no = nos.getOrderByNozzleNumberAndWorkOrder
					(Integer.toString(Converts.bytes2Int(fpNumber)), 
							Converts.bcd2Str(payInfoNum));
			
			if (no != null) {
				if(no.getOrderStatus() == NozzleOrder.ORDER_LOCKED)
				{
					re = 0x01;
				}					
				else
				{
					nos.updateOrderStatus(NozzleOrder.ORDER_LOCKED, 
							Integer.toString(Converts.bytes2Int(fpNumber))
							, Converts.bcd2Str(payInfoNum));
					re = 0x00;
				}
			}
		}
		catch(Exception e){
			
		}
		
		//创建返回消息

		byte[] bizHeaderArr = tpdu.getBody().getHeader().getOriginalContent();//业务数据包头
		
		ByteBuf b = Unpooled.buffer(bizHeaderArr.length + 1 );
		b.writeBytes(bizHeaderArr);
		b.writeByte(re);//锁定标示
		
		byte[] dataArr = b.array();
		b.release();

		this.channel.writeAndFlush(dataArr);
	}
}
