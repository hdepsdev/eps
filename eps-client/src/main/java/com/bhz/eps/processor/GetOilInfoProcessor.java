package com.bhz.eps.processor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.List;

import com.bhz.eps.Boot;
import com.bhz.eps.annotation.BizProcessorSpec;
import com.bhz.eps.entity.OilInfo;
import com.bhz.eps.msg.BizMessageType;
import com.bhz.eps.pdu.TPDU;
import com.bhz.eps.service.OilInfoService;
import com.bhz.eps.util.Converts;
import com.bhz.eps.util.Utils;
import com.sun.tools.javac.util.Convert;

/**
 * 返回油站班次信息
 * @author yangxb
 *
 */
@BizProcessorSpec(msgType=BizMessageType.GET_OIL_INFO)
public class GetOilInfoProcessor extends BizProcessor{

	@Override
	public void process() {
		TPDU tpdu = (TPDU)this.msgObject;
		
		//创建返回消息
		OilInfoService oilInfoSrv = Boot.appctx.getBean("oilInfoService", OilInfoService.class);
		List<OilInfo> oilTypeList =  null;
		List<OilInfo> oilCategoryList = null;
		oilTypeList = oilInfoSrv.getOilTypeList();
		oilCategoryList = oilInfoSrv.getOilCategoryList();
		
		byte[] bizHeaderArr = tpdu.getBody().getHeader().getOriginalContent();//业务数据包头
		byte[] oilTypeCount = Converts.long2U32(oilTypeList.size());
		byte[] oilTypeContent = new byte[oilTypeList.size()*OilInfo.SIZE];
		Utils.initByteArray(oilTypeContent, (byte) 0x20);
		for(int i = 0; i < oilTypeList.size(); i++){
			oilTypeContent[i*OilInfo.SIZE] = oilTypeList.get(i).getOilId();
			byte[] oilTypeNameAry = Convert.string2utf(oilTypeList.get(i).getOilName());
			System.arraycopy(oilTypeNameAry, 0, oilTypeContent, i*OilInfo.SIZE + 1, oilTypeNameAry.length);
		}
		
		byte[] oilCategoryCount = Converts.long2U32(oilCategoryList.size());
		byte[] oilCategoryContent = new byte[oilCategoryList.size()*OilInfo.SIZE];
		Utils.initByteArray(oilCategoryContent, (byte) 0x20);
		for(int i = 0; i < oilCategoryList.size(); i++){
			oilCategoryContent[i*OilInfo.SIZE] = oilCategoryList.get(i).getOilId();
			byte[] oilCategoryNameAry = Convert.string2utf(oilCategoryList.get(i).getOilName());
			System.arraycopy(oilCategoryNameAry, 0, oilCategoryContent, i*OilInfo.SIZE + 1, oilCategoryNameAry.length);
		}
		
		ByteBuf b = Unpooled.buffer(bizHeaderArr.length + oilTypeCount.length + oilTypeContent.length
				+ oilCategoryCount.length + oilCategoryContent.length);
		b.writeBytes(bizHeaderArr);
		b.writeBytes(oilTypeCount);
		b.writeBytes(oilTypeContent);
		b.writeBytes(oilCategoryCount);
		b.writeBytes(oilCategoryContent);
		
		byte[] dataArr = b.array();		
		b.release();
		//传递封装好的业务返回消息给Encoder
		this.channel.writeAndFlush(dataArr);
		
	}

}
