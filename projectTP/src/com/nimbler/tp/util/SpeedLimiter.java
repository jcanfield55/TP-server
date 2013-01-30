package com.nimbler.tp.util;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
/**
 * 
 * @author nirmal
 *
 */
public class SpeedLimiter extends Semaphore{

	int requestLimit;
	TimeUnit timeUnit;
	private long waitTime;

	public SpeedLimiter(int request, int timePeriod,TimeUnit timeUnit) {
		super(request);
		new ResourceProvider().start();
		requestLimit = request;
		waitTime = TimeUnit.MILLISECONDS.convert(timePeriod, timeUnit);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -5583176293087248547L;

	/**
	 * The Class Filler.
	 */
	private final class ResourceProvider extends Thread{
		@Override
		public void run() {
			while (true) {
				try {
					sleep(waitTime);
					drainPermits();
					release(requestLimit);
				} catch (Exception e) {}

			}
		}
	}
	public static void main(String[] args) {
		try {
			SpeedLimiter limiter = new SpeedLimiter(3,1000,TimeUnit.MILLISECONDS);
			long last = System.currentTimeMillis();
			for (int i = 0; i < 2000; i++) {
				boolean o = limiter.tryAcquire(10,TimeUnit.SECONDS);
				if(!o)
					System.out.println("fail.....");
				long newL = System.currentTimeMillis();
				System.out.println(o+" - "+(newL-last));
				last=System.currentTimeMillis();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
