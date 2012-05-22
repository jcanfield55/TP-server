/*
 * 
 */
package com.apprika.otp;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.apprika.otp.util.TpConstants;

/**
 * The Class OTPApplicationContext.
 */
public class TPApplicationContext {

	public enum SPRING_BEANS{
		MONGO_OPERATION_TEMPLATE("mongoOpetation"),
		PERSISTANCE_SERVICE("PersistanceService");
		String name;

		private SPRING_BEANS(String name) {
			this.name = name;
		}
		private SPRING_BEANS() {
			this.name = name();
		}
		public String bean() {
			return name;
		}
	}

	private static TPApplicationContext ctxt = new TPApplicationContext();
	ClassPathXmlApplicationContext mainCtxt;


	private TPApplicationContext(){
		load();
	}

	/**
	 * Load.
	 */
	private void load() {
		mainCtxt = new ClassPathXmlApplicationContext(getBeanList());
		System.out.println("Context Initialized...");
	}

	/**
	 * Gets the bean list.
	 *
	 * @return the bean list
	 */
	private String[] getBeanList() {
		//		return new String[]{OtpConstants.FILE_SPRING_CONFIGURATION,"conf/spring/mongo-config.xml"};
		return new String[]{TpConstants.FILE_SPRING_CONFIGURATION};
	}

	/**
	 * Gets the single instance of OTPApplicationContext.
	 *
	 * @return single instance of OTPApplicationContext
	 */
	public static TPApplicationContext getInstance(){
		return ctxt;
	}

	/**
	 * Gets the bean instance.
	 *
	 * @return the bean instance
	 */
	public static TPApplicationContext getBeanInstance(){
		return ctxt;
	}

	/**
	 * Gets the bean.
	 *
	 * @param beanName the bean name
	 * @return the bean
	 */
	public Object getBean(String beanName) {
		return mainCtxt.getBean(beanName);
	}
	public static Object getBeanByName(String beanName) {
		return getInstance().getBean(beanName);
	}
}
