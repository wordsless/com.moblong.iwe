package com.moblong.iwe;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.moblong.flipped.model.Whistle;

public interface IImmediatelyWhistlerEngine {

	void register(IReciveListener listener);

	void unregister(IReciveListener listener);

	void send(Whistle<?> whistle);

	void init(String id, String host, int port) throws IOException, TimeoutException;

	void startup();

	void shutdown();

	boolean isAlive();

}
