package com.nimbler.tp.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.common.DBException;
import com.nimbler.tp.dbobject.Login;
import com.nimbler.tp.gtfs.GraphAcceptanceTest;
import com.nimbler.tp.mongo.PersistenceService;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.twitter.CaltrainAdvisoriesService;
import com.nimbler.tp.service.twitter.CaltrainAdvisoriesService.PUSH_NOTIFICATION;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.OperationCode.TP_CODES;
import com.nimbler.tp.util.StatusMsgConfig;
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
			sendTweetMessage(request, response);
		} else if (opcode.equalsIgnoreCase("testGraph")) {
			testGraph(request, response);
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
		} else {
			String message = "Username and Password are invalid.";
			response.sendRedirect("login.jsp?error="+message);
		}

	}
	/**
	 * 
	 * @param request
	 * @param response
	 */
	private void sendTweetMessage(HttpServletRequest request,HttpServletResponse response) {
		try {
			String tweet= request.getParameter("tweet");
			if(!ComUtils.isEmptyString(tweet)) {
				String alertMsg = StatusMsgConfig.getInstance().getMsg("CALTRAIN_URGENT_TWEET");
				alertMsg = String.format(alertMsg, tweet); 

				CaltrainAdvisoriesService caltrainService = BeanUtil.getCaltrainService();
				int pushNotification = caltrainService.pushUrgentTweets(alertMsg);
				String message = null;
				if (pushNotification == PUSH_NOTIFICATION.SUCCESS.ordinal()) 
					message ="Tweet Sent Succesfully.";
				else if(pushNotification == PUSH_NOTIFICATION.NO_DEVICE_FOUND.ordinal()) 
					message ="No device found for push alert.";
				else if (pushNotification == PUSH_NOTIFICATION.FAIL.ordinal())
					message = "Tweet Send Fail.";
				response.sendRedirect("tweet.jsp?message="+message);
			} else 
				response.sendRedirect("tweet.jsp?message=please fill the tweet. ");
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
		PersistenceService persistenceService = BeanUtil.getPersistanceService();
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
	private void testGraph(HttpServletRequest request,HttpServletResponse response) {
		try {
			GraphAcceptanceTest graphTest = (GraphAcceptanceTest) TPApplicationContext.getBeanInstance().getBean("gat");
			graphTest.doTest();
			response.sendRedirect("tweet.jsp?graphTest=Graph Test started.");
		} catch (IOException e) {
			logger.error(loggerName, e.getMessage());
		}
	}
}
