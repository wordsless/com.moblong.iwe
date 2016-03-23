package com.moblong.iwe;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.moblong.flipped.model.Whistle;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;

public final class ImmediatelyWhistlerEngine {

	private boolean alive = false;

	private IReciveListener observer;
	
	private BlockingQueue<Whistle<?>> queue;

	private IDetegater<IReciveListener> starter, closer; 
	
	private Thread reciver, sender;

	public void send(final Whistle<?> whistle) {
		try {
			queue.put(whistle);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void init(final String id, final String host, final int port) throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setAutomaticRecoveryEnabled(false);
		factory.setHost(host);
		factory.setPort(port);
		queue = new ArrayBlockingQueue<Whistle<?>>(128);
		
		final Connection con = factory.newConnection();
		final Gson gson = new GsonBuilder()
				.setDateFormat("yyyy-MM-dd HH:mm:ss")
				.create();
		
		reciver = new Thread(new Runnable() {

			@Override
			public void run() {
				Channel channel = null;
				try {
					channel = con.createChannel();
					QueueingConsumer consumer = new QueueingConsumer(channel);
					channel.queueDeclare(id, false, false, false, null);
					channel.basicConsume(id, true, consumer);
					Gson gson = new Gson();
					while (alive) {
						Delivery delivery = consumer.nextDelivery();
						byte[] body = delivery.getBody();
						String msg = new String(body, "UTF-8");
						Whistle<String> whistle = gson.fromJson(msg, new TypeToken<Whistle<String>>() {}.getType());
						if(observer != null)
							observer.recived(whistle);
						Thread.yield();
					}
					channel.close();
					channel = null;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ShutdownSignalException e) {
					e.printStackTrace();
				} catch (ConsumerCancelledException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				}
			}
			
		});
		
		sender = new Thread(new Runnable() {

			@Override
			public void run() {
				Channel channel = null;
				try {
					channel = con.createChannel();
					while(alive) {
						Whistle<?> whistle = queue.poll();
						if(whistle != null) {
							channel.queueDeclare(whistle.getTarget(), false, false, false, null);
							channel.basicPublish("", whistle.getTarget(), MessageProperties.TEXT_PLAIN, gson.toJson(whistle).getBytes("UTF-8"));
							channel.waitForConfirms(1000L);
						}
						Thread.yield();
					}
					channel.close();
					channel = null;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				}
			}
			
		});
		
		starter = new IDetegater<IReciveListener>() {

			@Override
			public void detegate(IReciveListener observer) {
				
				alive = true;
				
				ImmediatelyWhistlerEngine.this.observer = observer;
				
				if(reciver != null && !reciver.isAlive()) {
					reciver.start();
				}
				
				if(sender != null && !sender.isAlive()) {
					sender.start();
				}
			}
			
		};
		
		closer = new IDetegater<IReciveListener>() {

			@Override
			public void detegate(IReciveListener observer) {
				
				alive = false;
				
				ImmediatelyWhistlerEngine.this.observer = null;
				
				while(reciver != null && reciver.isAlive()) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				while(sender != null && sender.isAlive()) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			}
			
		};
	}
	
	public void startup(IReciveListener observer) {
		starter.detegate(observer);
	}
	
	public void shutdown() {
		closer.detegate(this.observer);
	}
	
	public final boolean isAlive() {
		return alive;
	}
}
