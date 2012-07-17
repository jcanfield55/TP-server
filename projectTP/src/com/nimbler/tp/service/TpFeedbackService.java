/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.common.DBException;
import com.nimbler.tp.dataobject.TPResponse;
import com.nimbler.tp.dataobject.TripPlan;
import com.nimbler.tp.dbobject.EventLog;
import com.nimbler.tp.dbobject.FeedBack;
import com.nimbler.tp.dbobject.FeedBack.FEEDBACK_FORMAT_TYPE;
import com.nimbler.tp.dbobject.FeedBack.FEEDBACK_SOURCE_TYPE;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.service.smtp.MailService;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.HtmlUtil;
import com.nimbler.tp.util.JSONUtil;
import com.nimbler.tp.util.OperationCode;
import com.nimbler.tp.util.OperationCode.TP_CODES;
import com.nimbler.tp.util.RequestParam;
import com.nimbler.tp.util.ResponseUtil;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.MONGO_TABLES;
import com.nimbler.tp.util.TpException;

/**
 * The Class OTPService.
 * @author nirmal
 */
public class TpFeedbackService {
	private String loggerName;
	@Autowired
	private LoggingService logger;
	@Autowired
	private PersistenceService persistenceService;
	@Autowired
	private MailService mailService;

	/**
	 * 
	 * @param reqParams
	 * @param lstFile
	 * @return
	 * @throws IOException
	 */
	public TPResponse addFeedback(Map<String, String> reqParams, List<File> lstFile) throws IOException, TpException {
		String tripFilePath  = null;
		TpPlanService planService = BeanUtil.getPlanService();
		try {
			FeedBack  feedBack = new FeedBack();
			int feedbacSource = NumberUtils.toInt(reqParams.get(RequestParam.SOURCE),-1);
			TripPlan plan = null;
			FEEDBACK_SOURCE_TYPE type =  FEEDBACK_SOURCE_TYPE.values()[feedbacSource];
			String uniqueId = reqParams.get(RequestParam.UNIQUE_ID);

			switch (type) {
			case OVERVIEW:
				feedBack.setAddFrom(reqParams.get(RequestParam.RAW_ADDRESS_FROM));
				feedBack.setAddTo(reqParams.get(RequestParam.RAW_ADDRESS_TO));
				feedBack.setDate(reqParams.get(RequestParam.DATE));
				break;
			case PLAN:
				feedBack.setPlanID(uniqueId);
				plan = planService.getFullPlanFromDB(uniqueId);				
				break;
			case ITINERARY:
				feedBack.setItineraryID(uniqueId);
				plan = planService.getFullPlanOfItinerary(uniqueId);
				break;
			case LEG:
				feedBack.setLegID(uniqueId);
				plan = planService.getFullPlanOfLeg(uniqueId);
				break;
			default:
				break;
			}

			if (!ComUtils.isEmptyString(uniqueId) && plan==null) {
				logger.error(loggerName, "Plan object not found from DB for feedback. - "+reqParams);
				throw new TpException(OperationCode.TP_CODES.FAIL);
			}			
			String feedBackText = reqParams.get(RequestParam.TEXT_FEEDBACK);
			int feedBackFormat = NumberUtils.toInt(reqParams.get(RequestParam.FEEDBACK_FORMAT_TYPE),FEEDBACK_FORMAT_TYPE.AUDIO.ordinal());
			float rating = NumberUtils.toFloat(reqParams.get(RequestParam.RATING),-1);
			String deviceId = reqParams.get(RequestParam.DEVICE_ID);

			if(ComUtils.isEmptyString(deviceId) || feedbacSource==-1)
				throw new TpException(TP_CODES.INVALID_REQUEST);

			if (type != FEEDBACK_SOURCE_TYPE.OVERVIEW) {
				TpEventLoggingService eventService = BeanUtil.getTpEventLoggingService();
				EventLog eventLog = eventService.getLogForPlan(plan.getId());
				if (eventLog!=null) {
					feedBack.setAddFrom(eventLog.getFrmtdAddrFrom()!=null ? eventLog.getFrmtdAddrFrom() : eventLog.getRawAddrFrom());
					feedBack.setAddTo(eventLog.getFrmtdAddrTo()!=null ? eventLog.getFrmtdAddrTo() : eventLog.getRawAddrTo());
				}
			}
			feedBack.setDeviceId(deviceId);
			if (lstFile!=null && lstFile.size()>0)
				feedBack.setAudioFileUrl(lstFile.get(0).getName());
			feedBack.setFbText(feedBackText);
			feedBack.setFormatType(feedBackFormat);
			feedBack.setCreateTime(System.currentTimeMillis());
			feedBack.setSource(feedbacSource);
			feedBack.setRating(rating);
			feedBack.setEmailId(reqParams.get(RequestParam.EMAIL_ID));
			persistenceService.addObject(MONGO_TABLES.feedback.name(), feedBack);
			try {
				String tripTemplet = HtmlUtil.getFeedbackHtmlTemplet(plan, feedBack);
				List<String> lstFileNames = null;
				if (lstFile!=null && lstFile.size()>0) 
					lstFileNames = ComUtils.getFileNamesFromFiles(lstFile);
				List<String> lstFilesToDelete = new ArrayList<String>();
				if(plan!=null){
					if(lstFileNames==null)
						lstFileNames = new ArrayList<String>();
					String planJsonStr = JSONUtil.getJsonFromObj(plan);
					tripFilePath = getPlanFilePath(planJsonStr,deviceId);
					lstFileNames.add(tripFilePath);
					lstFilesToDelete.add(tripFilePath);
				}

				mailService.sendFeedBackMail(feedBack,lstFileNames,tripTemplet,lstFilesToDelete);
				return ResponseUtil.createResponse(TP_CODES.SUCESS);
			} catch (IOException ioe) {
				logger.error(loggerName, ioe.getMessage());
				return ResponseUtil.createResponse(TP_CODES.SUCESS);
			} catch (TpException te) {
				logger.error(loggerName, te.getMessage());
				return ResponseUtil.createResponse(TP_CODES.SUCESS);
			}
		} catch (DBException e) {
			logger.error(loggerName, e.getMessage());
			return ResponseUtil.createResponse(TP_CODES.FAIL);
		}
	}
	/**
	 * 
	 * @param strPlan
	 * @param deviceId
	 * @return
	 */
	public String getPlanFilePath(String strPlan, String deviceId) throws IOException {
		String filepath = TpConstants.TEMP_DIR_PATH+"/"+deviceId+"_trip.txt";		
		BufferedWriter bufferedWriter = null;
		try {
			FileWriter fileWriter = new FileWriter(filepath);
			bufferedWriter= new BufferedWriter(fileWriter);
			bufferedWriter.write(strPlan);
		} finally {
			if (bufferedWriter!=null) {
				try {
					bufferedWriter.flush();
					bufferedWriter.close();
				} catch (IOException e) {
				}
			}
		}
		return filepath;
	}
	/**
	 * 
	 * @param itineraryId
	 * @return
	 *//*
	private TripPlan getPlanOfItineraryFromDB(String itineraryId) {
		Itinerary itinerary = (Itinerary) persistenceService.findOne(MONGO_TABLES.itinerary.name(), "id", itineraryId, Itinerary.class);
		if (itinerary == null)
			return null;
		TripPlan plan = (TripPlan) persistenceService.findOne(MONGO_TABLES.plan.name(), "id", itinerary.getPlanId(), TripPlan.class);
		return plan;
	}
	  *//**
	  * 
	  * @param legId
	  * @return
	  *//*
	private Itinerary getItineraryOfLegFromDB(String legId) {
		Leg leg = (Leg) persistenceService.findOne(MONGO_TABLES.leg.name(), "id", legId, Leg.class);
		if (leg == null)
			return null;
		Itinerary itinerary = (Itinerary) persistenceService.findOne(MONGO_TABLES.itinerary.name(), "id", leg.getId(), Itinerary.class);
		return itinerary;	
	}*/
	/**
	 * 
	 * @param legId
	 * @return
	 *//*
	private TripPlan getPlanOfLegFromDB(String legId) {
		Itinerary itinerary  = getItineraryOfLegFromDB(legId);
		if (itinerary==null)
			return null;
		TripPlan plan = (TripPlan) persistenceService.findOne(MONGO_TABLES.plan.name(), "id", itinerary.getPlanId(), TripPlan.class);
		return plan;
	}*/
	public PersistenceService getPersistenceService() {
		return persistenceService;
	}

	public void setPersistenceService(PersistenceService persistenceService) {
		this.persistenceService = persistenceService;
	}

	public MailService getMailService() {
		return mailService;
	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}
	public String getLoggerName() {
		return loggerName;
	}
	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
	public LoggingService getLogger() {
		return logger;
	}
	public void setLogger(LoggingService logger) {
		this.logger = logger;
	}
}