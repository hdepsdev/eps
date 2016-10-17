package com.bhz.eps.test;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.bhz.eps.util.Converts;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

public class EPSPosEmulator {
	public void connect(String host, int port, byte[] msg) throws Exception{
		EventLoopGroup worker = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap();
		try{
			b.group(worker)
			.channel(NioSocketChannel.class)
			.option(ChannelOption.SO_KEEPALIVE, true)
			.handler(new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel ch) throws Exception {
					ch.pipeline().addLast(new EPSClientHandler(msg));
				}
				
			});
			ChannelFuture f = b.connect(host, port).sync();
			f.channel().closeFuture().sync();
		}finally{
			worker.shutdownGracefully();
		}
	}
	public static void main(String[] args) throws Exception{
//		EPSPosEmulator ec = new EPSPosEmulator();
//		PosConnectMessage msg1 = new PosConnectMessage();
//		NozzleOrderMessage msg2 = new NozzleOrderMessage();
//		ec.connect("10.1.1.145", 9000, msg1.generateMessage());
//		ec.connect("10.1.1.145", 9000, msg2.generateMessage());
		
		ExecutorService es = Executors.newFixedThreadPool(5);
		for(int i=0;i<1;i++){
			//es.submit(new NozzleOrderRun());//Nozzle Order
			//es.submit(new PosConnectRun());//POS Connection
			//es.submit(new HeartBeatRun());//HeartBeat
			//es.submit(new FPInfoRun());//FPInfo
			//es.submit(new FpOrderlistRun());//FpOrderList
			//es.submit(new LockOrderRun());//LockOrder
			//es.submit(new OrderDetailRun());//GetDetailsOrder
			//es.submit(new PayCompleteRun());//PayComplete
			es.submit(new UnLockOrderRun());//UnLockOrder
		}
		
	}
}

class PosConnectRun implements Runnable{
	EPSPosEmulator ec = new EPSPosEmulator();

	@Override
	public void run() {
		PosConnectMessage msg = new PosConnectMessage();
		try {
			ec.connect("10.1.1.145", 9000, msg.generateMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class NozzleOrderRun implements Runnable{
	
	EPSPosEmulator ec = new EPSPosEmulator();

	@Override
	public void run() {
		NozzleOrderMessage msg2 = new NozzleOrderMessage();
		try {
			ec.connect("10.1.1.145", 9000, msg2.generateMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

class FPInfoRun implements Runnable{
	EPSPosEmulator ec = new EPSPosEmulator();
	@Override
	public void run() {
		FPInfoMessage msg = new FPInfoMessage();
		try {
			ec.connect("10.1.1.145", 9000, msg.generateMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
class HeartBeatRun implements Runnable{
	
	EPSPosEmulator ec = new EPSPosEmulator();

	@Override
	public void run() {
		HeartBeatMessage msg = new HeartBeatMessage();
		try {
			ec.connect("10.1.1.145", 9000, msg.generateMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
class LockOrderRun implements Runnable{
	EPSPosEmulator ec = new EPSPosEmulator();

	@Override
	public void run() {
		LockOrderMessage msg = new LockOrderMessage();
		try {
			ec.connect("localhost", 9000, msg.generateMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class FpOrderlistRun implements Runnable{
	EPSPosEmulator ec = new EPSPosEmulator();

	@Override
	public void run() {
		FpOrderlistMessage msg = new FpOrderlistMessage();
		try {
			ec.connect("localhost", 9000, msg.generateMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
class OrderDetailRun implements Runnable{
	EPSPosEmulator ec = new EPSPosEmulator();

	@Override
	public void run() {
		OrderDetailMessage msg = new OrderDetailMessage();
		try {
			ec.connect("localhost", 9000, msg.generateMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
class PayCompleteRun implements Runnable{
	EPSPosEmulator ec = new EPSPosEmulator();

	@Override
	public void run() {
		PayCompleteMessage msg = new PayCompleteMessage();
		try {
			ec.connect("localhost", 9000, msg.generateMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
class UnLockOrderRun implements Runnable{
	EPSPosEmulator ec = new EPSPosEmulator();

	@Override
	public void run() {
		UnlockOrderMessage msg = new UnlockOrderMessage();
		try {
			ec.connect("localhost", 9000, msg.generateMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
class EPSClientHandler extends SimpleChannelInboundHandler<ByteBuf>{
	
	final byte[] msg;
	
	public EPSClientHandler(byte[] msg) {
		super();
		this.msg = msg;
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
		byte[] result = new byte[msg.readableBytes()];
		msg.readBytes(result);
		System.out.println("Server back message: " + Converts.bytesToHexString(result));
		ctx.channel().close();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("Sending message to server: " + Converts.bytesToHexString(msg));
		ByteBuf bb = Unpooled.buffer(msg.length);
		bb.writeBytes(msg);
		ctx.writeAndFlush(bb);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}
	
}
