package com.axonivy.market.emailencryption.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.mail.SimpleMailMessage;

import com.axonivy.market.emailencryption.service.EncryptedEmailSender;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.mail.MailClientConfig;
import ch.ivyteam.ivy.scripting.objects.Binary;
import ch.ivyteam.ivy.scripting.objects.File;
import ch.ivyteam.util.IvyException;


public class EncryptedEmailSenderBean {
	private String hostname;

	private String port;
	
	private String username;
	
	private SimpleMailMessage email = new SimpleMailMessage();
	
	private String emailRecipient;
	
	private String certificateAsString;
	
	private List<File> listAttachments = new ArrayList<>();
	
	
	public EncryptedEmailSenderBean() throws IvyException {
		try {
			MailClientConfig mailClientConfig = EncryptedEmailSender.getMailSetup();
			hostname = mailClientConfig.host();
			port = String.valueOf(mailClientConfig.port());
			username = mailClientConfig.user();
		} catch (Exception e) {
			throw new IvyException(e);
		}
		
	}
	
	
	public void addAttachment(FileUploadEvent event) {
		UploadedFile file = event.getFile();
		if (file != null && file.getContent() != null && file.getContent().length > 0 && file.getFileName() != null) {
			try {
				File attachment = new File(file.getFileName(), true);
				attachment.writeBinary(new Binary(file.getContent()));
				listAttachments.add(attachment);
			} catch (IOException e) {
				Ivy.log().error("Error when adding attachement", e);
			}
			
		}
	}
	
	public void removeAttachment(File attachment) {
		listAttachments.remove(attachment);
	}
	
	public void sendEmail() {
		email.setTo(emailRecipient);
		
		try (InputStream inStream = new ByteArrayInputStream(certificateAsString.getBytes())) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate certificate = (X509Certificate) cf.generateCertificate(inStream);
			
			try {
				EncryptedEmailSender.sendEncryptedMail(email, certificate, listAttachments);
				
				FacesContext.getCurrentInstance().addMessage("", 
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Email sent to: " + emailRecipient, ""));
			} catch (Exception e) {
				FacesContext.getCurrentInstance().addMessage("", 
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email is not sent to: " + emailRecipient, ""));
				Ivy.log().error("Error occurred while sending email.", e);
			}
		} catch (IOException | CertificateException e) {
			FacesContext.getCurrentInstance().addMessage("", 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Certificate is not valid.", ""));
			Ivy.log().error("Certificate is not valid.", e);
		}
	}
	
	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}
	/**
	 * @param hostname the hostname to set
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	/**
	 * @return the port
	 */
	public String getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(String port) {
		this.port = port;
	}
	/**
	 * @return the certificateAsString
	 */
	public String getCertificateAsString() {
		return certificateAsString;
	}
	/**
	 * @param certificateAsString the certificateAsString to set
	 */
	public void setCertificateAsString(String certificateAsString) {
		this.certificateAsString = certificateAsString;
	}
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the listAttachments
	 */
	public List<File> getListAttachments() {
		return listAttachments;
	}

	/**
	 * @param listAttachments the listAttachments to set
	 */
	public void setListAttachments(List<File> listAttachments) {
		this.listAttachments = listAttachments;
	}

	/**
	 * @return the email
	 */
	public SimpleMailMessage getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(SimpleMailMessage email) {
		this.email = email;
	}

	/**
	 * @return the emailRecipient
	 */
	public String getEmailRecipient() {
		return emailRecipient;
	}

	/**
	 * @param emailRecipient the emailRecipient to set
	 */
	public void setEmailRecipient(String emailRecipient) {
		this.emailRecipient = emailRecipient;
	}
	
}
