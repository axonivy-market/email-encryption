package com.axonivy.utils.email.encryption.test;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import com.axonivy.ivy.webtest.IvyWebTest;
import com.axonivy.ivy.webtest.engine.EngineUrl;
import com.codeborne.selenide.SelenideElement;


/**
 * This WebTest:
 * <ul>
 * <li>starts the SendEncryptedEmail process</li>
 * <li>fill in the email form</li>
 * <li>set the public key of the receiver</li>
 * <li>and send the email</li>
 * </ul>
 * 
 */
@IvyWebTest(headless = true)
public class SendEncryptedEmailWebIT {

	@Test
	public void fillDialogForm() {
		// valid links can be copied from the start page of the internal web-browser
		open(EngineUrl.createProcessUrl("email-encryption-demo/18442CF71BAC8A4F/start.ivp"));

		$(By.id("form:emailFromInputText")).sendKeys("noreply@ivyserver.local");
		
		$(By.id("form:emailToInputText")).sendKeys("tester.webit@ivyserver.local");
		
		$(By.id("form:emailSubjectInputText")).sendKeys("Test Encrypted Email");
		
		try(InputStream certificate = SendEncryptedEmailWebIT.class.getResourceAsStream("encrypted.email.crt")) {
			$(By.id("form:certificateInputTextarea")).sendKeys(IOUtils.toString(certificate, StandardCharsets.UTF_8));
		} catch (IOException e) {
		}
		$(By.id("form:emailContentInputTextEditor")).scrollTo().shouldBe(enabled).click();
		SelenideElement contentEditor = $(By.id("form:emailContentInputTextEditor_editor")).$("div");
		contentEditor.sendKeys("Hi Tester WebIT,");
		contentEditor.pressEnter();
		contentEditor.sendKeys("This is an encrypted email sent to you by an Axon Ivy Integration Test.");
		contentEditor.pressEnter();
		contentEditor.sendKeys("Best regards,");
		contentEditor.pressEnter();
		contentEditor.sendKeys("Axon Ivy AG.");
		// verify that the sendEmail button is enabled, before clicking it.
		$(By.id("form:sendEmailButton")).shouldBe(enabled).click();
	}

}