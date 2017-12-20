package com.ai_int.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.quickveggies.UserGlobalParameters;

public class EmailUtil {

	private static final String email_api_key = "e6be3a9f-9421-459f-80b7-b2d675bdcc0e";

	private static final String email_pub_acct_id = "6d95e2a4-08f8-4e82-af65-b055e1dd77d6";

	private static final String sender_email = "supersalesagro@gmail.com";

	private static final String sender_email_pwd = "e6be3a9f-9421-459f-80b7-b2d675bdcc0e ";

	private static final String smtp_main_server = "smtp.elasticemail.com";

	private static final Integer smtp_main_server_port = 2525;

	private static final String smtp_alternate_server = "smtp25.elasticemail.com";

	private static final Integer smtp_alternate_server_port = 25;

	private static String user = sender_email;

	private static final String SENDER_NAME = "Quick Veggies";

	public static void main(String[] args) {

		// sendEmail("shoebkhan09@gmail.com", "QV Test email", "Shoeb,
		// This is sample email., \n\n\n\n\n\n\n");

		// send("supersalesagro@gmail.com", "Vaibhav, This is new sample test
		// email., \n\n\n\n\n\n\n", "QV Test email" );
		send("shoebkhan09@gmail.com", "Shoeb, This is new sample test email., \n\n\n\n\n\n\n", "QV Test email", null);

	}

	public static void send(String to, String body, String subject, String filename) {
		// sendEmailUsingPostAPI(SENDER_NAME, email_api_key, sender_email,
		// SENDER_NAME, subject, body, to,"false");

		sendEmailUsingSMTP(to, body, subject, filename);

	}

	private static String sendEmailUsingSMTP(String to,  String body, String subject, String fileName) {
		Properties props = new Properties();
		/*
		 * props.put("mail.smtp.host", smtp_main_server);
		 * props.put("mail.smtp.port", smtp_main_server_port);
		 */
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		Session session = Session.getDefaultInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				// return new PasswordAuthentication(sender_email,
				// sender_email_pwd);
				//return new PasswordAuthentication("SuperSalesAgro@gmail.com", "demo1234");
				return new PasswordAuthentication(UserGlobalParameters.userEmail, UserGlobalParameters.userPwd);

			}
		});

		// Compose the message 
		try {
			Multipart multipart = new MimeMultipart();
			if (fileName != null) {
				MimeBodyPart filePart = new MimeBodyPart();
				DataSource source = new FileDataSource(fileName);
				filePart.setDataHandler(new DataHandler(source));
				filePart.setFileName(fileName);
				filePart.setHeader("Content-Transfer-Encoding", "base64");
				filePart.setHeader("Content-Type", "application/pdf");
				multipart.addBodyPart(filePart);
			}

			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setHeader("MIME-Version", "1.0");
			textPart.setHeader("Content-Type", "text/html");
			textPart.setText(body);

			multipart.addBodyPart(textPart);

			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(user));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject(subject);
			message.setContent(multipart);

			// send the message  
			Transport.send(message);
			return "Message sent successfully...";
		} catch (Exception x) {
			x.printStackTrace();
			return x.getMessage();
		}

	}

	/**
	 * FIXME: Elastic mail doesn't support emails with attachments
	 * @param userName
	 * @param apiKey
	 * @param from
	 * @param fromName
	 * @param subject
	 * @param body
	 * @param to
	 * @param isTransactional
	 * @return
	 */
	public static String sendEmailUsingPostAPI(String userName, String apiKey, String from, String fromName,
			String subject, String body, String to, String isTransactional) {

		try {

			ArrayList<FileData> files = new ArrayList<>();
			String filename = "Buyer Deals _1492491403904.pdf";

			Path path = Paths.get("C:\\Users\\Shoeb\\AppData\\Local\\Temp\\" + filename);
			byte[] data = null;
			try {
				data = Files.readAllBytes(path);
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			FileData contactsFile = new FileData();
			contactsFile.contentType = "application/pdf"; // Change
															// correspondingly
															// to the file type
			contactsFile.fileName = filename;
			contactsFile.content = data;
			files.add(contactsFile);

			String encoding = "UTF-8";
			Map<String, String> values = new LinkedHashMap<>();
			/*
			 * values.put("apikey" , URLEncoder.encode(apiKey, encoding));
			 * values.put("from" , URLEncoder.encode(from, encoding));
			 * values.put("fromName" , URLEncoder.encode(fromName, encoding));
			 * values.put("subject" , URLEncoder.encode(subject, encoding));
			 * values.put("bodyHtml" , URLEncoder.encode(body, encoding));
			 * values.put("to" , URLEncoder.encode(to, encoding));
			 * values.put("isTransactional", URLEncoder.encode(isTransactional,
			 * encoding));
			 */
			values.put("apikey", apiKey);
			values.put("from", from);
			values.put("fromName", fromName);
			values.put("subject", subject);
			values.put("bodyHtml", body);
			values.put("to", to);
			values.put("isTransactional", isTransactional);

			String boundary = String.valueOf(System.currentTimeMillis());
			byte[] boundarybytes = ("\r\n--" + boundary + "\r\n").getBytes(Charset.forName("ASCII"));

			URL url = new URL("https://api.elasticemail.com/v2/email/send");
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			conn.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			String formdataTemplate = "Content-Disposition: form-data; name=\"%s\"\r\n\r\n%s";
			for (String key : values.keySet()) {
				wr.write(boundarybytes, 0, boundarybytes.length);
				String formitem = String.format(formdataTemplate, key, values.get(key));
				byte[] formitembytes = formitem.getBytes(Charset.forName("UTF8"));
				wr.write(formitembytes, 0, formitembytes.length);
			}

			if (files != null) {
				for (FileData file : files) {
					wr.write(boundarybytes, 0, boundarybytes.length);

					String headerTemplate = "Content-Disposition: form-data; name=\"filefoobarname\"; filename=\"%s\"\r\nContent-Type: %s\r\n\r\n";
					String header = String.format(headerTemplate, file.fileName, file.contentType);
					byte[] headerbytes = header.getBytes(Charset.forName("UTF8"));
					wr.write(headerbytes, 0, headerbytes.length);
					wr.write(file.content, 0, file.content.length);
				}
			}

			byte[] trailer = ("\r\n--" + boundary + "--\r\n").getBytes(Charset.forName("ASCII"));
			wr.write(trailer, 0, trailer.length);
			wr.flush();
			wr.close();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String result = rd.readLine();
			wr.close();
			rd.close();
			System.out.println(result);
			return result;
		}

		catch (Exception e) {

			e.printStackTrace();
			return e.getMessage();
		}
	}

}
