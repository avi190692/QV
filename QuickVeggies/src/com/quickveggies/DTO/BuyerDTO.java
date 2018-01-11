package com.quickveggies.DTO;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BuyerDTO {
	private StringProperty email;
	private DoubleProperty milestone;
	
	public BuyerDTO(String email,Double milestone) {
		this.email = new SimpleStringProperty(email);
		this.milestone = new SimpleDoubleProperty(milestone);
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
}
