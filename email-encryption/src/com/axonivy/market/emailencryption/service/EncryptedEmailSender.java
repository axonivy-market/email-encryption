package com.axonivy.market.emailencryption.service;

import java.io.UnsupportedEncodingException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.operator.OutputEncryptor;
import org.eclipse.core.resources.IProject;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.mail.MailClientConfig;
import ch.ivyteam.ivy.mail.MailClientConfigProvider;
import ch.ivyteam.ivy.mail.MailConstants.EmailEncryption;
import ch.ivyteam.ivy.project.IIvyProject;
import ch.ivyteam.ivy.scripting.objects.File;
import ch.ivyteam.ivy.security.SecurityManagerFactory;
import ch.ivyteam.util.IvyException;

/**
 * Service to send encrypted mails.
 * 
 */
public class EncryptedEmailSender {

	/**
	 * Sends email encrypted by given certificate to provided receiver.
	 * 
	 * @param mail
	 * @param certificate
	 * @throws Exception
	 */
	public static void sendEncryptedMail(Email mail, X509Certificate certificate) throws Exception {
		if (mail == null || StringUtils.isBlank(mail.getTo()) || certificate == null) {
			throw new IvyException("Invalid mail settings for encrypted mail sending.");
		}

		ClassLoader csBackup = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(EncryptedEmailSender.class.getClassLoader());

		MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		mc.addMailcap(
				"multipart/*;;x-java-content-handler=com.sun.mail.handlers.multipart_mixed;x-java-fallback-entry=true");
		CommandMap.setDefaultCommandMap(mc);

		MimeMultipart bodyMultipart = new MimeMultipart();

		MimeBodyPart bodyMessagePart = new MimeBodyPart();
		bodyMessagePart.setText(mail.getContent(), "UTF-8", "html");
		bodyMultipart.addBodyPart(bodyMessagePart);

		for (File attachment: mail.getAttachments()) {
			try {
				addAttachment(bodyMultipart, attachment);
			} catch (UnsupportedEncodingException e) {
				Ivy.log().error(e.toString());
			}
		}
		// Get a Session object and create the mail message
		Properties props = null;
		Session session = null;

		try {
			MailClientConfig emailConfig = mailSetup();
			props = getProperties(emailConfig);
			session = getSession(props, emailConfig);
		} catch (Exception e) {
			throw new IvyException(e);
		}

		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.insertProviderAt(new BouncyCastleProvider(), 1);
		}

		SMIMEEnvelopedGenerator gen = new SMIMEEnvelopedGenerator();
		JceKeyTransRecipientInfoGenerator keyGen = new JceKeyTransRecipientInfoGenerator(certificate);
		keyGen.setProvider(BouncyCastleProvider.PROVIDER_NAME);
		gen.addRecipientInfoGenerator(keyGen);
		OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(CMSAlgorithm.RC2_CBC)
				.setProvider(BouncyCastleProvider.PROVIDER_NAME).build();

		MimeMessage messageToEncrypt = new MimeMessage(session);
		messageToEncrypt.setContent(bodyMultipart);
		messageToEncrypt.saveChanges();
		MimeBodyPart contentPart = gen.generate(messageToEncrypt, encryptor);

		Address fromUser = new InternetAddress(StringUtils.remove(mail.getFrom(), ";"));
		Address toUser = new InternetAddress(StringUtils.remove(mail.getTo(), ";"));

		MimeMessage message = new MimeMessage(session);
		message.setFrom(fromUser);
		message.setRecipient(Message.RecipientType.TO, toUser);
		message.setSubject(mail.getSubject());
		message.setContent(contentPart.getContent(), contentPart.getContentType());
		message.saveChanges();

		Transport.send(message);
		Thread.currentThread().setContextClassLoader(csBackup);
	}

	/**
	 * Adds attachment to multipart
	 * 
	 * @param multipart
	 * @param ivyFile
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 * @throws SMIMEException
	 */
	private static void addAttachment(MimeMultipart multipart, File ivyFile)
			throws MessagingException, UnsupportedEncodingException, SMIMEException {
		DataSource source = new FileDataSource(ivyFile.getJavaFile());
		MimeBodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(ivyFile.getName());

		multipart.addBodyPart(messageBodyPart);
	}

	/**
	 * Gets session for mail sending.
	 * 
	 * @param props
	 * @param emailConfig
	 * @throws Exception
	 */
	private static Session getSession(Properties props, MailClientConfig emailConfig) throws Exception {
		if (emailConfig.encryption() == EmailEncryption.SSL || emailConfig.encryption() == EmailEncryption.START_TLS) {
			Authenticator authenticator = new Authenticator() {
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(emailConfig.user(), emailConfig.password());
				}
			};

			return Session.getInstance(props, authenticator);
		} else {
			return Session.getInstance(props);
		}
	}

	/**
	 * Gets properties for mail sending.
	 * 
	 * @param emailConfig
	 * @throws Exception
	 */
	private static Properties getProperties(MailClientConfig emailConfig) throws Exception {
		Properties props = new Properties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.host", emailConfig.host());
		props.put("mail.smtp.port", emailConfig.port());

		if (emailConfig.encryption() == EmailEncryption.SSL || emailConfig.encryption() == EmailEncryption.START_TLS) {
			if (emailConfig.encryption() == EmailEncryption.SSL) {
				props.put("mail.smtp.socketFactory.port", emailConfig.port());
				props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
				props.put("mail.smtp.auth", "true");
			} else if (emailConfig.encryption() == EmailEncryption.START_TLS) {
				props.put("mail.smtp.starttls.enable", "true");
				props.put("mail.smtp.auth", "true");
			}
		}

		return props;
	}

	/**
	 * @return the mail configuration: designer+engine aware, so that mail sending
	 *         with test-preferences is possible!
	 * @throws Exception
	 */
	public static MailClientConfig mailSetup() throws Exception {
		return SecurityManagerFactory.getSecurityManager().executeAsSystem(new Callable<MailClientConfig>() {
			@Override
			public MailClientConfig call() throws Exception {
				IProject project = Ivy.request().getProcessModelVersion().getProject();
				IIvyProject ivyProject = (IIvyProject) project.getAdapter(IIvyProject.class);
				return MailClientConfigProvider.get(ivyProject);
			}
		});
	}
}
