/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd.
 * All rights reserved.
 *
 */
package com.nimbler.tp.startup;


import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.common.DBException;
import com.nimbler.tp.dataobject.Credential;
import com.nimbler.tp.dataobject.UserCredential;
import com.nimbler.tp.dbobject.Login;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.MONGO_TABLES;
import com.nimbler.tp.util.TpException;

/**
 * The Class StartupManager.
 * @author nirmal
 */
public class StartupManager {
	/**
	 * 
	 */
	public StartupManager() {
		init();
	}

	private void init() {
		DOMConfigurator.configure(LoggingService.class.getClassLoader().getResource(TpConstants.FILE_LOG_CONFIGURATION));
		ComUtils.readHtmlTemplet();
		TPApplicationContext.getInstance();
		//BeanUtil.getGtfsDataMonitorService().readGtfsFiles(); Called By Bean initialization
		System.out.println("\n");
		System.out.println("Image Repository Relative Path: "+TpConstants.REPO_RELATIVE_PATH);
		System.out.println("\n==============================================================================");
		System.out.println("SMTP                           : "+((JavaMailSenderImpl)TPApplicationContext.getBeanByName("mailSender")).getHost());
		System.out.println("==============================================================================\n");
		File imageRepository = new File(TpConstants.REPO_RELATIVE_PATH);
		if(!imageRepository.exists())
			imageRepository.mkdirs();
		createIndex();
		addCredentials();
	}
	public static void main(String[] args) {
		new StartupManager();
	}
	/**
	 * 
	 */
	private void createIndex() {
		PersistenceService service = BeanUtil.getPersistanceService();
		try {
			service.createIndexDescending(TpConstants.MONGO_TABLES.plan.name(), "deviceId");
			service.createIndexDescending(TpConstants.MONGO_TABLES.itinerary.name(), "planId");
			service.createIndexDescending(TpConstants.MONGO_TABLES.leg.name(), "itinId");
			service.createIndexDescending(TpConstants.MONGO_TABLES.users.name(), "deviceId");
		} catch (DBException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 */
	public void addCredentials()  {
		List<Object> lstObjToAdd = new ArrayList<Object>();
		try {
			PersistenceService persistenceService= BeanUtil.getPersistanceService();
			int count = persistenceService.getRowCount(MONGO_TABLES.login.name());
			if(count >0)
				return;
			InputStream in= StartupManager.class.getClassLoader().getResourceAsStream(TpConstants.FILE_USER_CREDENTIAL);
			JAXBContext context = JAXBContext.newInstance(Credential.class);
			Unmarshaller um = context.createUnmarshaller();
			Credential credential = (Credential) um.unmarshal(in);
			List<UserCredential> list= credential.getUserCredentials();
			if (list == null || list.size() == 0)
				throw new TpException("Error create while file read of default user.");

			for (UserCredential userCredential : list) {
				Login login = new Login();
				login.setUsername(userCredential.getUsername());
				login.setPassword(userCredential.getPassword());
				lstObjToAdd.add(login);
			}
			persistenceService.addObjects(MONGO_TABLES.login.name(), lstObjToAdd);
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (TpException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
