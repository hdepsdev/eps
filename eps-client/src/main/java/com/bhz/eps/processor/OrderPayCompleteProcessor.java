package com.bhz.eps.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import com.bhz.eps.Boot;
import com.bhz.eps.annotation.BizProcessorSpec;
import com.bhz.eps.entity.NozzleOrder;
import com.bhz.eps.msg.BizMessageType;
import com.bhz.eps.pdu.TPDU;
import com.bhz.eps.service.NozzleOrderService;
import com.bhz.eps.util.Converts;
import com.bhz.eps.util.Utils;

@BizProcessorSpec(msgType=BizMessageType.ORDER_PAY_COMPLETE)
public class OrderPayCompleteProcessor extends BizProcessor{

	@Override
	public void process() {
		TPDU tpdu = (TPDU)this.msgObject;
		byte[] cnt = tpdu.getBody().getData().getContent();
		byte[] nozzleCodeArr = new byte[4];//油枪编号：U32
		byte[] orderArr = new byte[11];//支付流水号：BCD 11
		byte[] amountArr = new byte[8];//折后金额 BCD 8
		
		int idx = 0;
		System.arraycopy(cnt, idx, nozzleCodeArr, 0, nozzleCodeArr.length);
		idx += nozzleCodeArr.length;
		System.arraycopy(cnt, idx, orderArr, 0, orderArr.length);
		idx += orderArr.length;
		byte payMethod = cnt[15];
		System.arraycopy(cnt, 32, amountArr, 0, amountArr.length);
		
        String bposIp = Utils.systemConfiguration.getProperty("bpos.server.ip");
        String bposPort = Utils.systemConfiguration.getProperty("bpos.server.port");
        String version = Utils.systemConfiguration.getProperty("hht.protocol.version");
        String terminal = Utils.systemConfiguration.getProperty("eps.client.terminal");
		
		byte re = 0x01;
        Socket socket = null;
        OutputStream out = null;
        InputStream in = null;
		try{
/*			NozzleOrderService nos = Boot.appctx.getBean(
					"nozzleOrderService",NozzleOrderService.class);
			nos.updateOrderStatus(NozzleOrder.ORDER_PAYED, 
					Integer.toString(Converts.bytes2Int(nozzleCodeArr)), Converts.bcd2Str(orderArr));*/
	        //创建发送给bpos的消息
			//由于EPS-Client一次只能锁定一条支付信息，且支付成功后不会返回相关终端信息，所以支付完成的saleitem只包含2个
			//hht body
			ByteBuf hht = Unpooled.buffer(115);
			byte[] transactionNo = Converts.bcd2Str(orderArr).substring(13).getBytes("utf-8");
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			byte[] transactionDate = df.format(new Date()).getBytes("utf-8");
			byte[] totalValue = Converts.bcd2Str(amountArr).substring(2).getBytes("utf-8");
			byte[] totalTax = new String("00000000000000").getBytes("utf-8");
			byte[] noOfSaleItems = new String("02").getBytes("utf-8");
			//fuel delivery sale item
			byte[] deliverySaleItemTypeId = new String("01").getBytes("utf-8");
			String nozzleCode = Integer.toString(Converts.bytesToInt(nozzleCodeArr));
			byte[] pumpNo; 
			if (nozzleCode.length() < 2)
				pumpNo = ("0" + nozzleCode).getBytes("utf-8");
			else if (nozzleCode.length() == 2)
				pumpNo = nozzleCode.getBytes("utf-8");
			else
				pumpNo = nozzleCode.substring(nozzleCode.length() - 2).getBytes("utf-8");
			byte[] deliveryId = Converts.bcd2Str(orderArr).substring(13).getBytes("utf-8");
			byte[] taxId = new String("00").getBytes("utf-8");
			byte[] taxAmount = new String("00000000000000").getBytes("utf-8");
			//mop sale item
			byte[] mopSaleItem =  new String("02").getBytes("utf-8");
			String strMopId = Integer.toString(payMethod);
			byte[] mopId;
			if (strMopId.length() < 2)
				mopId = ("0" + strMopId).getBytes("utf-8");
			else if (strMopId.length() == 2)
				mopId = strMopId.getBytes("utf-8");
			else
				mopId = strMopId.substring(strMopId.length() - 2).getBytes("utf-8");
			byte[] mopTenderAmount = Converts.bcd2Str(amountArr).substring(2).getBytes("utf-8");
			//hht deader
	        Utils.setHeaderForHHT(hht, Integer.toHexString(109), version, terminal, "5");
	        //hht body
	        hht.writeBytes(transactionNo);
	        hht.writeBytes(transactionDate);
	        hht.writeBytes(totalValue);
	        hht.writeBytes(totalTax);
	        hht.writeBytes(noOfSaleItems);
	        //fuel delivery sale item
	        hht.writeBytes(deliverySaleItemTypeId);
	        hht.writeBytes(pumpNo);
	        hht.writeBytes(deliveryId);
	        hht.writeBytes(taxId);
	        hht.writeBytes(taxAmount);
	        //mop sale item
	        hht.writeBytes(mopSaleItem);
	        hht.writeBytes(mopId);
	        hht.writeBytes(mopTenderAmount);
	        //->BasePOS
            socket = new Socket(bposIp, Integer.parseInt(bposPort));
            out = socket.getOutputStream();
            in = socket.getInputStream();
            out.write(hht.array());
            byte[] bytes = new byte[15];//头
            in.read(bytes);
            byte[] msgtype = new byte[4];
            in.read(msgtype);
            String strMsgtype = new String(msgtype, "utf-8");
            if (strMsgtype.equals("0005"))
            {
            	int bi = in.read();//成功标识
            	if (bi == 0x31)
            		re = 0x00;    			
            }            
		}
		catch(Exception e){

		}
		finally{
			try {
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				out.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				in.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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