package com.apprika.otp.ws;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.apprika.otp.dataobject.TPResponse;
import com.apprika.otp.dbobject.FeedBack.FEEDBACK_FORMAT_TYPE;
import com.apprika.otp.service.LoggingService;
import com.apprika.otp.service.TPFeedbackService;
import com.apprika.otp.util.ComUtils;
import com.apprika.otp.util.OperationCode.TP_CODES;
import com.apprika.otp.util.RequestParam;
import com.apprika.otp.util.ResponseUtil;
import com.apprika.otp.util.TpException;

@Path("/feedback/")
public class FeedbackWS {
	Logger logger = LoggingService.getLoggingService(FeedbackWS.class.getName());

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/test/")
	public String firstTest() {
		return "Feedback service";
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)	
	public String firstTest1() {
		return "Feedback service1";
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/new/new")
	public String firstTest5() {
		return "Feedback service5";
	}
	@POST
	@Path("/new/")
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public TPResponse upload(@Context HttpServletRequest request) throws Exception {
		TPResponse response = null;		
		try {
			Map<String,String> reqMap = new HashMap<String, String>();
			List<File> lstFile = new ArrayList<File>();
			ComUtils.parseMultipartRequest(request, reqMap, lstFile,System.currentTimeMillis()+"_%s");

			String deviceId = reqMap.get(RequestParam.DEVICE_ID);
			String strTrip = reqMap.get(RequestParam.TRIP);
			String strTextFeedBabk = reqMap.get(RequestParam.TEXT_FEEDBACK);

			int source = NumberUtils.toInt(reqMap.get(RequestParam.SOURCE),-1);
			int feedBackFormat = NumberUtils.toInt(reqMap.get(RequestParam.FEEDBACK_FORMAT_TYPE),FEEDBACK_FORMAT_TYPE.AUDIO.ordinal());
			float rating = NumberUtils.toFloat(reqMap.get(RequestParam.RATING),-1);

			if(ComUtils.isEmptyString(strTrip) || ComUtils.isEmptyString(deviceId) || lstFile.size()==0 || source==-1)
				throw new TpException(TP_CODES.INVALID_REQUEST);			
			TPFeedbackService service = new TPFeedbackService();
			response=  service.saveNewFeedBack(deviceId,strTrip,source,lstFile,feedBackFormat,rating,strTextFeedBabk);
		} catch (TpException e) {			
			logger.info(e.getErrMsg());
			response = ResponseUtil.createResponse(e);
		} catch (Exception e) {	
			logger.error("",e);
			response = ResponseUtil.createResponse(TP_CODES.INTERNAL_SERVER_ERROR);
		}
		return response;
	}
}