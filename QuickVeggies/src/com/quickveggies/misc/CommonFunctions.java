package com.quickveggies.misc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CommonFunctions {
	
	public String financialYear()
	{
		
	   	 String finYr = null;
	   	 Date currDt = Calendar.getInstance().getTime();
	   	 DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	   	 String currDt_str = df.format(currDt);
	   	 String dt[]=currDt_str.split("/");
	   	 String month = dt[1];
	   	 System.out.println("month:::"+month);
	   	 String currYr = dt[2];
	   	 if(Integer.parseInt(month)==1 || Integer.parseInt(month)==2 || Integer.parseInt(month)==3)
	      	 {
	      		 Integer nxtYr = Integer.parseInt(currYr);
	      		 finYr = (Integer.parseInt(currYr)-1)+"-"+nxtYr.toString();
	      	 }
	      	 else
	      	 {
	      		 Integer nxtYr = Integer.parseInt(currYr)+1;
	      		 finYr = currYr+"-"+nxtYr.toString();
	      	 }
	   	 
	   	 return finYr;
	}
	
	public String currentMonth()
	{
	   	 Date currDt = Calendar.getInstance().getTime();
	   	 DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	   	 String currDt_str = df.format(currDt);
	   	 String dt[]=currDt_str.split("/");
	   	 String month = dt[1];
	   	 
	   	 
	   	 return month;
	}
	
	public String currentYear()
	{

	   	 Date currDt = Calendar.getInstance().getTime();
	   	 DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	   	 String currDt_str = df.format(currDt); 	 
	   	 String dt[]=currDt_str.split("/");
	   	 String currYr = dt[2];
	 
	   	 
	   	 return currYr;
	}
	
	public String currentTime_withtimeZone()
	{
		 Date currDt = Calendar.getInstance().getTime();
      	 //DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
      	 //String currDt_str = df.format(currDt);
   	     String currDt_withTimeZone = currDt.toString();
   	     
   	     return currDt_withTimeZone;
	}

}
