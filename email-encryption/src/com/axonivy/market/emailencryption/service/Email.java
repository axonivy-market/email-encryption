package com.axonivy.market.emailencryption.service;

import java.util.ArrayList;
import java.util.List;

import ch.ivyteam.ivy.scripting.objects.File;


public class Email {
	private String from;
	
	private String to;
	
	private String subject;
	
	private String content;
	
	private List<File> attachments = new ArrayList<>();

	
	/**
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * @param from the from to set
	 */
	public void setFrom(String from) {
		this.from = from;
	}

	/**
	 * @return the to
	 */
	public String getTo() {
		return to;
	}

	/**
	 * @param to the to to set
	 */
	public void setTo(String to) {
		this.to = to;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the attachments
	 */
	public List<File> getAttachments() {
		return attachments;
	}

	/**
	 * @param attachments the attachments to set
	 */
	public void setAttachments(List<File> attachments) {
		this.attachments = attachments;
	}
	
	
}
