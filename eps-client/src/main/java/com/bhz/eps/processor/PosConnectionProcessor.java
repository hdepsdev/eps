package com.bhz.eps.processor;

import com.bhz.eps.Boot;
import com.bhz.eps.annotation.BizProcessorSpec;
import com.bhz.eps.entity.PosRegInfo;
import com.bhz.eps.msg.BizMessageType;
import com.bhz.eps.pdu.TPDU;
import com.bhz.eps.service.PosRegService;
import com.bhz.eps.util.Converts;
import com.bhz.eps.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * POS连接
 * @author yaoh
 *
 */
@BizProcessorSpec(msgType=BizMessageType.CONN)
public class PosConnectionProcessor extends BizProcessor {

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
		ByteBuf bizBuf = Unpooled.buffer();
		bizBuf.writeBytes(tpdu.getBody().getHeader().getOriginalContent());//业务数据包头
		byte[] sysVer = new byte[1];
		sysVer[0] = Utils.getSysVersion();
		bizBuf.writeBytes(sysVer);//服务器软件版本号
		byte[] sysTime = Converts.str2Bcd(Utils.getServerTime());
		bizBuf.writeBytes(sysTime);//服务器时间
		byte[] responseContent = new byte[bizBuf.readableBytes()];
		bizBuf.readBytes(responseContent);
		
		channel.writeAndFlush(responseContent);
	}
}
