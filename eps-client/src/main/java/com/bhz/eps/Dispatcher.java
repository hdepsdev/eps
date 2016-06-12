package com.bhz.eps;

import io.netty.channel.Channel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.bhz.eps.pdu.TPDU;
import com.bhz.eps.processor.BizProcessor;
import com.bhz.eps.util.ClassUtil;

/**
 * 抽象了分发器
 * 多线程执行
 * 某个消息对象msgObject指定某个业务逻辑对象processor
 * submit到线程池中
 * @author yaoh
 *
 */

public class Dispatcher {
	
	private static final int MAX_THREAD_NUM = 50;
	
	private static ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREAD_NUM);
	
	public static void submit(Channel channel, Object msgObject) throws InstantiationException, IllegalAccessException {
		
		TPDU tpdu = (TPDU) msgObject;
		Class<?> processorClass = ClassUtil.getProcessorClassByType(tpdu.getBody().getHeader().getCmd().shortValue());
		BizProcessor processor = (BizProcessor) processorClass.newInstance();
		processor.setChannel(channel);
		processor.setMsgObject(msgObject);
		
		executorService.submit(processor);
	}
}