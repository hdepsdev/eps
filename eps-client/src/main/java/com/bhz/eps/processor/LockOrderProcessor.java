package com.bhz.eps.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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


/**
 * 锁定待支付订单
 * @author txy
 *
 */
@BizProcessorSpec(msgType=BizMessageType.LOCK_ORDER)
public class LockOrderProcessor extends BizProcessor{
	 private static final Logger logger = LogManager.getLogger(LockOrderProcessor.class);
	
	@Override
	public void process() {
		TPDU tpdu = (TPDU)this.msgObject;
		byte[] cnt = tpdu.getBody().getData().getContent();
		byte[] fpNumber = new byte[4];
		System.arraycopy(cnt, 0, fpNumber, 0, fpNumber.length);
		byte[] realPayInfoNum = new byte[11];
		System.arraycopy(cnt, 4, realPayInfoNum, 0, realPayInfoNum.length);//支付流水号：BCD 11
		
		 byte re = 0x7F;

	        String bposIp = Utils.systemConfiguration.getProperty("bpos.server.ip");
	        String bposPort = Utils.systemConfiguration.getProperty("bpos.server.port");
	        String version = Utils.systemConfiguration.getProperty("hht.protocol.version");
	        String terminal = Utils.systemConfiguration.getProperty("eps.client.terminal");

	        //创建发送给bpos的消息
	        ByteBuf hhtByte = Unpooled.buffer(27);
	        Utils.setHeaderForHHT(hhtByte, Integer.toHexString(21), version, terminal, "4");//Lock/Unlock delivery request的messageType为4
	        try {
	            hhtByte.writeByte(0x30);//LOCK Flag
                hhtByte.writeBytes(Converts.addZeroInLeft2Str(Integer.toString(Converts.bytes2Int(fpNumber)), 2).getBytes("utf-8"));//油枪编号
                hhtByte.writeBytes(Converts.addZeroInLeft2Str(Integer.valueOf(Converts.bcd2Str(realPayInfoNum)).toString(), 9).getBytes("utf-8"));//流水id
	        } catch (UnsupportedEncodingException e) {
	            logger.error("", e);
	        }

	        //发送消息，并记录结果
	        Socket socket = null;
	        OutputStream out = null;
	        InputStream in = null;
	        try {
	            socket = new Socket(bposIp, Integer.parseInt(bposPort));
	            out = socket.getOutputStream();
	            in = socket.getInputStream();
	            out.write(hhtByte.array());
	            byte[] bytes = new byte[19];//包头
	            in.read(bytes);
	            int bi = in.read();//bpos返回成功标识，0x31为成功
	            if (bi != 0x31) {
	                bytes = new byte[4];//Result Code
	                in.read(bytes);
	                int resultCode = Converts.bytes2Int(bytes);
	                if (resultCode == 2003 || resultCode == 2004) {
						re = 0x01;
					}
	                else {
	                	re = 0x7F;
	                	logger.error("HHT LOCK Delivery Request failed");
					}
	            }
	            else{
	            	re = 0x00;
	            }
	        } catch (IOException e) {
	            logger.error("", e);
	        } finally {
	            if (socket != null) {
	                try {
	                    socket.close();
	                } catch (IOException e) {
	                    logger.error("", e);
	                }
	            }
	            if (out != null) {
	                try {
	                    out.close();
	                } catch (IOException e) {
	                    logger.error("", e);
	                }
	            }
	            if (in != null) {
	                try {
	                    in.close();
	                } catch (IOException e) {
	                    logger.error("", e);
	                }
	            }
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
