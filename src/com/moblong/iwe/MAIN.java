package com.moblong.iwe;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.moblong.flipped.model.Whistle;

public final class MAIN {

	public static void main(String[] args) {
		
		IReciveListener listener = new IReciveListener() {

			@Override
			public boolean recived(Whistle<String> whistle) {
				return false;
			}
			
		};
		
		ImmediatelyWhistlerEngine engine = new ImmediatelyWhistlerEngine();
		try {
			engine.init(UUID.randomUUID().toString().replace("-", ""), "push.tlthsc.com", 5479);
			engine.startup(listener);
			while(engine.isAlive()) {
				Thread.sleep(500);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
