package com.bhz.eps.processor;

import java.math.BigDecimal;

import com.bhz.eps.Boot;
import com.bhz.eps.annotation.BizProcessorSpec;
import com.bhz.eps.entity.NozzleOrder;
import com.bhz.eps.msg.BizMessageType;
import com.bhz.eps.pdu.TPDU;
import com.bhz.eps.service.NozzleOrderService;
import com.bhz.eps.util.Converts;

/**
 * 接受处理BPOS发送的交易信息
 * @author yaoh
 *
 */
@BizProcessorSpec(msgType=BizMessageType.BPOS_ORDER)
public class BPosOrderProcessor extends BizProcessor{
	@Override
	public void process() {
		TPDU tpdu = (TPDU)this.msgObject;
		byte[] cnt = tpdu.getBody().getData().getContent();
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
		channel.writeAndFlush(tpdu.getBody().getData().getContent());
	}
}
