package com.bhz.eps.processor;

import java.util.List;

import com.bhz.eps.Boot;
import com.bhz.eps.annotation.BizProcessorSpec;
import com.bhz.eps.entity.FPInfo;
import com.bhz.eps.entity.PosRegInfo;
import com.bhz.eps.msg.BizMessageType;
import com.bhz.eps.pdu.TPDU;
import com.bhz.eps.service.HeartBeatService;
import com.bhz.eps.service.PosRegService;
import com.bhz.eps.util.Converts;
import com.bhz.eps.util.Utils;

import io.netty.buffer.ByteBuf;		
import io.netty.buffer.Unpooled;
/**
 * 心跳
 * @author txy
 *
 */
@BizProcessorSpec(msgType=BizMessageType.HEARTBEAT)

public class HeartBeatProcessor extends BizProcessor {
	@Override
	public void process() {
		TPDU tpdu = (TPDU)this.msgObject;
		byte[] cnt = tpdu.getBody().getData().getContent();
		byte[] posCodeArr = new byte[10];
		System.arraycopy(cnt, 0, posCodeArr, 0, posCodeArr.length);
		byte[] psamCodeArr = new byte[10];
		System.arraycopy(cnt, 10, psamCodeArr, 0, psamCodeArr.length);
		PosRegInfo pos = new PosRegInfo();
		pos.setPosCode(new String(posCodeArr));
		pos.setPsamNum(Converts.bcd2Str(psamCodeArr));
		pos.setStatus(1);
		PosRegService pss = Boot.appctx.getBean("posRegService",PosRegService.class);
		pss.regist(pos);
		
		//创建返回消息
		HeartBeatService hbs = Boot.appctx.getBean("heratBeatService",HeartBeatService.class);
		List<FPInfo> nozzles = hbs.getNozzleCode();
		byte[] bizHeaderArr = tpdu.getBody().getHeader().getOriginalContent();//业务数据包头//业务数据包头
		byte[] sysVer = new byte[1];
		sysVer[0] = Utils.getSysVersion();//服务器软件版本号
		byte[] sysTime = Converts.str2Bcd(Utils.getServerTime());//服务器时间
		byte[] nozzleCodes;
		StringBuffer sb = new StringBuffer();
		if(nozzles.size() != 0)
		{
			for(FPInfo info:nozzles){
			sb.append(info.getNozzleNumber()).append(",");
		}
		
			sb.deleteCharAt(sb.length()-1);
			nozzleCodes = new byte[sb.length()];//油枪编号
		}
		else
		{
			nozzleCodes = new byte[0];
		}
		for(int i=0;i<nozzleCodes.length;i++){
			nozzleCodes[i] = (byte) sb.charAt(i);
		}
		
		ByteBuf b = Unpooled.buffer(bizHeaderArr.length + sysVer.length + sysTime.length + nozzleCodes.length);
		b.writeBytes(bizHeaderArr);
		b.writeBytes(sysVer);
		b.writeBytes(sysTime);
		b.writeBytes(nozzleCodes);
		
		byte[] dataArr = b.array();
		b.release();

		this.channel.writeAndFlush(dataArr);

	}
}
