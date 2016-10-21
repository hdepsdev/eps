package com.bhz.eps;

import com.bhz.eps.util.Converts;
import com.bhz.eps.util.Utils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by wangshuo on 2016/10/11.
 */
public class BPosEmulator implements Runnable {
    private static String REQUEST_RESULT = "0001";//对应LOCK_UNLOCK与COMPLETE的返回
    private static String DETAILS_REQUEST = "0002";//详情
    private static String DETAILS_RESPONSE = "0003";//对应DETAILS_REQUEST的返回
    private static String LOCK_UNLOCK = "0004";//锁
    private static String COMPLETE = "0005";//交易完成

    private static String BPOS_TERMINAL_NUMBER = "998";

    private Map<String, Map<String, Delivery>> data = new HashMap<String, Map<String, Delivery>>();

    {//初始化数据
        Properties dataPro = new Properties();
        try {
            dataPro.load(new InputStreamReader(BPosEmulator.class.getClassLoader().getResourceAsStream("BPOS.data"), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("BPosEmulator init data error, BPOS.data file is not exists");
        }
        for (Object o : dataPro.keySet()) {
            String id = o.toString();
            if (id.length() != 9) {
                System.out.println("BPosEmulator init data error,id:" + id + " length:" + id.length() + " length is not 9");
            }
            String[] deliveryArr = dataPro.get(o).toString().split(",");
            if (deliveryArr.length != 6) {
                System.out.println("BPosEmulator init data error,id:" + id + " size:" + deliveryArr.length + " size is not 6");
                continue;
            } else {
                String code = null;
                String value = null;
                String length = null;
                if (deliveryArr[0].length() != 2) {
                    code = "Pump";
                    value = deliveryArr[0];
                    length = "2";
                } else if (deliveryArr[3].length() != 14) {
                    code = "Value";
                    value = deliveryArr[3];
                    length = "14";
                } else if (deliveryArr[4].length() != 14) {
                    code = "Volume";
                    value = deliveryArr[4];
                    length = "14";
                } else if (deliveryArr[5].length() != 14) {
                    code = "Price";
                    value = deliveryArr[5];
                    length = "14";
                }
                if (code != null && value != null && length != null) {
                    System.out.println("BPosEmulator init data error,id:" + id + " " + code + ":" + value + " length:" + value.length() + " length is not " + length);
                    continue;
                }
            }
            String pump = deliveryArr[0];
            Map<String, Delivery> map = data.get(pump);
            if (map == null) {
                map = new HashMap<String, Delivery>();
                data.put(pump, map);
            }
            map.put(id, new Delivery(id, deliveryArr[1], deliveryArr[2], deliveryArr[3], deliveryArr[4], deliveryArr[5]));
        }
        if (data.size() > 0) {
            System.out.println("BPosEmulator init data complete");
        }
    }

    public static void main(String[] args) {
        new BPosEmulator().run();
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(8000);
            while (true) {
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
                            System.out.println("detail request error, error code:2000, pumpNumber:" + pumpNumber);
                        } else if (data.get(pumpNumber).size() == 0) {
                            resultCode = "2001";//There are no deliveries on this pump
                            successFlag = 0x30;
                            System.out.println("detail request error, error code:2001, pumpNumber:" + pumpNumber);
                        } else {
                            deliveries = data.get(pumpNumber).values();
                            System.out.println("detail request success");
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
                            System.out.println("lock/unlock error, error code:2000, pumpNumber:" + pumpNumber);
                        } else if (data.get(pumpNumber).get(deliveryID) == null) {
                            resultCode = "2002";//Unknown delivery ID or requested delivery is not on this pump
                            successFlag = 0x30;
                            System.out.println("lock/unlock error, error code:2002, deliveryID:" + deliveryID);
                        } else {
                            Delivery d = data.get(pumpNumber).get(deliveryID);
                            if (lock == 0x30) {//锁逻辑
                                if (d.lockBy != null && !d.lockBy.equals(terminalNumber)) {
                                    successFlag = 0x30;
                                    resultCode = "2003";
                                    System.out.println("lock error, error code:2003, deliveryID:" + deliveryID + ", terminalNumber:" + terminalNumber);
                                } else {
                                    d.lockBy = terminalNumber;
                                    System.out.println("lock success");
                                }
                            } else if (lock == 0x31) {//解锁逻辑
                                d.lockBy = null;
                                System.out.println("unlock success");
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
                        bytes = new byte[2];
                        in.read(bytes);
                        String SaleItems = new String(bytes, "utf-8");
                        in.read(bytes);
                        String SaleType = new String(bytes, "utf-8");
                        in.read(bytes);
                        String pumpNo = new String(bytes, "utf-8");
                        bytes = new byte[9];
                        in.read(bytes);
                        String deliveryId = new String(bytes, "utf-8");

                        int successFlag = 0x31;//Success:0x31,Fail:0x30
                        String resultCode = "0000";//Result Code
                        if (data.get(pumpNo) == null) {
                            resultCode = "2000";//Unknown Pump Number
                            successFlag = 0x30;
                            System.out.println("complete error, error code:2000, pumpNumber:" + pumpNo);
                        } else if (data.get(pumpNo).get(deliveryId) == null) {
                            resultCode = "2002";//Unknown delivery ID or requested delivery is not on this pump
                            successFlag = 0x30;
                            System.out.println("complete error, error code:2002, deliveryID:" + deliveryId);
                        } else {
                            data.get(pumpNo).remove(deliveryId);
                            System.out.println("complete success");
                        }
                        content.write(REQUEST_RESULT.getBytes("utf-8"));//向返回包中写入返回的messageType
                        content.write(messageType.getBytes("utf-8"));//向返回包中写入请求的messageType
                        content.write(successFlag);//Success:0x31,Fail:0x30
                        content.write(resultCode.getBytes("utf-8"));//Result Code
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
            }
        } catch (IOException e) {
            e.printStackTrace();
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
}
