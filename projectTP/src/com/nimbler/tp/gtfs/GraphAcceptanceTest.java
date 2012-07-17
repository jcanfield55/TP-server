/*
 * @author nirmal
 */
package com.nimbler.tp.gtfs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.service.smtp.MailService;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.HtmlUtil;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpException;
import com.nimbler.tp.util.TpProperty;

/**
 * The Class GraphAcceptanceTest.
 *
 * @author nirmal
 */
@SuppressWarnings("unchecked")
public class GraphAcceptanceTest{

	@Autowired
	private LoggingService logger;
	@Autowired
	private MailService mailService;
	private String loggerName;

	private String sourceDir;
	protected String destDir;
	private int  threads;
	private ExecutorService executorService;
	private List<PlanCompareResult> lstCompareResults;	
	public static final String EMAIL_ID = TpConstants.FEEDBACK_EMAIL_ID;
	public static final String SUBJECT = TpConstants.GRAPH_TEST_MAIL_SUBJECT;

	/**
	 * Start test.
	 */
	public void startTest() {
		logger.debug(loggerName, "Service Started.....");
		if(TpProperty.getDefaultProperty("test.graph").trim().equalsIgnoreCase("false")){
			logger.debug(loggerName, "Graph test will not be performed..");

		}else
			doTest();
	}	

	/**
	 * Do test.
	 */
	public void doTest() {
		try {
			executorService = Executors.newFixedThreadPool(threads);
			lstCompareResults = Collections.synchronizedList(new ArrayList<PlanCompareResult>());
			List<File> lstSourceFiles  = new ArrayList<File>(FileUtils.listFiles(new File(sourceDir),new String[]{"json"}, false));
			FileUtils.cleanDirectory(new File(destDir));
			if(ComUtils.isEmptyList(lstSourceFiles))
				throw new TpException("No source files found in : "+sourceDir);
			logger.debug(loggerName, "No of Plans to be tested: "+lstSourceFiles.size());
			for (File file : lstSourceFiles){ 
				executorService.execute(new PlanCompareTask(file,destDir,logger,loggerName,lstCompareResults));
			}
			executorService.shutdown();
			extractResult();
		} catch (TpException e) {
			logger.error(loggerName, e.getErrMsg());
			mailService.sendMail(EMAIL_ID,SUBJECT,"Error While Test Graph,Please check Logs: "+e.getMessage(),false,null,null);
		}catch (Exception e) {
			logger.error(loggerName, e);
			mailService.sendMail(EMAIL_ID,SUBJECT,"Error While Test Graph,Please check Logs: "+e.getMessage(),false,null,null);
		}
	}

	/**
	 * Extract result.
	 */
	private void extractResult(){
		Thread task = new Thread(){
			public void run() {
				try {
					logger.debug(loggerName, "waiting for tasks to be compeleted.....");
					while(!executorService.isTerminated())
						ComUtils.sleep(500);
					logger.debug(loggerName, "Done...");
					int sucessCount = 0;
					int failCount = 0;
					List<PlanCompareResult> lstFailResult = new ArrayList<PlanCompareResult>();
					for (PlanCompareResult result : lstCompareResults) {
						if(result.isMatch())
							sucessCount++;
						else{
							failCount++;
							lstFailResult.add(result);
						}
					}
					logger.debug(loggerName, "Sucess:"+sucessCount+", Fail:"+failCount);
					mailResult(sucessCount,failCount,lstFailResult);
				}catch (Exception e) {					
					logger.error(loggerName, e);					
					mailService.sendMail(EMAIL_ID,SUBJECT,"Error While Test Graph,Please check Logs: "+e.getMessage(),false,null,null);
				}
			}

			/**
			 * Mail result.
			 *
			 * @param sucessCount the sucess count
			 * @param failCount the fail count
			 * @param lstFailResult the lst fail result
			 * @throws IOException 
			 */
			private void mailResult(int sucessCount, int failCount,	List<PlanCompareResult> lstFailResult) throws IOException {
				logger.debug(loggerName, "sending mail...");
				List<String> lstAttachement = null;
				if(!ComUtils.isEmptyList(lstFailResult))
					lstAttachement = HtmlUtil.getGraphTestDetailResult(lstFailResult);
				String strSummery = HtmlUtil.getGraphTestResultSummeryTable(sucessCount,failCount);
				mailService.sendMail(EMAIL_ID,SUBJECT,strSummery,true,lstAttachement,lstAttachement);
			};
		};
		task.start();
	}

	public LoggingService getLogger() {
		return logger;
	}
	public void setLogger(LoggingService logger) {
		this.logger = logger;
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
	public String getSourceDir() {
		return sourceDir;
	}
	public void setSourceDir(String sourceDir) {
		this.sourceDir = sourceDir;
	}
	public String getDestDir() {
		return destDir;
	}
	public void setDestDir(String destDir) {
		this.destDir = destDir;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

}
