package com.bhz.eps.processor;

import com.bhz.eps.Boot;
import com.bhz.eps.annotation.BizProcessorSpec;
import com.bhz.eps.entity.NozzleOrder;
import com.bhz.eps.entity.OilInfo;
import com.bhz.eps.msg.BizMessageType;
import com.bhz.eps.pdu.TPDU;
import com.bhz.eps.service.NozzleOrderService;
import com.bhz.eps.service.OilInfoService;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取油枪待支付列表
 *
 * @author txy
 */
@BizProcessorSpec(msgType = BizMessageType.FP_ORDERLIST)
public class FpOrderlistProcessor extends BizProcessor {
    private static final Logger logger = LogManager.getLogger(FpOrderlistProcessor.class);

    @Override
    public void process() {
        NozzleOrderService nos = Boot.appctx.getBean("nozzleOrderService", NozzleOrderService.class);
        OilInfoService ois = Boot.appctx.getBean("oilInfoService", OilInfoService.class);
        List<OilInfo> oilCategoryList = ois.getOilCategoryList();
        List<OilInfo> oilTypeList = ois.getOilTypeList();

        TPDU tpdu = (TPDU) this.msgObject;
        String stationCode = tpdu.getBody().getHeader().getStationID();
        byte[] cnt = tpdu.getBody().getData().getContent();
        byte[] fpNumber = new byte[4];
        System.arraycopy(cnt, 0, fpNumber, 0, fpNumber.length);

        String bposIp = Utils.systemConfiguration.getProperty("bpos.server.ip");
        String bposPort = Utils.systemConfiguration.getProperty("bpos.server.port");
        String version = Utils.systemConfiguration.getProperty("hht.protocol.version");
        String terminal = Utils.systemConfiguration.getProperty("eps.client.terminal");

        //创建发送给bpos的消息
        ByteBuf hhtByte = Unpooled.buffer(17);
        Utils.setHeaderForHHT(hhtByte, Integer.toHexString(11), version, terminal, "2");//Fuel delivery details request的messageType为2
        try {
            hhtByte.writeBytes(Converts.addZeroInLeft2Str(Integer.toString(Converts.bytes2Int(fpNumber)), 2).getBytes("utf-8"));//油枪编号
        } catch (UnsupportedEncodingException e) {
            logger.error("", e);
        }

        List<fpEntity> list = new ArrayList<fpEntity>();//用于缓存bpos返回的数据

        //发送消息，并缓存数据
        Socket socket = null;
        OutputStream out = null;
        InputStream in = null;
        try {
            socket = new Socket(bposIp, Integer.parseInt(bposPort));
            out = socket.getOutputStream();
            in = socket.getInputStream();
            out.write(hhtByte.array());
            out.flush();
            byte[] bytes = new byte[15];//包头及其他与本功能无关的数据
            in.read(bytes);
            int bi = in.read();//bpos返回成功标识，0x31为成功
            bytes = new byte[4];//Result Code
            in.read(bytes);
            String resultCode = new String(bytes, "utf-8");
            bytes = new byte[2];//油枪编号
            in.read(bytes);
            String pumpNumber = new String(bytes, "utf-8");
            bytes = new byte[2];//Number Deliveries
            in.read(bytes);
            int dataSize = Integer.parseInt(new String(bytes, "utf-8"));
            if (bi != 0x31) {
                logger.error("HHT Fuel Delivery Request/Response failed. resultCode:" + resultCode + ",pumpNumber:" + pumpNumber);
            } else {
                for (int i = 0; i < dataSize; i++) {
                    fpEntity entity = new fpEntity();
                    list.add(entity);
                    bytes = new byte[9];//流水id
                    in.read(bytes);
                    entity.id = Converts.addZeroInLeft2Str(Long.valueOf(new String(bytes, "utf-8")).toString(), 22);
                    bytes = new byte[3];//油品编号长度
                    in.read(bytes);
                    bytes = new byte[Integer.parseInt(new String(bytes, "utf-8"))];//油品编号
                    in.read(bytes);
                    String oilNumber = new String(bytes, "utf-8");
                    bytes = new byte[3];//油品名称长度
                    in.read(bytes);
                    bytes = new byte[Integer.parseInt(new String(bytes, "utf-8"))];//油品名称
                    in.read(bytes);
                    String oilName = new String(bytes, "utf-8");
                    bytes = new byte[14];//金额总数，单位分
                    in.read(bytes);
                    entity.value = Long.valueOf(new String(bytes, "utf-8"));
                    in.read(bytes);//数量，单位毫升
                    String volumeStr = new String(bytes, "utf-8");
                    in.read(bytes);//单价
                    String priceStr = new String(bytes, "utf-8");
                    bytes = new byte[2];//锁
                    in.read(bytes);

                    //将获取到的待支付列表保存进数据库，用于记录与统计
                    pumpNumber = Integer.valueOf(pumpNumber).toString();
                    Byte oilType = null;
                    for (OilInfo oil : oilTypeList) {
                        if (oilName.contains(oil.getOilName())) {
                            oilType = oil.getOilId();
                            break;
                        }
                    }
                    if (oilType == null) {
                        oilType = 0;
                        logger.error("get oil Type error, oilName:" + oilName);
                    }
                    Byte oilCategory = null;
                    for (OilInfo oil : oilCategoryList) {
                        if (oilName.equals(oil.getOilName()) || oil.getOilName().startsWith(oilNumber)) {
                            oilCategory = oil.getOilId();
                            break;
                        }
                    }
                    if (oilCategory == null) {
                        oilCategory = 0;
                        logger.error("get oil Category error, oilName:" + oilName + ",oilNumber:" + oilNumber);
                    }
                    NozzleOrder no = new NozzleOrder();
                    no.setNozzleNumber(pumpNumber);
                    no.setWorkOrder(entity.id);
                    no.setOilType(oilType);
                    no.setOilCategory(oilCategory);
                    no.setPrice(Integer.parseInt(priceStr) / 10);
                    BigDecimal volume = new BigDecimal(volumeStr);
                    no.setVolumeConsume(volume.divide(new BigDecimal(1000), 2, RoundingMode.DOWN));
                    no.setOrderStatus(NozzleOrder.ORDER_NOT_PAYED);
                    no.setStationCode(stationCode);
                    no.setUploadStatus(NozzleOrder.UN_UPLOAD);
                    NozzleOrder check = nos.getOrderByNozzleNumberAndWorkOrder(pumpNumber, entity.id);
                    if (check == null) {
                        nos.addOrder(no);
                    }
                }
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
        byte[] countToPay = Converts.long2U32(list.size());//待支付信息数量

        ByteBuf b = Unpooled.buffer(bizHeaderArr.length + fpNumber.length + countToPay.length + list.size() * 15);
        b.writeBytes(bizHeaderArr);
        b.writeBytes(fpNumber);
        b.writeBytes(countToPay);

        for (fpEntity entity : list) {
            byte[] nozzlecode = Converts.str2Bcd(entity.id);
            b.writeBytes(nozzlecode);//支付流水号
            byte[] totalCount = Converts.long2U32(entity.value);
            b.writeBytes(totalCount);//支付金额
        }

        byte[] dataArr = b.array();
        b.release();

        this.channel.writeAndFlush(dataArr);
    }

    private class fpEntity {
        public String id;

        public Long value;
    }
}
