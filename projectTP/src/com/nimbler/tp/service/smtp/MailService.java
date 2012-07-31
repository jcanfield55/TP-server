/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.service.smtp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.nimbler.tp.dbobject.FeedBack;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.util.ComUtils;
import com.nimbler.tp.util.TpConstants;

/**
 * The Class MailService.
 */
public class MailService {

	private Session mailSession;
	ExecutorService executor ;
	private int mailThreadPoolSize;
	@Autowired
	private LoggingService logger;
	private String loggerName;
	@Autowired
	JavaMailSenderImpl mailSender;

	/**
	 * 
	 */
	public void init() {
		try {
			executor = Executors.newFixedThreadPool(mailThreadPoolSize);
			logger.info(loggerName, "Mail service started");
		} catch (Exception e) {
			e.printStackTrace();			
			logger.error(loggerName, e);
		}
	}

	/**
	 * 
	 * @param mailMsg
	 * @param lstAttachmentPaths
	 * @param lstFilesToDelete 
	 */
	public void sendMail(MailMessage mailMsg, List<String> lstAttachmentPaths, List<String> lstFilesToDelete) {
		MailTask task = new MailTask(mailMsg,lstAttachmentPaths,lstFilesToDelete);
		executor.execute(task);
	}

	/**
	 * The Class MailTask.
	 *
	 * @author nirmal
	 */
	class MailTask implements Runnable{
		MailMessage mailMsg;
		List<String> lstAttachmentPaths;
		List<String> lstFilesToDelete;
		public MailTask(MailMessage mailMsg, List<String> lstAttachmentPaths, List<String> lstFilesToDelete) {
			this.lstAttachmentPaths = lstAttachmentPaths;
			this.mailMsg = mailMsg;
			this.lstFilesToDelete=lstFilesToDelete;
		}
		@Override
		public void run() {
			try {
				MimeMessage msg = new MimeMessage(mailSession);
				msg.setSentDate(new Date());
				String from = mailSender.getSession().getProperty("mail.smtp.from");
				if(from!=null)
					msg.setFrom(new InternetAddress(from));
				mailMsg.toMimeMessage(msg, lstAttachmentPaths);
				mailSender.send(msg);
				logger.debug(loggerName,"mail sent to "+mailMsg.getTo()+", Subject: "+mailMsg.getSubject());
			} catch (AddressException e) {
				logger.error(loggerName, e.getMessage()+", Msg:"+mailMsg);
			} catch (MessagingException e) {
				logger.error(loggerName, e.getMessage()+", Msg:"+mailMsg);
			}catch (Exception e) {
				logger.error(loggerName, e+", Msg:"+mailMsg);
			}finally{
				deleteFiles(lstFilesToDelete);
			}
		}

		/**
		 * Delete files.
		 *
		 * @param lstFilesToDelete2 the lst files to delete2
		 */
		private void deleteFiles(List<String> lstFilesToDelete2) {
			if(!ComUtils.isEmptyList(lstFilesToDelete)){
				File file = null;
				for (String strFile : lstFilesToDelete) {						
					file = new File(strFile);
					if(file.exists()){
						if(!file.delete())
							logger.info(loggerName, "File not deleted: "+strFile);

					}
				}
			}
		}

	}

	/**
	 * 
	 * @author nirmal
	 *
	 */
	static class HTMLDataSource implements DataSource {
		private String html;

		public HTMLDataSource(String htmlString) {
			html = htmlString;
		}
		// Return html string in an InputStream.
		// A new stream must be returned each time.
		public InputStream getInputStream() throws IOException {
			if (html == null) throw new IOException("Null HTML");
			return new ByteArrayInputStream(html.getBytes());
		}

		public OutputStream getOutputStream() throws IOException {
			throw new IOException("This DataHandler cannot write HTML");
		}

		public String getContentType() {
			return "text/html";
		}

		public String getName() {
			return "JAF text/html dataSource to send e-mail only";
		}

	}

	/**
	 * 
	 * @param message
	 */
	public void sendMailForOtpCheckFail(String message){
		MailMessage mailMsg = new MailMessage();
		mailMsg.setTo(TpConstants.OTP_FAIL_NOTIFY_EMAIL_ID);
		mailMsg.setSubject(TpConstants.OTP_FAIL_NOTIFY_EMAIL_SUBJECT);
		mailMsg.setMessage(message);
		mailMsg.setHtml(true);
		sendMail(mailMsg, null,null);
	}

	/**
	 * Send mail.
	 *
	 * @param to the to
	 * @param subject the subject
	 * @param msg the msg
	 * @param isHtml the is html
	 */
	public void sendMail(String to,String subject,String msg,boolean isHtml, List<String> lstAttachmentPaths, List<String> lstFilesToDelete){
		MailMessage mailMsg = new MailMessage();
		mailMsg.setTo(to);
		mailMsg.setSubject(subject);
		mailMsg.setMessage(msg);
		mailMsg.setHtml(isHtml);
		sendMail(mailMsg, lstAttachmentPaths,lstFilesToDelete);
	}
	/**
	 * Send feed back mail.
	 *
	 * @param feedBack the feed back
	 * @param lstAttachment the lst attachment
	 * @param lstFilesToDelete 
	 */
	public void sendFeedBackMail(FeedBack feedBack, List<String> lstAttachment, String tripMsg, List<String> lstFilesToDelete) {
		MailMessage mailMsg = new MailMessage();
		mailMsg.setTo(TpConstants.FEEDBACK_EMAIL_ID);
		mailMsg.setSubject(TpConstants.FEEDBACK_EMAIL_SUBJECT);
		mailMsg.setMessage(tripMsg);
		mailMsg.setHtml(true);
		sendMail(mailMsg, lstAttachment,lstFilesToDelete);		
	}

	public static void main(String[] args) {
		MailMessage mailMsg = new MailMessage();
		//mailMsg.setBcc("nikunj@apprika.com");
		//mailMsg.setCc("nikunj@apprika.com");
		mailMsg.setTo("nirmal@apprika.com");
		//		mailMsg.setMessage("<b>Test message....<b>");
		//		mailMsg.setMessage(TestConstant.TEMPLET);
		mailMsg.setSubject("Test mail");
		mailMsg.setHtml(true);

		MailService mailService = new MailService();
		List<String> attach = new ArrayList<String>();
		attach.add("C://plan.xml");
		//		attach.add("C://syslog/Copy of NetworkSetting.java");
		mailService.sendMail(mailMsg, attach,null);
		System.out.println("done..");
	}

	public int getMailThreadPoolSize() {
		return mailThreadPoolSize;
	}

	public void setMailThreadPoolSize(int mailThreadPoolSize) {
		this.mailThreadPoolSize = mailThreadPoolSize;
	}

	public LoggingService getLogger() {
		return logger;
	}

	public void setLogger(LoggingService logger) {
		this.logger = logger;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

}