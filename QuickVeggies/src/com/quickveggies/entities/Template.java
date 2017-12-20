package com.quickveggies.entities;

public class Template {

	private String accountName;
	private int dateCol;
	private int chqnoCol, descriptionCol, withdrawalCol, depositCol, balanceCol, transIdCol;

	public Template(String accountName, int transIdCol, int dateCol, int chqnoCol, int descriptionCol, int withdrawalCol,
			int depositCol, int balanceCol) {
		setAccountName(accountName);
		setDateCol(dateCol);
		setChqnoCol(chqnoCol);
		setDescriptionCol(descriptionCol);
		setWithdrawalCol(withdrawalCol);
		setDepositCol(depositCol);
		setBalanceCol(balanceCol);
		setTransIdCol(transIdCol);
	}

	public int[] getColsIndexesArray() {
		return new int[] { transIdCol, dateCol, chqnoCol, descriptionCol, withdrawalCol, depositCol, balanceCol };
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public int getDateCol() {
		return dateCol;
	}

	public void setDateCol(int dateCol) {
		this.dateCol = dateCol;
	}

	public int getChqnoCol() {
		return chqnoCol;
	}

	public void setChqnoCol(int chqnoCol) {
		this.chqnoCol = chqnoCol;
	}

	public int getDescriptionCol() {
		return descriptionCol;
	}

	public void setDescriptionCol(int descriptionCol) {
		this.descriptionCol = descriptionCol;
	}

	public int getWithdrawalCol() {
		return withdrawalCol;
	}

	public void setWithdrawalCol(int withdrawalCol) {
		this.withdrawalCol = withdrawalCol;
	}

	public int getDepositCol() {
		return depositCol;
	}

	public void setDepositCol(int depositCol) {
		this.depositCol = depositCol;
	}

	public int getBalanceCol() {
		return balanceCol;
	}

	public void setBalanceCol(int balanceCol) {
		this.balanceCol = balanceCol;
	}

	public int getTransIdCol() {
		return transIdCol;
	}

	public void setTransIdCol(int transIdCol) {
		this.transIdCol = transIdCol;
	}
}
