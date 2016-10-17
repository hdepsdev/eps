package com.bhz.eps.test;

import com.bhz.eps.util.Converts;
import com.bhz.eps.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangshuo on 2016/10/11.
 */
public class BPosEmulator {
    private static String REQUEST_RESULT = "0001";//对应LOCK_UNLOCK与COMPLETE的返回
    private static String DETAILS_REQUEST = "0002";//详情
    private static String DETAILS_RESPONSE = "0003";//对应DETAILS_REQUEST的返回
    private static String LOCK_UNLOCK = "0004";//锁
    private static String COMPLETE = "0005";//交易完成

    private static String BPOS_TERMINAL_NUMBER = "998";

    private static Map<String, Map<String, Delivery>> data = new HashMap<String, Map<String, Delivery>>();

    static {//初始化假数据
        Map<String, Delivery> map = new HashMap<String, Delivery>();
        data.put("01", map);
        map.put("000047506", new Delivery("000047506", "97#", "97# 汽油", "00000000028515", "00000000039770", "00000000007170"));
        map.put("000047507", new Delivery("000047507", "93#", "93# 汽油", "00000000025100", "00000000038620", "00000000006500"));
        map.put("000047003", new Delivery("000047003", "0#", "0# 汽油", "00000000005940", "00000000009180", "00000000006600"));

        map = new HashMap<String, Delivery>();
        data.put("02", map);

        map = new HashMap<String, Delivery>();
        data.put("14", map);
        map.put("000046231", new Delivery("000046231", "90#", "90# 汽油", "00000000023500", "00000000036150", "00000000006500"));
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8000);
            Socket socket = serverSocket.accept();
            OutputStream out = null;
            InputStream in = null;
            try {
                ByteArrayOutputStream content = new ByteArrayOutputStream();//缓存返回内容
                out = socket.getOutputStream();
                in = socket.getInputStream();
                byte[] bytes = new byte[6];//请求包头
                in.read(bytes);
                bytes = new byte[2];//请求版本号
                in.read(bytes);
                content.write(bytes);//向返回包中写入版本号
                content.write(BPOS_TERMINAL_NUMBER.getBytes("utf-8"));//向返回包中写入BPOS号
                bytes = new byte[3];//请求终端号
                in.read(bytes);
                String terminalNumber = new String(bytes, "utf-8");
                bytes = new byte[4];//请求messageType
                in.read(bytes);
                String messageType = new String(bytes, "utf-8");

                //根据messageType执行不同逻辑
                if (DETAILS_REQUEST.equals(messageType)) {//明细
                    content.write(DETAILS_RESPONSE.getBytes("utf-8"));//向返回包中写入返回的messageType
                    bytes = new byte[2];//油枪编号
                    in.read(bytes);
                    String pumpNumber = new String(bytes, "utf-8");

                    int successFlag = 0x31;//Success:0x31,Fail:0x30
                    String resultCode = "0000";//Result Code
                    Collection<Delivery> deliveries = null;
                    if (data.get(pumpNumber) == null) {
                        resultCode = "2000";//Unknown Pump Number
                        successFlag = 0x30;
                    } else if (data.get(pumpNumber).size() == 0) {
                        resultCode = "2001";//There are no deliveries on this pump
                        successFlag = 0x30;
                    } else {
                        deliveries = data.get(pumpNumber).values();
                    }
                    content.write(successFlag);
                    content.write(resultCode.getBytes("utf-8"));
                    content.write(bytes);//油枪编号
                    if (deliveries == null) {
                        content.write("00".getBytes("utf-8"));
                    } else {
                        content.write(Converts.addZeroInLeft2Str(Integer.toString(deliveries.size()), 2).getBytes("utf-8"));//Number Deliveries
                        for (Delivery d : deliveries) {
                            content.write(d.deliveryID.getBytes("utf-8"));
                            content.write(Converts.addZeroInLeft2Str(Integer.toString(d.gradeCode.getBytes("utf-8").length), 3).getBytes("utf-8"));//Grade Code Size
                            content.write(d.gradeCode.getBytes("utf-8"));
                            content.write(Converts.addZeroInLeft2Str(Integer.toString(d.gradeName.getBytes("utf-8").length), 3).getBytes("utf-8"));//Grade Name Size
                            content.write(d.gradeName.getBytes("utf-8"));
                            content.write(d.deliveryValue.getBytes("utf-8"));
                            content.write(d.deliveryVolume.getBytes("utf-8"));
                            content.write(d.deliveryPrice.getBytes("utf-8"));
                            if (d.lockBy == null) {
                                content.write("00".getBytes("utf-8"));//Lock Status
                            } else {
                                content.write("01".getBytes("utf-8"));//Lock Status
                            }
                        }
                    }

                } else if (LOCK_UNLOCK.equals(messageType)) {//锁与解锁
                    content.write(REQUEST_RESULT.getBytes("utf-8"));//向返回包中写入返回的messageType
                    content.write(messageType.getBytes("utf-8"));//向返回包中写入请求的messageType
                    int lock = in.read();//lock:0x30,unlock:0x31
                    bytes = new byte[2];//油枪编号
                    in.read(bytes);
                    String pumpNumber = new String(bytes, "utf-8");
                    bytes = new byte[9];//流水id
                    in.read(bytes);
                    String deliveryID = new String(bytes, "utf-8");

                    int successFlag = 0x31;//Success:0x31,Fail:0x30
                    String resultCode = "0000";//Result Code
                    if (data.get(pumpNumber) == null) {
                        resultCode = "2000";//Unknown Pump Number
                        successFlag = 0x30;
                    } else if (data.get(pumpNumber).get(deliveryID) == null) {
                        resultCode = "2002";//Unknown delivery ID or requested delivery is not on this pump
                        successFlag = 0x30;
                    } else {
                        Delivery d = data.get(pumpNumber).get(deliveryID);
                        if (lock == 0x30) {//锁逻辑
                            if (d.lockBy != null && !d.lockBy.equals(terminalNumber)) {
                                successFlag = 0x30;
                                resultCode = "2003";
                            } else {
                                d.lockBy = terminalNumber;
                            }
                        } else if (lock == 0x31) {//解锁逻辑
                            d.lockBy = null;
                        }
                    }
                    content.write(successFlag);
                    content.write(resultCode.getBytes("utf-8"));

                } else if (COMPLETE.equals(messageType)) {//交易完成
                    bytes = new byte[9];//Transaction Number
                    in.read(bytes);
                    String id = new String(bytes, "utf-8");
                    bytes = new byte[14];
                    in.read(bytes);//Transaction Date
                    String date = new String(bytes, "utf-8");
                    in.read(bytes);//Total Transaction Value
                    String value = new String(bytes, "utf-8");
                    in.read(bytes);//Total Transaction Tax
                    String tax = new String(bytes, "utf-8");
                    content.write(REQUEST_RESULT.getBytes("utf-8"));//向返回包中写入返回的messageType
                    content.write(messageType.getBytes("utf-8"));//向返回包中写入请求的messageType
                    content.write(0x31);//Success:0x31,Fail:0x30
                    content.write("0000".getBytes("utf-8"));//Result Code
                }

                //写入返回包
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                result.write(Utils.hexStringToByteAndAddZeroInLeftSide(Integer.toHexString(content.size()), 4));//长度
                result.write(new byte[]{0x00, 0x00});//保留字段
                result.write(content.toByteArray());//内容
                result.writeTo(out);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Delivery {
    Delivery(String deliveryID, String gradeCode, String gradeName, String deliveryValue, String deliveryVolume, String deliveryPrice) {
        this.deliveryID = deliveryID;
        this.gradeCode = gradeCode;
        this.gradeName = gradeName;
        this.deliveryValue = deliveryValue;
        this.deliveryVolume = deliveryVolume;
        this.deliveryPrice = deliveryPrice;
    }

    public String deliveryID;
    public String gradeCode;
    public String gradeName;
    public String deliveryValue;
    public String deliveryVolume;
    public String deliveryPrice;
    public String lockBy;//锁定该条记录的终端号，未锁定时为null
}
