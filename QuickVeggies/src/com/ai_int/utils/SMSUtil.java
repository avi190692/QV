package com.ai_int.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.quickveggies.UserGlobalParameters;

public class SMSUtil {

	public static void main(String... strings) {
		String msg = "test";
		String[] receivers = new String[]{"+91 7972509 759"};
		sendMessage(msg, receivers);
	}

	/**
	 * 
	 * @param sms - Message to send
	 * @param receivers  - Listing of phone numbers
	 */
	public static void sendMessage(String sms,  String... receivers) {
		// Sender ID,While using route4 sender id should be 6 characters long.
		String senderId = UserGlobalParameters.SMS_SENDER_ID;
		// Your authentication key
		String authkey = UserGlobalParameters.SMS_AUTH_KEY;
		String mobiles = "";
		for (String mobile : receivers) {
			if (mobile != null && !mobile.isEmpty()) {
				mobile = mobile.trim();
				mobile = mobile.replaceAll("\\s", "");
				mobile = mobile.replaceAll("\\+", "");
				mobiles = mobiles.concat(mobile).concat(",");
			}
		}
		//Multiple mobile numbers separated by comma
		mobiles = mobiles.trim();
		if (mobiles.endsWith(",")) {
			mobiles.substring(0, mobiles.lastIndexOf(","));
		}
		// define route
		String route = "4";

		// Prepare Url
		URLConnection myURLConnection = null;
		URL myURL = null;
		BufferedReader reader = null;

		// encoding message
		String encoded_message = sms;
		try {
			encoded_message = URLEncoder.encode(sms,StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Send SMS API
		String mainUrl = "https://control.msg91.com/api/sendhttp.php?";

		// Prepare parameter string
		StringBuilder sbPostData = new StringBuilder(mainUrl);
		sbPostData.append("authkey=" + authkey);
		sbPostData.append("&mobiles=" + mobiles);
		sbPostData.append("&message=" + encoded_message);
		sbPostData.append("&route=" + route);
		sbPostData.append("&sender=" + senderId); 
		// sbPostData.append("&country=" + country);  // add this if country code is missing
		// final string
		mainUrl = sbPostData.toString();
		try {
			// prepare connection
			myURL = new URL(mainUrl);
			myURLConnection = myURL.openConnection();
			myURLConnection.connect();
			reader = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
			// reading response
			String response;
			while ((response = reader.readLine()) != null)
				// print response
				System.out.println(response);

			// finally close connection
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
