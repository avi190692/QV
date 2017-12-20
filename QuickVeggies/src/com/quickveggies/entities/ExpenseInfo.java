package com.quickveggies.entities;

public class ExpenseInfo {

	private int id;

	private String name;

	private String type;

	private String defaultAmount;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDefaultAmount() {
		return defaultAmount;
	}

	public void setDefaultAmount(String defaultAmount) {
		this.defaultAmount = defaultAmount;
	}

}
