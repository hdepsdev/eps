package com.bhz.eps.test;



import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.bhz.eps.msg.ManageMessageProto;
import com.bhz.eps.msg.ManageMessageProto.MsgType;
import com.google.protobuf.ByteString;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

public class EPSManagerEMU {
	public void connect(String host, int port) throws Exception{
		EventLoopGroup worker = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap();
		try{
			b.group(worker)
			.channel(NioSocketChannel.class)
			.option(ChannelOption.SO_KEEPALIVE, true)
			.handler(new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel ch) throws Exception {
					ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
					ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
					ch.pipeline().addLast(new ProtobufEncoder());
					ch.pipeline().addLast(new ProtobufDecoder(ManageMessageProto.ManageMessage.getDefaultInstance()));
					ch.pipeline().addLast(new ManagerClientHandler());
				}
				
			});
			ChannelFuture f = b.connect(host, port).sync();
			f.channel().closeFuture().sync();
		}finally{
			worker.shutdownGracefully();
		}
	}
	public static void main(String[] args) throws Exception{
			final EPSManagerEMU emu = new EPSManagerEMU();
			ExecutorService es = Executors.newFixedThreadPool(50);
			for(int i=0;i<50;i++){
				System.out.println("idx: [ " + i + " ]");
				es.submit(new Runnable(){
					public void run(){
						try {
							emu.connect("127.0.0.1", 30303);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
			es.shutdown();
	}
}

class ManagerClientHandler extends SimpleChannelInboundHandler<ManageMessageProto.ManageMessage>{

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, ManageMessageProto.ManageMessage msg) throws Exception {
		System.out.println("Message received.");
		System.out.println(msg.getResponse().getResult());
		ctx.close();
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
//		System.out.println("KKKKK");
		ManageMessageProto.ManageMessage.Builder mb = ManageMessageProto.ManageMessage.newBuilder();
		ManageMessageProto.Request.Builder rb = ManageMessageProto.Request.newBuilder();
		ManageMessageProto.LoginRequest.Builder lrb = ManageMessageProto.LoginRequest.newBuilder();
		lrb.setUsername("yaoh");
		lrb.setPassword(ByteString.copyFrom("cc".getBytes()));
		rb.setLoginRequest(lrb.build());
		mb.setRequest(rb.build());
		mb.setType(MsgType.Login_Request);
		mb.setSeqence(123);
		ManageMessageProto.ManageMessage mm = mb.build();
		ctx.writeAndFlush(mm);
	}
	
}
