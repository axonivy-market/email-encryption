package com.axonivy.market.emailencryption.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import com.axonivy.market.emailencryption.service.Email;
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
	
	private Email email = new Email();
	
	private String certificateAsString;
	
	
	public EncryptedEmailSenderBean() throws IvyException {
		try {
			MailClientConfig mailClientConfig = EncryptedEmailSender.mailSetup();
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
				email.getAttachments().add(attachment);
			} catch (IOException e) {
				Ivy.log().error("Error when adding attachement", e);
			}
			
		}
	}
	
	public void removeAttachment(File attachment) {
		email.getAttachments().remove(attachment);
	}
	
	public void sendEmail() {
		try (InputStream inStream = new ByteArrayInputStream(certificateAsString.getBytes())) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate certificate = (X509Certificate) cf.generateCertificate(inStream);
			
			EncryptedEmailSender.sendEncryptedMail(email, certificate);
			
			FacesContext.getCurrentInstance().addMessage("", 
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Email sent to: " + email.getTo(), ""));
		} catch (Exception e) {
			Ivy.log().error("Error occurred while sending email.", e);
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
	 * @return the email
	 */
	public Email getEmail() {
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(Email email) {
		this.email = email;
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
	
	
}
