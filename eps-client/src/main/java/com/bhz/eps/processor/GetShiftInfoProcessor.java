package com.bhz.eps.processor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.List;

import com.bhz.eps.Boot;
import com.bhz.eps.annotation.BizProcessorSpec;
import com.bhz.eps.entity.ShiftInfo;
import com.bhz.eps.msg.BizMessageType;
import com.bhz.eps.pdu.TPDU;
import com.bhz.eps.service.GetShiftlistService;
import com.bhz.eps.util.Converts;
import com.bhz.eps.util.Utils;
import com.sun.tools.javac.util.Convert;

/**
 * 返回油站班次信息
 * @author yangxb
 *
 */
@BizProcessorSpec(msgType=BizMessageType.GET_SHIFT_INFO)
public class GetShiftInfoProcessor extends BizProcessor{

	@Override
	public void process() {
		TPDU tpdu = (TPDU)this.msgObject;
		
		//创建返回消息
		GetShiftlistService shiftSrv = Boot.appctx.getBean("getShiftlistService",GetShiftlistService.class);
		List<ShiftInfo> shiftInfolist = shiftSrv.getShiftInfos();
		
		byte[] bizHeaderArr = tpdu.getBody().getHeader().getOriginalContent();//业务数据包头
		byte[] shiftCount = Converts.long2U32(shiftInfolist.size());
		byte[] shiftInfoContent = new byte[shiftInfolist.size()*ShiftInfo.SIZE];
		Utils.initByteArray(shiftInfoContent, (byte) 0x20);
		for(int i = 0; i < shiftInfolist.size(); i++){
			shiftInfoContent[i*ShiftInfo.SIZE] = shiftInfolist.get(i).getShiftId();
			byte[] ShiftNameAry = Convert.string2utf(shiftInfolist.get(i).getShiftName());
			System.arraycopy(ShiftNameAry, 0, shiftInfoContent, i*ShiftInfo.SIZE + 1, ShiftNameAry.length);
		}
		
		ByteBuf b = Unpooled.buffer(bizHeaderArr.length + shiftCount.length + shiftInfoContent.length);
		b.writeBytes(bizHeaderArr);
		b.writeBytes(shiftCount);
		b.writeBytes(shiftInfoContent);
		
		byte[] dataArr = b.array();		
		b.release();
		//传递封装好的业务返回消息给Encoder
		this.channel.writeAndFlush(dataArr);
	}

}
