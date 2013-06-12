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
	private long rampUptime = 0;	

	public SpeedLimiter(int request, int timePeriod,TimeUnit timeUnit) {
		super(0);
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
					drainPermits();
					if(rampUptime>0){
						release(requestLimit,rampUptime);
						long restTime = waitTime-rampUptime;
						if(restTime>0)
							sleep(restTime);
					}else{
						release(requestLimit);
						sleep(waitTime);
					}

				} catch (Exception e) {
					System.out.println("SpeedLimiter.ResourceProvider.run(): "+e);
				}

			}
		}
	}
	public synchronized void  release(int permits,long timeInMills) {
		int sleepTime = (int) (timeInMills/(permits));
		for (int i = 0; i < permits; i++) {
			release(1);
			ComUtils.sleep(sleepTime);
		}
	}

	public SpeedLimiter withRampUptime(long t) {
		this.rampUptime = t;
		return this;
	}

	public void setRampUptime(long rampUptime) {
		this.rampUptime = rampUptime;
	}

	public static void main(String[] args) {
		try {
			SpeedLimiter limiter = new SpeedLimiter(3,1000,TimeUnit.MILLISECONDS).withRampUptime(900);
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
