package com.axonivy.utils.email.encryption.service;

import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.bouncycastle.operator.OutputEncryptor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.mail.MailClientConfig;
import ch.ivyteam.ivy.mail.MailClientConfigProvider;
import ch.ivyteam.ivy.mail.MailConstants.EmailEncryption;
import ch.ivyteam.ivy.scripting.objects.File;
import ch.ivyteam.ivy.security.exec.Sudo;

/**
 * Service to send encrypted mails.
 * 
 */
public class EncryptedEmailSender {
	private static JavaMailSender mailSender;
	
	
	/**
	 * Sends email encrypted by given certificate to the provided receiver.
	 * 
	 * @param mail
	 * @param certificate
	 * @param listAttachments
	 * @throws Exception
	 */
	public static void sendEncryptedMail(SimpleMailMessage mail, X509Certificate certificate, 
			List<File> listAttachments) throws Exception {
		if (mail == null || mail.getTo() == null || mail.getTo().length == 0 || 
				StringUtils.isBlank(mail.getTo()[0]) || certificate == null) {
			throw new Exception("Invalid mail settings for encrypted mail sending.");
		}
		
		ClassLoader csBackup = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(EncryptedEmailSender.class.getClassLoader());
		
		initMailSender();
		
		MimeMessage messageToBeEncrypted = mailSender.createMimeMessage();
		MimeMessageHelper bodyMultipart = new MimeMessageHelper(messageToBeEncrypted, MimeMessageHelper.MULTIPART_MODE_MIXED);
		bodyMultipart.setText(mail.getText(), true);
		
		if (listAttachments != null) {
			for (File attachment: listAttachments) {
				try {
					bodyMultipart.addAttachment(attachment.getName(), 
							new ByteArrayResource(attachment.readBinary().toByteArray()));
				} catch (MessagingException e) {
					Ivy.log().error("Error when adding attachements.", e);
				}
			}
		}
		
		MimeBodyPart contentPart = generateEncryptedMessage(messageToBeEncrypted, certificate);
		
		MimeMessage message = mailSender.createMimeMessage();
		message.setFrom(new InternetAddress(mail.getFrom()));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(mail.getTo()[0]));
		message.setSubject(mail.getSubject());
		message.setContent(contentPart.getContent(), contentPart.getContentType());
		message.saveChanges();
		
		mailSender.send(message);
		
		Thread.currentThread().setContextClassLoader(csBackup);
	}

	private static MimeBodyPart generateEncryptedMessage(MimeMessage message, X509Certificate certificate) throws Exception {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.insertProviderAt(new BouncyCastleProvider(), 1);
		}
		
		SMIMEEnvelopedGenerator generator = new SMIMEEnvelopedGenerator();
		JceKeyTransRecipientInfoGenerator keyGenerator = new JceKeyTransRecipientInfoGenerator(certificate);
		keyGenerator.setProvider(BouncyCastleProvider.PROVIDER_NAME);
		generator.addRecipientInfoGenerator(keyGenerator);
		OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(CMSAlgorithm.RC2_CBC)
				.setProvider(BouncyCastleProvider.PROVIDER_NAME).build();
		
		return generator.generate(message, encryptor);
	}
	
	/**
	 * Gets properties for mail sending.
	 * 
	 * @param mailConfig
	 * @throws Exception
	 */
	private static Properties getJavaMailProperties(MailClientConfig mailConfig) {
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		if (mailConfig.encryption() == EmailEncryption.SSL) {
			props.put("mail.smtp.socketFactory.port", mailConfig.port());
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		}
		else if (mailConfig.encryption() == EmailEncryption.START_TLS) {
			props.put("mail.smtp.starttls.enable", "true");
		}
		return props;
	}

	/**
	 * @return the mail configuration: designer + engine aware, so that mail sending 
	 * with test-preferences is possible.
	 * 
	 * @throws Exception
	 */
	public static MailClientConfig getMailSetup() throws Exception {
		return Sudo.call(new Callable<MailClientConfig>() {
			@Override
			public MailClientConfig call() throws Exception {
				return MailClientConfigProvider.get();
			}
		});
	}
	
	private static void initMailSender() throws Exception {
		if(mailSender == null) {
			MailClientConfig mailConfig = getMailSetup();
			JavaMailSenderImpl initMailSender = new JavaMailSenderImpl();
			initMailSender.setHost(mailConfig.host());
			initMailSender.setPort(mailConfig.port());
			initMailSender.setUsername(mailConfig.user());
			initMailSender.setPassword(Objects.toString(mailConfig.password(), ""));
			initMailSender.setJavaMailProperties(getJavaMailProperties(mailConfig));
			mailSender = initMailSender;
		}
	}
}
