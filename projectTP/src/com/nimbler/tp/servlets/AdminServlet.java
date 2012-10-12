package com.nimbler.tp.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.common.DBException;
import com.nimbler.tp.dbobject.Login;
import com.nimbler.tp.dbobject.NimblerParams;
import com.nimbler.tp.dbobject.NimblerParams.NIMBLER_PARAMS;
import com.nimbler.tp.gtfs.GraphAcceptanceTest;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.twitter.CaltrainAdvisoriesService;
import com.nimbler.tp.service.twitter.CaltrainAdvisoriesService.PUSH_NOTIFICATION;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.OperationCode.TP_CODES;
import com.nimbler.tp.util.StatusMsgConfig;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpConstants.MONGO_TABLES;
import com.nimbler.tp.util.TpException;
/**
 * 
 * @author suresh
 *
 */
public class AdminServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private LoggingService logger = (LoggingService)TPApplicationContext.getInstance().getBean("loggingService");
	private static String loggerName = "com.nimbler.tp.service.twitter";
	private PersistenceService persistenceService = BeanUtil.getPersistanceService();
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AdminServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String opcode = request.getParameter("opcode");
		if (opcode.equalsIgnoreCase("authentication")) {
			checkLogin(request, response);
		} else if (opcode.equalsIgnoreCase("tweet")) {
			String chkSendToTestUser = request.getParameter("chkSendToTestUser");
			String chkEnableSound = request.getParameter("chkEnableSound");
			String user = request.getSession().getAttribute("username")+"";
			String betaUsers= StringUtils.trimToNull(request.getParameter("deviceToken"));
			boolean isForTestUser = false;
			boolean enableSound = false;
			if (chkEnableSound !=null && chkEnableSound.equalsIgnoreCase("on")) {
				enableSound = true;
			}
			if (chkSendToTestUser !=null && chkSendToTestUser.equalsIgnoreCase("on")) {
				logger.debug(loggerName, "Sending admin tweet to test users by ["+user+"],to :"+betaUsers);
				isForTestUser = true;
				storeDeviceToken(request, response,betaUsers, isForTestUser);
			}else
				logger.debug(loggerName, "Sending admin tweet to all users by ["+user+"]");
			sendTweetMessage(request, response, betaUsers, isForTestUser,enableSound);
		} else if (opcode.equalsIgnoreCase("testGraph")) {
			graphTest(request, response);
		} 
	}
	/**
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	private void checkLogin(HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");

		boolean success = checkAuthentication(username, password);
		if (success) {
			HttpSession session = request.getSession();
			session.setAttribute("username", username);
			response.sendRedirect("tweet.jsp");
			logger.debug(loggerName, "Login sucess for "+username);
		} else {
			String message = "Username and Password are invalid.";
			response.sendRedirect("login.jsp?error="+message);
			logger.debug(loggerName, "Login failed for "+username+" with password:"+password);
		}

	}
	/**
	 * 
	 * @param request
	 * @param response
	 */
	private void sendTweetMessage(HttpServletRequest request,HttpServletResponse response, String betaUserdeviceToken, boolean isForTestUser, boolean enableSound) {
		try {
			String tweet= request.getParameter("tweet");
			if(!ComUtils.isEmptyString(tweet)) {
				String alertMsg = StatusMsgConfig.getInstance().getMsg("CALTRAIN_URGENT_TWEET");
				alertMsg = String.format(alertMsg, tweet); 
				logger.debug(loggerName, "Sending Tweet:"+alertMsg);
				CaltrainAdvisoriesService caltrainService = BeanUtil.getCaltrainService();
				int pushNotification;
				if (isForTestUser) {
					pushNotification = caltrainService.pushTweetToTestUser(alertMsg,betaUserdeviceToken,enableSound);
				} else  { 
					pushNotification = caltrainService.pushUrgentTweets(alertMsg);
				}
				String message = null;
				if (pushNotification == PUSH_NOTIFICATION.SUCCESS.ordinal()) 
					message ="Tweet Sent Succesfully.";
				else if(pushNotification == PUSH_NOTIFICATION.NO_DEVICE_FOUND.ordinal()) 
					message ="No device found for push alert.";
				else if (pushNotification == PUSH_NOTIFICATION.FAIL.ordinal())
					message = "Tweet Sent Fail.";
				response.sendRedirect("tweet.jsp?message="+message);
				logger.debug(loggerName, message);
			} else 
				response.sendRedirect("tweet.jsp?message=please fill the tweet. ");
		} catch (Exception e) {
			logger.error(loggerName, e.getMessage());
		}
	}
	/**
	 * 
	 * @param request
	 * @param response
	 * @param betaUsers 
	 */
	private void storeDeviceToken(HttpServletRequest request,HttpServletResponse response, String betaUsers, boolean chkSentToTestUser) {
		try {
			NimblerParams nimblerParam= new NimblerParams();
			nimblerParam.setName(NIMBLER_PARAMS.BETA_USERS.name());
			nimblerParam.setValue(betaUsers);
			List<NimblerParams> listParams = persistenceService.getCollectionList(MONGO_TABLES.nimbler_params.name(), NimblerParams.class);
			if (listParams.size() > 0) {
				persistenceService.updateSingleObject(MONGO_TABLES.nimbler_params.name(), TpConstants.NIMBLER_PARAMS_NAME, NIMBLER_PARAMS.BETA_USERS.name(), TpConstants.NIMBLER_PARAMS_VALUE, nimblerParam.getValue());
			} else {
				persistenceService.addObject(MONGO_TABLES.nimbler_params.name(), nimblerParam);
			}
		} catch (Exception e) {
			logger.error(loggerName, e.getMessage());
		}
	}
	/**
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	private boolean checkAuthentication(String username, String password) {
		try {
			List<Login> authentication = (List<Login>) persistenceService.find(MONGO_TABLES.login.name(), "username", username, Login.class);
			if(authentication == null || authentication.size() == 0)
				throw new TpException(TP_CODES.LOGIN_FAILURE.getCode());
			if(authentication.get(0).getUsername().equals(username) && authentication.get(0).getPassword().equals(password)) {
				return true;
			}
		} catch (DBException e) {
			logger.error(loggerName, e.getMessage());
		} catch (TpException e) {
			logger.error(loggerName, e.getMessage());
		}
		return false;
	}
	/**
	 * 
	 */
	private void graphTest(HttpServletRequest request,HttpServletResponse response) {
		try {
			logger.debug(loggerName, "Grapg test initiated by "+request.getSession().getAttribute("username"));
			GraphAcceptanceTest graphTest = (GraphAcceptanceTest) TPApplicationContext.getBeanInstance().getBean("gat");
			graphTest.doTest();
			response.sendRedirect("tweet.jsp?graphTest=Graph Test started.");
			logger.debug(loggerName, "Graph test started");
		} catch (IOException e) {
			logger.error(loggerName, e.getMessage());
		}
	}
}