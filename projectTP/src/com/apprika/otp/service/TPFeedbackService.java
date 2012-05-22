package com.apprika.otp.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.apprika.otp.TPApplicationContext;
import com.apprika.otp.TPApplicationContext.SPRING_BEANS;
import com.apprika.otp.dataobject.TPResponse;
import com.apprika.otp.dbobject.FeedBack;
import com.apprika.otp.dbobject.Trip;
import com.apprika.otp.dbobject.User;
import com.apprika.otp.mongo.PersistanceService;
import com.apprika.otp.smtp.MailService;
import com.apprika.otp.util.ComUtils;
import com.apprika.otp.util.OperationCode.TP_CODES;
import com.apprika.otp.util.ResponseUtil;
import com.apprika.otp.util.TpConstants;
import com.apprika.otp.util.TpConstants.MONGO_TABLES;

/**
 * The Class OTPService.
 * @author nirmal
 */
public class TPFeedbackService {
	Logger logger = LoggingService.getLoggingService(TPFeedbackService.class.getName());
	private PersistanceService persistanceService;
	MailService mailService;

	public TPFeedbackService() {
		try {
			this.persistanceService = (PersistanceService) TPApplicationContext.getBeanByName(SPRING_BEANS.PERSISTANCE_SERVICE.bean());
			this.mailService = MailService.getInstance();
		} catch (Exception e) {
			logger.error("",e);
		}
	}

	/**
	 * Save new feed back.
	 *
	 * @param deviceId the device id
	 * @param strTrip the str trip
	 * @param source the source
	 * @param lstFile the lst file
	 * @param feedBackFormat the feed back format
	 * @param rating the rating
	 * @return the oTP response
	 * @throws IOException 
	 * @throws Exception the exception
	 */
	public TPResponse saveNewFeedBack(String deviceId, String strTrip, int source, List<File> lstFile, int feedBackFormat, float rating,String textFeedback) throws IOException {
		String tripFilePath  = null;
		try{
			FeedBack  feedBack = new FeedBack();
			feedBack.setUser(new User(deviceId));
			feedBack.setAudioFileUrl(lstFile.get(0).getName());
			feedBack.setData(textFeedback);
			feedBack.setFormatType(feedBackFormat);
			feedBack.setCreateTime(System.currentTimeMillis());
			feedBack.setSource(source);

			Trip trip = new Trip();		
			trip.setTripDef(strTrip);
			trip.setType(Trip.TRIP_TYPE.WHOLE_TRIP.ordinal());
			feedBack.setTrip(trip);

			String tripTemplet = ComUtils.getHtmlTemplet(strTrip,source,textFeedback);

			tripFilePath =getTempTripXmlFile(strTrip,deviceId); 
			persistanceService.addObject(MONGO_TABLES.feedback.name(), feedBack);
			List<String> lstFileNames = ComUtils.getFileNamesFromFiles(lstFile);
			lstFileNames.add(tripFilePath);
			mailService.sendFeedBackMail(feedBack, lstFileNames,tripTemplet);
			return ResponseUtil.createResponse(TP_CODES.SUCESS);
		}finally{			
			if(!ComUtils.isEmptyString(tripFilePath)){
				File file = new File(tripFilePath);
				if(file.exists())
					file.delete();
			}
		}
	}

	/**
	 * Make response xml file.
	 *
	 * @param strTrip the str trip
	 * @param deviceId the device id
	 * @return the string
	 */
	public String getTempTripXmlFile(String strTrip, String deviceId){
		String filepath = TpConstants.TEMP_DIR_PATH+"/"+deviceId+"_trip.xml";
		BufferedWriter bufferedWriter = null;
		try {
			FileWriter fileWriter = new FileWriter(filepath);
			bufferedWriter= new BufferedWriter(fileWriter);
			bufferedWriter.write(strTrip);
		} catch (IOException e) {
			logger.error("",e);
		}finally{
			if(bufferedWriter!=null){
				try {
					bufferedWriter.flush();
					bufferedWriter.close();
				} catch (IOException e) {
					logger.error("",e);
				}
			}
		}
		return filepath;
	}
}
