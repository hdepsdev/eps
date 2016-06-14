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
//		ec.connect("localhost", 4088, msg1.generateMessage());
//		ec.connect("localhost", 4088, msg2.generateMessage());
		
		ExecutorService es = Executors.newFixedThreadPool(5);
		for(int i=0;i<5;i++){
//			es.submit(new NozzleOrderRun());//Nozzle Order
			es.submit(new PosConnectRun());//POS Connection
//			es.submit(new FPInfoRun());//FPInfo
		}
		
	}
}

class PosConnectRun implements Runnable{
	EPSPosEmulator ec = new EPSPosEmulator();

	@Override
	public void run() {
		PosConnectMessage msg = new PosConnectMessage();
		try {
			ec.connect("localhost", 4088, msg.generateMessage());
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
			ec.connect("localhost", 4088, msg2.generateMessage());
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
			ec.connect("localhost", 4088, msg.generateMessage());
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
