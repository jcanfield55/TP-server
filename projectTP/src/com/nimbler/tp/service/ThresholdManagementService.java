/*
 * @author nirmal
 */
package com.nimbler.tp.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import com.nimbler.tp.dataobject.ThresholdBoard;
import com.nimbler.tp.util.ComUtils;



/**
 * 
 * @author nirmal
 *
 */
@Scope("singleton")
@SuppressWarnings("unused")
public class ThresholdManagementService{
	public enum ADVISORY_SERVICE{
		CALTRAIN,
		BART
	}
	@Autowired	
	private LoggingService logger;

	private String loggerName  = ThresholdManagementService.class.getName();

	private int thresholdStart = 2;
	private int thresholdEnd = 6;
	private boolean locked = false;


	private volatile Map<ADVISORY_SERVICE, Vector<ThresholdBoard>> thresholdMap = new HashMap<ADVISORY_SERVICE, Vector<ThresholdBoard>>();

	/**
	 * Inits the.
	 */
	public void init() {		
		for (ADVISORY_SERVICE adServ : ADVISORY_SERVICE.values()) {
			Vector<ThresholdBoard> lstBoards =   new Vector<ThresholdBoard>();
			for (int i = thresholdStart; i <= thresholdEnd; i++) 
				lstBoards.add(new ThresholdBoard(i));				
			thresholdMap.put(adServ, lstBoards);
		}
	}

	/**
	 * Gets the threshold board.
	 *
	 * @param service the service
	 * @return the threshold board
	 */
	private Vector<ThresholdBoard> getThresholdBoard(ADVISORY_SERVICE service) {		
		return thresholdMap.get(service);		
	}

	/**
	 * Reset all counters.
	 */
	public void resetAllCounters() {
		while (locked) 	
			ComUtils.sleep(100);

		for (Map.Entry<ThresholdManagementService.ADVISORY_SERVICE, Vector<com.nimbler.tp.dataobject.ThresholdBoard>> entry : thresholdMap
				.entrySet()) {				
			Vector<ThresholdBoard> value = entry.getValue();
			for (ThresholdBoard thresholdBoard : value) {
				thresholdBoard.resetCounter();
			}
		}
	}

	public int getThresholdStart() {
		return thresholdStart;
	}

	public void setThresholdStart(int thresholdStart) {
		this.thresholdStart = thresholdStart;
	}

	public boolean isLocked() {
		return locked;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	public int getThresholdEnd() {
		return thresholdEnd;
	}

	public LoggingService getLogger() {
		return logger;
	}

	public void setLogger(LoggingService logger) {
		this.logger = logger;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	public void setThresholdEnd(int thresholdEnd) {
		this.thresholdEnd = thresholdEnd;
	}
	public static void main(String[] args) {
		System.out.println(ThresholdManagementService.class.getName());
	}
}

