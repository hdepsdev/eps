package com.bhz.eps.processor;

import com.bhz.eps.annotation.BizProcessorSpec;
import com.bhz.eps.msg.BizMessageType;
import com.bhz.eps.pdu.TPDU;
import com.bhz.eps.util.Converts;
import com.bhz.eps.util.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

@BizProcessorSpec(msgType = BizMessageType.UNLOCK_ORDER)
public class UnlockOrderProcessor extends BizProcessor {
    private static final Logger logger = LogManager.getLogger(UnlockOrderProcessor.class);

    @Override
    public void process() {
        TPDU tpdu = (TPDU) this.msgObject;
        byte[] cnt = tpdu.getBody().getData().getContent();
        byte[] nozzleCodeArr = new byte[4];//油枪编号：U32
        byte[] orderArr = new byte[11];//支付流水号：BCD

        int idx = 0;
        System.arraycopy(cnt, idx, nozzleCodeArr, 0, nozzleCodeArr.length);
        idx += nozzleCodeArr.length;
        System.arraycopy(cnt, idx, orderArr, 0, orderArr.length);
        idx += orderArr.length;

        byte re = 0x01;//成功标识，0x00为成功

        String bposIp = Utils.systemConfiguration.getProperty("bpos.server.ip");
        String bposPort = Utils.systemConfiguration.getProperty("bpos.server.port");
        String version = Utils.systemConfiguration.getProperty("hht.protocol.version");
        String terminal = Utils.systemConfiguration.getProperty("eps.client.terminal");

        //创建发送给bpos的消息
        ByteBuf hhtByte = Unpooled.buffer(27);
        Utils.setHeaderForHHT(hhtByte, Integer.toHexString(21), version, terminal, "4");//Lock/Unlock delivery request的messageType为4
        try {
            hhtByte.writeByte(0x31);//解锁标识
            hhtByte.writeBytes(Converts.addZeroInLeft2Str(Integer.toString(Converts.bytes2Int(nozzleCodeArr)), 2).getBytes("utf-8"));//油枪编号
            hhtByte.writeBytes(Converts.addZeroInLeft2Str(Integer.valueOf(Converts.bcd2Str(orderArr)).toString(), 9).getBytes("utf-8"));//流水id
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
            byte[] bytes = new byte[19];//包头及其他与本功能无关的数据
            in.read(bytes);
            int bi = in.read();//bpos返回成功标识，0x31为成功
            bytes = new byte[4];//Result Code
            in.read(bytes);
            String resultCode = new String(bytes, "utf-8");
            if (bi == 0x31) {
                re = 0x00;
            } else {
                logger.error("HHT HHT UNLOCK Delivery Request failed. resultCode:" + resultCode);
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
        byte[] bizHeaderArr = tpdu.getBody().getHeader().getOriginalContent();
        ByteBuf b = Unpooled.buffer(bizHeaderArr.length + 1);

        b.writeBytes(bizHeaderArr);
        b.writeByte(re);

        byte[] dataArr = b.array();
        b.release();

        this.channel.writeAndFlush(dataArr);
    }

}
