package com.quickveggies;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserGlobalParameters {
	public final static String userEmail = "SuperSalesAgro@gmail.com";// "crobuk@gmail.com";
	public final static String userPwd = "demo1234";// "102938475";
	
	private static final String qvPath = System.getProperty("user.home") + File.separator + "QVSMSTemplate.txt";
	public static final String qvprofileImagePath = System.getProperty("user.dir") + File.separator + "resources" + File.separator + "images" + File.separator + "profileImage" + File.separator;
	
	// PARAMETERS FOR LOGIN INTO SQL SERVER FROM JAVA LIBRARY JDBC
	// jdbc:sqlserver://[serverName[\instanceName][:portNumber]][;property=value[;property=value]]
	//public static String SQLURL = "127.0.0.1";
        public static String SQLURL = "localhost";
	public static String SQLURL2 = "192.168.1.110";
	// public static String SQLUSER = "qvuser";
	// public static String SQLPASS = "qvdbusr123";

	public static String SQLUSER = "postgres";
	public static String SQLPASS = "root";
	//SMS sender ID 
	public static final String SMS_SENDER_ID = "QIKVEG"; // 777777 ,  QIKVEG -- 333000 NA
	//SMS Authentication Key
	public static final String SMS_AUTH_KEY = "84044AwKDEHKgbhjt554ef84d";

	public static String[] appleQualitiesList = new String[] { "Royal Supreme", "Red Gold", "Golden", "AAA" };

	public static String[] kinnowMangoQualitiesList = new String[] { "Malcom", "Bonjour", "Merrick" };

	public static final String[] creditPeriodSource = { "1 day", "3 days", "7 days", "15 days", "30 days", "45 days" };

	public static String[] boxSizes = new String[] { "24", "32", "42", "45", "54", "60", "72", "84", "96", "108",
			"120" };

	public static final String[] buyerTypes = new String[] { "Ladaan", "Bijak", "Regular" };

	public static Map<Integer, PaymentMethodSource> getPaymentMethodMap() {
		Map<Integer, PaymentMethodSource> map = new LinkedHashMap<>();
		for (int i = 0; i < PaymentMethodSource.values().length; i++) {
			map.put(i + 1, PaymentMethodSource.values()[i]);
		}
		return map;
	}
	
	private static String SMS_TEMPLATE;
	
	public static String GET_SMS_TEMPLATE() {
		if (SMS_TEMPLATE == null) {
			StringBuilder sb = new StringBuilder();
			try {
				byte[] data = Files.readAllBytes(Paths.get(qvPath));
				SMS_TEMPLATE = new String(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return SMS_TEMPLATE;
	}
        
	/**
	 * 
	 * @param newTemplate
	 * @param update  Does not update the file if set to false
	 */
	public static void SET_SMS_TEMPLATE(String newTemplate, boolean update) {
			try {
				if (!new File(qvPath).exists() || update)
					Files.write(Paths.get(qvPath), newTemplate.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public static void main(String args[]) {
		//SET_SMS_TEMPLATE("blah", false);
		System.out.println(GET_SMS_TEMPLATE());
	}
}
