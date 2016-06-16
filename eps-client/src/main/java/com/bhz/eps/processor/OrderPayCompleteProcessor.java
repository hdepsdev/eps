package com.bhz.eps.processor;

import com.bhz.eps.Boot;
import com.bhz.eps.annotation.BizProcessorSpec;
import com.bhz.eps.entity.NozzleOrder;
import com.bhz.eps.msg.BizMessageType;
import com.bhz.eps.pdu.TPDU;
import com.bhz.eps.service.NozzleOrderService;
import com.bhz.eps.util.Converts;

@BizProcessorSpec(msgType=BizMessageType.ORDER_PAY_COMPLETE)
public class OrderPayCompleteProcessor extends BizProcessor{

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
		nos.updateOrderStatus(NozzleOrder.ORDER_PAYED, 
				Converts.bcd2Str(nozzleCodeArr), Converts.bcd2Str(orderArr));
	}

}