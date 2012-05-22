package com.apprika.otp.startup;


import java.io.File;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.apprika.otp.TPApplicationContext;
import com.apprika.otp.service.LoggingService;
import com.apprika.otp.servlet.MainServlet;
import com.apprika.otp.util.ComUtils;
import com.apprika.otp.util.TpConstants;
import com.apprika.otp.util.TpProperty;

/**
 * The Class StartupManager.
 * @author nirmal
 */
public class StartupManager{
	Logger logger = LoggingService.getLoggingService(MainServlet.class.getName());
	/**
	 * 
	 */
	public StartupManager() {
		init();
	}

	private void init() {
		DOMConfigurator.configure(LoggingService.class.getClassLoader().getResource(TpConstants.FILE_LOG_CONFIGURATION));
		TPApplicationContext.getInstance();		
		logger.info("Image Repository Relative Path: "+TpConstants.REPO_RELATIVE_PATH);
		System.out.println("\n==============================================================================");
		System.out.println("SMTP                           : "+TpProperty.getDefaultProperty("smtpHost"));
		System.out.println("==============================================================================\n");
		File imageRepository = new File(TpConstants.REPO_RELATIVE_PATH);
		if(!imageRepository.exists())
			imageRepository.mkdirs();
		ComUtils.readHtmlTemplet();
	}
	public static void main(String[] args) {
		new StartupManager();
	}
}
