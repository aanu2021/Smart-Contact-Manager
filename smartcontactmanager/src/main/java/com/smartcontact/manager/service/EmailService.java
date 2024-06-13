package com.smartcontact.manager.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
	public boolean sendEmail(String message, String subject, String to, String from) {

		String host = "smtp.gmail.com";

		Properties properties = System.getProperties();
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");

		// Step 1 : To get the session object 
		Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("anumoynandyanunan2019@gmail.com", "GENERATED_PASSWORD");
			}
		});
		
		session.setDebug(true);
		
		// Step 2 : Compose the message
		MimeMessage mimeMessage = new MimeMessage(session);
		try {
			mimeMessage.setFrom(from);
			mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			mimeMessage.setContent(message,"text/html");
			mimeMessage.setSubject(subject);
			
			// Step 3 : Send the message using Transport class
			Transport.send(mimeMessage);
			System.out.println("Sent successfully...");
			return true;
			
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}

		return false;
	}
}
