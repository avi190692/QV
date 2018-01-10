package com.quickveggies.entities;

import java.util.Date;

public class AccountMaster {
	
	  private int accountmaster_id;
	  private String accountcode;
	  private String accountname;
	  private double amount;
	  private String fin_year;
	  private String creation_date;
	  private String report_flag;
	  private boolean active_flag;
	  private String month;
	  
	  
	public AccountMaster() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public AccountMaster(String accountcode, String accountname, double amount, String fin_year,
                         String creation_date, String report_flag, boolean active_flag, String month) {
		super();
		
		this.accountcode = accountcode;
		this.accountname = accountname;
		this.amount = amount;
		this.fin_year = fin_year;
		this.creation_date = creation_date;
		this.report_flag = report_flag;
		this.active_flag = active_flag;
		this.month = month;
		}

	public AccountMaster(int accountmaster_id, String accountcode, String accountname, double amount, String fin_year,
			             String creation_date, String report_flag, boolean active_flag, String month) {
		super();
		this.accountmaster_id = accountmaster_id;
		this.accountcode = accountcode;
		this.accountname = accountname;
		this.amount = amount;
		this.fin_year = fin_year;
		this.creation_date = creation_date;
		this.report_flag = report_flag;
		this.active_flag = active_flag;
		this.month = month;
	}
	
	public int getAccountmaster_id() {
		return accountmaster_id;
	}
	public void setAccountmaster_id(int accountmaster_id) {
		this.accountmaster_id = accountmaster_id;
	}
	public String getAccountcode() {
		return accountcode;
	}
	public void setAccountcode(String accountcode) {
		this.accountcode = accountcode;
	}
	public String getAccountname() {
		return accountname;
	}
	public void setAccountname(String accountname) {
		this.accountname = accountname;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public String getFin_year() {
		return fin_year;
	}
	public void setFin_year(String fin_year) {
		this.fin_year = fin_year;
	}

	public String getReport_flag() {
		return report_flag;
	}
	public void setReport_flag(String report_flag) {
		this.report_flag = report_flag;
	}
	public boolean isActive_flag() {
		return active_flag;
	}
	public void setActive_flag(boolean active_flag) {
		this.active_flag = active_flag;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getCreation_date() {
		return creation_date;
	}

	public void setCreation_date(String creation_date) {
		this.creation_date = creation_date;
	}
	
	
	
	
	  
	  
	  

}
