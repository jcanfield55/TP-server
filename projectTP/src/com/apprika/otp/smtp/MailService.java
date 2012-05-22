package com.apprika.otp.smtp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.apprika.otp.dataobject.SmtpConfig;
import com.apprika.otp.dbobject.FeedBack;
import com.apprika.otp.util.ComUtils;
import com.apprika.otp.util.TpConstants;
import com.apprika.otp.util.TpProperty;

/**
 * The Class MailService.
 */
public class MailService {

	private SmtpConfig smtpConfig = null;
	Properties mailProperties = new Properties();
	private static MailService mailService = new MailService();
	Session mailSession;

	/**
	 * 
	 * @return
	 */
	public static MailService getInstance() {
		return mailService;
	}
	/**
	 * 
	 */
	private MailService() {
		init();
	}
	/**
	 * 
	 */
	private void init() {
		try {		
			smtpConfig = new SmtpConfig();
			boolean isThirdPary = Boolean.valueOf(TpProperty.getDefaultProperty("isThirdParty"));

			smtpConfig.setThirdParty(isThirdPary);
			smtpConfig.setHost(TpProperty.getDefaultProperty("smtpHost"));
			smtpConfig.setEnableDebug(Boolean.valueOf(TpProperty.getDefaultProperty("enableDebug")));
			smtpConfig.setFrom(TpProperty.getDefaultProperty("from"));
			smtpConfig.setTls(TpProperty.getDefaultProperty("mail.smtp.starttls.enable"));
			boolean enableAuth = Boolean.valueOf(TpProperty.getDefaultProperty("enableAuthentication"));
			if (enableAuth) {
				smtpConfig.setEnableAuthentication(enableAuth);
				smtpConfig.setUserName(TpProperty.getDefaultProperty("username"));
				smtpConfig.setPassword(TpProperty.getDefaultProperty("password"));
			}
			String port = TpProperty.getDefaultProperty("port");
			if(!ComUtils.isEmptyString(port))
				smtpConfig.setPort(Integer.valueOf(port));

			loadProperties();
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}

	/**
	 * Load properties.
	 */
	private void loadProperties() {
		if(smtpConfig.isThirdParty()){
			String tls = smtpConfig.getTls();
			if(!ComUtils.isEmptyString(tls))
				mailProperties.put("mail.smtp.starttls.enable", tls);
			mailProperties.put(TpConstants.MAIL_PROP_SMTP_AUTH,String.valueOf(smtpConfig.isEnableAuthentication()));
		}else{
			mailProperties.put(TpConstants.MAIL_PROP_SMTP_HOST, smtpConfig.getHost());
			mailProperties.put(TpConstants.MAIL_PROP_SMTP_AUTH,String.valueOf(smtpConfig.isEnableAuthentication()));
			if ( smtpConfig.isEnableAuthentication()) {
				mailProperties.put(TpConstants.MAIL_PROP_AUTH_USER, smtpConfig.getUserName());
				mailProperties.put(TpConstants.MAIL_PROP_AUTH_PASSWORD, smtpConfig.getPassword());
			}			
		}
		mailSession = Session.getInstance(mailProperties,null);
		mailSession.setDebug(smtpConfig.isEnableDebug());		
	}
	/**
	 * Must be synchronized.
	 * @param newConfig
	 */
	public void updateSmtpConfig(SmtpConfig newConfig) {
		synchronized (smtpConfig) {
			smtpConfig = newConfig;
		}
	}
	/**
	 * 
	 * @param mailMsg
	 * @param lstAttachmentPaths
	 */
	public void sendMail(MailMessage mailMsg, List<String> lstAttachmentPaths) {
		try {
			//			OtpProperty.logger.debug("sending mail ................");
			MimeMessage msg = new MimeMessage(mailSession);						
			msg.setSentDate(new Date());
			msg.setFrom(new InternetAddress(smtpConfig.getFrom()));
			Transport transport = getTransport(mailSession);
			mailMsg.toMimeMessage(msg, lstAttachmentPaths);
			transport.sendMessage(msg,msg.getAllRecipients());
		} catch (MessagingException mex) {
			mex.printStackTrace();						
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
	 * Gets the transport.
	 *
	 * @param session the session
	 * @return the transport
	 * @throws MessagingException the messaging exception
	 * @author nirmal
	 * @since Aug 24, 2011
	 */
	private Transport getTransport(Session session) throws MessagingException{
		Transport transport = session.getTransport("smtp");
		if(smtpConfig.isEnableAuthentication()){
			if(smtpConfig.getPort()!=0)
				transport.connect(smtpConfig.getHost(),smtpConfig.getPort(),smtpConfig.getFrom(), smtpConfig.getPassword());
			else
				transport.connect(smtpConfig.getHost(),smtpConfig.getFrom(), smtpConfig.getPassword());
		}else
			transport.connect();
		return transport;
	}

	/**
	 * Send feed back mail.
	 *
	 * @param feedBack the feed back
	 * @param lstAttachment the lst attachment
	 */
	public void sendFeedBackMail(FeedBack feedBack, List<String> lstAttachment, String tripMsg) {
		MailMessage mailMsg = new MailMessage();
		mailMsg.setTo(TpConstants.FEEDBACK_EMAIL_ID);
		mailMsg.setSubject(TpConstants.FEEDBACK_EMAIL_SUBJECT);
		mailMsg.setMessage(tripMsg);
		mailMsg.setHtml(true);
		mailService.sendMail(mailMsg, lstAttachment);
		System.out.println("mail sent...");
	}

	public static void main(String[] args) {
		MailMessage mailMsg = new MailMessage();
		//mailMsg.setBcc("nikunj@apprika.com");
		//mailMsg.setCc("nikunj@apprika.com");
		mailMsg.setTo("nirmal@apprika.com");
		//		mailMsg.setMessage("<b>Test message....<b>");
		//		mailMsg.setMessage(TestConstant.TEMPLET);
		mailMsg.setSubject("Test mail from Pick-1");
		mailMsg.setHtml(true);

		MailService mailService = new MailService();
		List<String> attach = new ArrayList<String>();
		attach.add("C://plan.xml");
		//		attach.add("C://syslog/Copy of NetworkSetting.java");
		mailService.sendMail(mailMsg, attach);
		System.out.println("done..");
	}
}