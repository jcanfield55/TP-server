package com.apprika.otp.smtp;

import java.io.Serializable;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


public class MailMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1019367921662567110L;


	private String to;
	private String cc;
	private String bcc;
	private String subject;
	private String message;
	private	boolean html = false;
	private boolean addressSet;
	private boolean isAddressSet() {
		return addressSet;
	}
	private void setAddressSet(boolean addressSet) {
		this.addressSet = addressSet;
	}
	public String getCc() {
		return cc;
	}
	public void setCc(String cc) {
		this.cc = cc;		
	}
	public String getBcc() {
		return bcc;
	}
	public void setBcc(String bcc) {
		this.bcc = bcc;		
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getTo() {
		return to;
	}	
	public void setTo(String to) {
		this.to = to;		
	}

	public boolean isHtml() {
		return html;
	}
	public void setHtml(boolean html) {
		this.html = html;
	}
	/**
	 * 
	 * @param msg
	 * @param lstAttachments
	 * @throws MessagingException
	 */
	public void toMimeMessage(MimeMessage msg, List<String> lstAttachments) throws MessagingException {		
		boolean isMultipart = false;
		if (lstAttachments!=null && lstAttachments.size()>0) {
			isMultipart = true;
		}
		if (this.getTo() != null) {
			String[] receipents = this.getTo().split(",");
			InternetAddress[] contacts = new InternetAddress[receipents.length];
			for (int i = 0; i < contacts.length; i++) {
				contacts[i] = new InternetAddress(receipents[i]);
			}
			msg.setRecipients(RecipientType.TO, contacts);
			setAddressSet(true);
		}

		if (this.getCc() != null && this.getCc().trim().length()!=0) {
			String[] ccReceipents  = this.getCc().split(",");
			InternetAddress[] ccContacts = new InternetAddress[ccReceipents.length];
			for (int i = 0; i < ccContacts.length; i++) {
				ccContacts[i] = new InternetAddress(ccReceipents[i]);
			}
			msg.setRecipients(RecipientType.CC, ccContacts); 			
			if(!isAddressSet())
				setAddressSet(true);
		}
		if (this.getBcc() != null && this.getBcc().trim().length()!=0) {
			String[] bccReceipents = this.getBcc().split(",");
			InternetAddress[] bccContacts = new InternetAddress[bccReceipents.length];
			for (int i = 0; i < bccContacts.length; i++) {
				bccContacts[i] = new InternetAddress(bccReceipents[i]);
			}
			msg.setRecipients(RecipientType.BCC, bccContacts);
			if(!isAddressSet())
				setAddressSet(true);
		}

		msg.setSubject(this.getSubject()!=null?this.getSubject():"");
		String strMessageText = this.getMessage()!=null?this.getMessage():"";
		if(isMultipart) { // attache all attachement 
			Multipart multipart = new MimeMultipart();
			BodyPart messageBodyPart = new MimeBodyPart();
			if(this.isHtml())
				messageBodyPart.setContent(strMessageText, "text/html; charset=ISO-8859-1");
			else
				messageBodyPart.setText(strMessageText);
			multipart.addBodyPart(messageBodyPart);
			for (int i=0;i<lstAttachments.size();i++) {
				messageBodyPart = new MimeBodyPart();
				String attachFilePath = lstAttachments.get(i);
				//File fileToAttach = new File(attachFilePath);
				DataSource source = new FileDataSource(attachFilePath);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(source.getName());
				multipart.addBodyPart(messageBodyPart);
			}
			msg.setContent(multipart); 
		} else {
			if(this.isHtml())
				msg.setContent(strMessageText, "text/html; charset=ISO-8859-1");
			else
				msg.setText(this.getMessage()!=null?this.getMessage():"");
		}
	}
}