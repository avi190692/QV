package com.quickveggies.misc;

//File Name SendEmail.java

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

import com.quickveggies.UserGlobalParameters;

import javax.activation.*;

public class SendMail{
	
private SendMail(){}
	
public static void send(String to,String from, String host,String msgSubject,String textMessage)
{    
   // Recipient's email ID needs to be mentioned.
   //String to = "abcd@gmail.com";

   // Sender's email ID needs to be mentioned
   //String from = "web@gmail.com";

   // Assuming you are sending email from localhost
   //String host = "localhost";

   // Get system properties
   Properties properties = System.getProperties();
   properties.put("mail.smtp.auth", "true");
   properties.put("mail.smtp.starttls.enable", "true");
   properties.put("mail.smtp.host", "smtp.gmail.com");
   properties.put("mail.smtp.port", "587");

   Session session = Session.getDefaultInstance(properties, 
                        new Authenticator(){
                           protected PasswordAuthentication getPasswordAuthentication() {
                              return new PasswordAuthentication(UserGlobalParameters.userEmail, UserGlobalParameters.userPwd);
                           }});

	try {

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.setRecipients(Message.RecipientType.TO,
			InternetAddress.parse(to));
		message.setSubject(msgSubject);
		message.setText(textMessage);

		Transport.send(message);

		System.out.println("Done");

	} catch (MessagingException e) {
		throw new RuntimeException(e);
	}
}
}
