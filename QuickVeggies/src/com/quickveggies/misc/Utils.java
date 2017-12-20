package com.quickveggies.misc;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ai.util.dates.DateUtil;

public class Utils {

	private static String dateFormat;

	private static final LocalDate NOW = LocalDate.now();

	public static LocalDate toDate(String strDate) {
		if (dateFormat == null) {
			dateFormat = DateUtil.determineDateFormat(strDate);
			if (dateFormat == null)
				System.out.println("Error determing date format");
		}
		try {
			if (dateFormat == null)
				return new java.sql.Date(DateUtil.parse(strDate).getTime()).toLocalDate();
			else
				return new java.sql.Date(DateUtil.parse(strDate, dateFormat).getTime()).toLocalDate();
		} catch (ParseException pe) {
			throw new IllegalArgumentException("Failed to parse the date");
		}
	}

	/**
	 * Returns true if the start date, plus the number of days fall after today
	 * or after today
	 * 
	 * @param startDate
	 * @param days
	 * @return
	 */
	public static boolean isUnderDateRange(String startDate, int days) {
		LocalDate dealDate = toDate(startDate);
		LocalDate crEndDate = dealDate.plusDays(days);
		if (NOW.isBefore(crEndDate)) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * Converts long to string format
	 * 
	 * @param num
	 * @return
	 */
	public static String toStr(Long num) {
		return String.valueOf(num);
	}

	/**
	 * Converts integer to string format
	 * 
	 * @param num
	 * @return
	 */
	public static String toStr(Integer num) {
		return String.valueOf(num);
	}

	/**
	 * Converts to the given nmeric string to number, may throw number format
	 * exception
	 * 
	 * @param str
	 * @return
	 */
	public static Integer toInt(String str) {
		str = str.trim();
		return Integer.valueOf(Double.valueOf(str).intValue());
	}
	
	/**
	 * Converts to the given nmeric string to Double, may throw number format
	 * exception
	 * 
	 * @param str
	 * @return
	 */
	public static Double toDbl(String str) {
		str = str.trim();
		return Double.valueOf(str);
	}


	/**
	 * Returns true if string is null or is an empty one, or is having value as
	 * 0
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumStrEmpty(String str) {
		if (str == null || str.trim().isEmpty() || str.trim().equals("0")) {
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the string is null or is an empty one after removing leading and trailing spaces
	 * 
	 */
	public static boolean isEmptyString(String str) {
		if (str == null || str.trim().isEmpty()) {
			return true;
		}
		return false;
	}

	public static Double extractNumberFromString(String strNum) {
	//	System.out.println("Parsing string ".concat(strNum));
		String[] charsToRemove = new String[] { ",", "-", "#", " " };
		if (isNumStrEmpty(strNum)) {
			return 0d;
		}
		strNum = strNum.trim();
		for (String ch : charsToRemove) {
			strNum = strNum.trim().replaceAll(ch, "");
		}
		Pattern p = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");
		Matcher m = p.matcher(strNum);
		StringBuilder builder = new StringBuilder();
	
		while (m.find()) {
			builder.append(m.group());
		}
		return Double.parseDouble(builder.toString());
	}

}
