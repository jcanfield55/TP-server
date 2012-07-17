/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.util;

import java.io.IOException;
import java.util.Properties;


/**
 * The Class TpProperty.
 *
 * @author nirmal
 */
public class TpProperty {

	private Properties props;
	private static Properties defaultProps;

	public Properties getProps() {
		return props;
	}

	public void setProps(Properties props) {
		this.props = props;
	}

	/**
	 * Load property.
	 *
	 * @param propertyFile the property file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void loadProperty(String propertyFile) throws IOException{
		if(props == null)
			props = new Properties();
		props.load(TpProperty.class.getClassLoader().getResourceAsStream(propertyFile));
	}
	/**
	 * 
	 * @param prop
	 * @return
	 */
	public static String getDefaultProperty(String prop) {
		if (defaultProps==null) {
			defaultProps = new Properties();
			try {
				defaultProps.load(TpProperty.class.getClassLoader().getResourceAsStream(TpConstants.FILE_OTP_PROPERTY));
			} catch (IOException e) {				
				System.err.println(e);
			}
		}
		return  (String) defaultProps.get(prop);
	}

	public String getProperty(String key){
		return props.getProperty(key);
	}

	public TpProperty() {
	}

	/**
	 * 
	 * @param propertyFile File path of property file.
	 * @throws IOException
	 */
	public TpProperty(String propertyFile) throws IOException {
		loadProperty(propertyFile);
	}

}
