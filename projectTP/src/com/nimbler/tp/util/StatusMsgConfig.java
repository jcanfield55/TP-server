package com.nimbler.tp.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
/**
 * 
 * @author nikunj
 *
 */
public class StatusMsgConfig {

	private static StatusMsgConfig config = new StatusMsgConfig();

	private Map<String, String> msgMap = null;

	private StatusMsgConfig() {

		Properties prop = new Properties();
		try {
			prop.load(StatusMsgConfig.class.getClassLoader().getResourceAsStream("conf/StatusMsgConfiguration.properties"));
			msgMap = new HashMap<String, String>((Map)prop);
		} catch (IOException e) {
			System.err.println("Error while loading status messages: "+e.getMessage()); 
		} 
	}
	/**
	 * 
	 * @return
	 */
	public static StatusMsgConfig getInstance() {
		return config;
	}
	/**
	 * 
	 * @param key
	 * @return
	 */
	public String getMsg(String key) {
		return msgMap.get(key); 
	}
}