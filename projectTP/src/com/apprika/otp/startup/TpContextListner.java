package com.apprika.otp.startup;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author nirmal
 *
 */
public class TpContextListner implements ServletContextListener{

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		new StartupManager();
	}

}
