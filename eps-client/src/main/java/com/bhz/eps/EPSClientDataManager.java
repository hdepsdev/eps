package com.bhz.eps;

import java.math.BigDecimal;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bhz.eps.entity.NozzleOrder;
import com.bhz.eps.msg.PaymentReqProto;
import com.bhz.eps.msg.PaymentRespProto;
import com.bhz.eps.msg.PaymentRespProto.PaymentResp;
import com.bhz.eps.service.NozzleOrderService;
import com.bhz.eps.util.Utils;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

public class EPSClientDataManager {
	private static final Logger logger = LogManager.getLogger(EPSClientDataManager.class);
	private String hostIP;
	private int hostPort;
	private static EPSClientDataManager manager;
	
	private EPSClientDataManager(String hostIP,int hostPort){
		this.hostIP = hostIP;
		this.hostPort = hostPort;
		logger.trace("Initialize EPS Client Data Manager.");
	}
	
	public static EPSClientDataManager getInstance(String hostIP,int hostPort){
		if(manager != null){
			return manager;
		}
		manager = new EPSClientDataManager(hostIP, hostPort);
		return manager;
	}
	
	public void submitTransData() throws Exception{
		Bootstrap boot = new Bootstrap();
		EventLoopGroup worker = new NioEventLoopGroup();
		try{
			boot.group(worker).option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Integer.parseInt(Utils.systemConfiguration.getProperty("eps.client.data.upload.timeout")))
				.channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>(){	
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
						ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
						ch.pipeline().addLast(new ProtobufEncoder());
						ch.pipeline().addLast(new ProtobufDecoder(PaymentRespProto.PaymentResp.getDefaultInstance()));
						ch.pipeline().addLast(new EPSClientHandler());
					}
					
				});
			ChannelFuture cf = boot.connect(this.hostIP, this.hostPort).sync();
			cf.addListener(new ChannelFutureListener() {
				
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					logger.debug("Established connection to " + hostIP + " on port " + hostPort);
				}
				
			});
			cf.channel().closeFuture().sync();
		}finally{
			worker.shutdownGracefully();
		}
	}
}

class EPSClientHandler extends SimpleChannelInboundHandler<PaymentRespProto.PaymentResp>{
	private static final Logger logger = LogManager.getLogger(EPSClientHandler.class);
	@Override
	protected void messageReceived(ChannelHandlerContext ctx, PaymentResp msg) throws Exception {
		if(msg.getResult().getResultCode().equals("1")){
			NozzleOrderService nos = Boot.appctx.getBean("nozzleOrderService",NozzleOrderService.class);
			nos.updateUploadStatus(msg.getWorkOrder(), NozzleOrder.UPLOADED, Utils.getServerTime());
			logger.debug("Processed work order [ " + msg.getWorkOrder() + " ] ");
		}else{
			logger.error(msg.getResult().getErrorCause());
		}
	}
	
	
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		NozzleOrderService nos = Boot.appctx.getBean("nozzleOrderService",NozzleOrderService.class);
		List<NozzleOrder> orderList = nos.queryUnUploadOrders();
		if(orderList == null || orderList.size()==0){
			logger.debug("No un_upload nozzle order found.");
			ctx.close();
		}else{
			for(NozzleOrder order:orderList){
				PaymentReqProto.PaymentReq.Builder builder = PaymentReqProto.PaymentReq.newBuilder();
				builder.setStationCode(order.getStationCode());
				builder.setNozzleNumber(Integer.parseInt(order.getNozzleNumber()));
				builder.setWorkOrder(order.getWorkOrder());
				builder.setPaymentMethod(1);
				builder.setPreferentialMethod(1);
				builder.setPoints("");
				BigDecimal totalAmount = (new BigDecimal(order.getPrice())).multiply(order.getVolumeConsume());
				builder.setReceivables(totalAmount.divide(new BigDecimal(100)).toString());
				builder.setProceeds(totalAmount.divide(new BigDecimal(100)).toString());
				PaymentReqProto.PaymentReq req = builder.build();
				ctx.write(req);
				logger.debug("Prepare to send NozzleOrder: [ id=" + order.getWorkOrder() + " ]");
			}
			logger.debug("Upload Nozzle orders.");
			ctx.flush();
		}
	}
}
