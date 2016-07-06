package com.bhz.eps;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bhz.eps.codec.TPDUDecoder;
import com.bhz.eps.codec.TPDUEncoder;
import com.bhz.eps.util.ClassUtil;
import com.bhz.eps.util.Utils;

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
	
	private static final Logger logger = LogManager.getLogger(Boot.class);
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
			
			ChannelFuture cf = sb.bind(Integer.parseInt(Utils.systemConfiguration.getProperty("eps.client.port"))).sync();
			cf.addListener(new ChannelFutureListener(){

				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					logger.info("Server is started and listening on port " + Utils.systemConfiguration.getProperty("eps.client.port"));
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
		if(Utils.systemConfiguration.getProperty("eps.client.data.upload.need").equalsIgnoreCase("true")){
			startEPSManager();
		}
		Boot b = new Boot();
		b.start();
	}
	
	private static void startEPSManager(){
		ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
		ses.scheduleAtFixedRate(new RunEPSManager(), 0, 
				Integer.parseInt(Utils.systemConfiguration.getProperty("eps.client.data.upload.interval")), 
				TimeUnit.SECONDS);
	}
}

class RunEPSManager implements Runnable{
	private static final Logger logger = LogManager.getLogger(RunEPSManager.class);
	@Override
	public void run() {
		EPSClientDataManager ecdm = EPSClientDataManager.getInstance(Utils.systemConfiguration.getProperty("eps.server.ip"), 
				Integer.parseInt(Utils.systemConfiguration.getProperty("eps.server.port")));
		try {
			ecdm.submitTask();
		} catch (Exception e) {
			if(e instanceof java.net.ConnectException){
				logger.error(e.getMessage());
//				System.out.println(e.getMessage());
			}else{
				e.printStackTrace();
			}
		}
	}
	
}
