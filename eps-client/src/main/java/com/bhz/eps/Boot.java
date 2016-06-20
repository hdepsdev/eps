package com.bhz.eps;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bhz.eps.codec.TPDUChecker;
import com.bhz.eps.codec.TPDUDecoder;
import com.bhz.eps.codec.TPDUEncoder;
import com.bhz.eps.util.ClassUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 服务端启动类
 * @author yaoh
 *
 */
public class Boot {
	public final static ApplicationContext appctx = new ClassPathXmlApplicationContext(new String[]{"conf/application-context.xml"});
	
	public void start() throws Exception {
		EventLoopGroup acceptor = new NioEventLoopGroup();
		EventLoopGroup worker = new NioEventLoopGroup();
		ServerBootstrap sb = new ServerBootstrap();
		try{
			sb.group(acceptor, worker)
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, 4096)
			.childOption(ChannelOption.TCP_NODELAY, true)
			.childHandler(new ChannelInitializer<SocketChannel>(){

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline pipeline = ch.pipeline();
					pipeline.addLast(new TPDUEncoder());
					pipeline.addLast("TPDULength",new LengthFieldBasedFrameDecoder(1280,2,4,4,0));
					pipeline.addLast(new TPDUDecoder());
//					pipeline.addLast(new TPDUChecker());
					pipeline.addLast("BizDispatcher",new BizHandlerDispatcher());
				}
				
			});
			
			ChannelFuture cf = sb.bind(9000).sync();
			cf.addListener(new ChannelFutureListener(){

				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					System.out.println("Server is started and listening on port 9000");
				}
				
			});
			cf.channel().closeFuture().sync();
		}finally{
			worker.shutdownGracefully();
			acceptor.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) throws Exception{
		ClassUtil.initTypeToProcessorClassMap();
		Boot b = new Boot();
		b.start();
	}
}
