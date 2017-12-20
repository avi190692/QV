package com.quickveggies.entities;

public class Account {

    private int Id;
    private String accountNumber;
    private int accountType;
    private double balance;
    private double initBalance;
    private String accountName;
    private String bankName;
    private String phone;
    private String description;
    private int lastupdated;

    public Account(int id, String number, int accountType,double balance,double initBalance,String accountName,
    		String bankName,String phone,String description,int lastupdated) {
        Id = id;
        this.accountNumber = number;
        this.accountType = accountType;
        this.balance = balance;
        this.accountName=accountName;
        this.bankName=bankName;
        this.description=description;     
        this.phone = phone;
        this.lastupdated=lastupdated;
        this.setInitBalance(initBalance);
    }

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public int getAccountType() {
		return accountType;
	}

	public void setAccountType(int accountType) {
		this.accountType = accountType;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getLastupdated() {
		return lastupdated;
	}

	public void setLastupdated(int lastupdated) {
		this.lastupdated = lastupdated;
	}

	public double getInitBalance() {
		return initBalance;
	}

	public void setInitBalance(double initBalance) {
		this.initBalance = initBalance;
	}

}
