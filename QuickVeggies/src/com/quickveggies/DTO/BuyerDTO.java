package com.quickveggies.DTO;

import org.apache.poi.util.StringUtil;

import com.itextpdf.text.pdf.StringUtils;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BuyerDTO {
	private StringProperty email;
	private DoubleProperty milestone;
	private StringProperty name;
	private StringProperty company;
	private StringProperty creditPerioad;
	private DoubleProperty budgetProgress;
	private StringProperty buyerType;
	private StringProperty buyerAction;

	public BuyerDTO(String email,Double milestone, String name, String company, String creditPerioad, Double budgetProgress) {
		this.email = new SimpleStringProperty(email);
		this.name = new SimpleStringProperty(name);
		this.milestone = new SimpleDoubleProperty(milestone);
		this.company = (company.isEmpty())? new SimpleStringProperty("N/A") : new SimpleStringProperty(company);
		this.creditPerioad = (creditPerioad.isEmpty())? new SimpleStringProperty("N/A") : new SimpleStringProperty(creditPerioad);
		this.budgetProgress =  (budgetProgress == null)? new SimpleDoubleProperty(0.0) : new SimpleDoubleProperty(budgetProgress);
		this.buyerType = new SimpleStringProperty("Buyer");
		this.buyerAction = new SimpleStringProperty("View");
	}

	public StringProperty getBuyerTypeProp() {
		return buyerType;
	}

	public String getBuyerType() {
		return buyerType.get();
	}
	
	public void setBuyerType(StringProperty buyerType) {
		this.buyerType = buyerType;
	}

	public StringProperty getBuyerActionProp() {
		return buyerAction;
	}

	public String getBuyerAction() {
		return buyerAction.get();
	}
	
	public void setBuyerAction(StringProperty buyerAction) {
		this.buyerAction = buyerAction;
	}

	public DoubleProperty getBudgetProgressProp() {
		return budgetProgress;
	}

	public Double getBudgetProgress() {
		return budgetProgress.get();
	}

	public void setBudgetProgress(DoubleProperty budgetProgress) {
		this.budgetProgress = budgetProgress;
	}


	public String getCreditPerioad() {
		return creditPerioad.get();
	}

	public StringProperty getCreditPerioadProp() {
		return creditPerioad;
	}

	
	public void setCreditPerioad(StringProperty creditPerioad) {
		this.creditPerioad = creditPerioad;
	}
	
	public StringProperty getCompanyProp() {
		return company;
	}

	public String getCompany() {
		return company.get();
	}
	
	public void setCompany(StringProperty company) {
		this.company = company;
	}
	
	public StringProperty getEmailProp() {
		return email;
	}

	public String getEmail() {
		return email.get();
	}

	public void setEmail(String email) {
		this.email.set(email);
	}

	public DoubleProperty getMilestoneProp() {
		return milestone;
	}

	public Double getMilestone() {
		return milestone.get();
	}

	public void setMilestone(Double milestone) {
		this.milestone.set(milestone);
	}
	

	public StringProperty getNameProp() {
		return name;
	}

	public String getName() {
		return name.get();
	}
	
	public void setName(StringProperty name) {
		this.name = name;
	}

}
