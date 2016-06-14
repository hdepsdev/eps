package com.bhz.eps.processor;

import java.util.List;

import com.bhz.eps.Boot;
import com.bhz.eps.annotation.BizProcessorSpec;
import com.bhz.eps.entity.FPInfo;
import com.bhz.eps.entity.PosRegInfo;
import com.bhz.eps.msg.BizMessageType;
import com.bhz.eps.pdu.TPDU;
import com.bhz.eps.service.FPInfoService;
import com.bhz.eps.util.Converts;
import com.bhz.eps.util.PosMessageEncryption;
import com.bhz.eps.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

@BizProcessorSpec(msgType=BizMessageType.FP_INFO)
public class FPInfoProcessor extends BizProcessor {

	@Override
	public void process() {
		TPDU tpdu = (TPDU)this.msgObject;
		byte[] cnt = tpdu.getBody().getData().getContent();
		byte[] posCodeArr = new byte[10];
		System.arraycopy(cnt, 0, posCodeArr, 0, posCodeArr.length);
		byte[] psamCodeArr = new byte[10];
		System.arraycopy(cnt, 10, psamCodeArr, 0, psamCodeArr.length);
		PosRegInfo pos = new PosRegInfo();
		pos.setPosCode(new String(posCodeArr));//设置POS编号
		pos.setPsamNum(Converts.bcd2Str(psamCodeArr));//设置POS PSAM卡号
		pos.setStatus(1);
		// TODO 
		//check pos valication
		
		//创建返回消息
		FPInfoService fpis = Boot.appctx.getBean("fpInfoService",FPInfoService.class);
		List<FPInfo> nozzles = fpis.getAllNozzle();
		
		byte[] bizHeaderArr = tpdu.getBody().getHeader().getOriginalContent();//业务数据包头
		byte[] nozzleAmount = Converts.long2U32(nozzles.size());//油枪数量
		StringBuffer sb = new StringBuffer();
		for(FPInfo info:nozzles){
			sb.append(info.getNozzleNumber()).append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		byte[] nozzleCodes = new byte[sb.length()];//油枪编号
		for(int i=0;i<nozzleCodes.length;i++){
			nozzleCodes[i] = (byte) sb.charAt(i);
		}
		ByteBuf b = Unpooled.buffer(bizHeaderArr.length + nozzleAmount.length + nozzleCodes.length);
		b.writeBytes(bizHeaderArr);
		b.writeBytes(nozzleAmount);
		b.writeBytes(nozzleCodes);
		
		byte[] dataArr = b.array();
		byte[] dataMac = null;
		try {
			dataMac = PosMessageEncryption.getPOSMac(dataArr);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally{
			b.release();
		}
		
		byte[] bizDataArr = Utils.concatTwoByteArray(dataArr, dataMac);
		
		byte[] tpduHeader = Utils.genTPDUHeader(bizDataArr.length,(byte)0x00);
		byte[] responseMsg = Utils.concatTwoByteArray(tpduHeader, bizDataArr);
		this.channel.writeAndFlush(responseMsg);
		
	}

}
