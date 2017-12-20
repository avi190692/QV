package com.quickveggies.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.quickveggies.GeneralMethods;
import com.quickveggies.dao.DatabaseClient;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class BuyerExpenseInfoAddController implements Initializable{

	@FXML
	private Button btnSave;
	
	@FXML
	private TextField txtName;
	
	@FXML
	private ComboBox<String> cboChargeTypes;
	
	@FXML
	private TextField txtAmount;
	
    private String[] chargeTypes = new String[] {"%","@"};

	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cboChargeTypes.setItems(FXCollections.observableArrayList(chargeTypes));
		cboChargeTypes.setValue("%");

		btnSave.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String name = txtName.getText();
				if (name == null || name.trim().isEmpty()) {
					GeneralMethods.errorMsg("Expense name cannot be null");
				}
				String type  = cboChargeTypes.getValue();
				String defAmount = txtAmount.getText();
				if (defAmount == null || defAmount.trim().isEmpty()) {
					defAmount = null;
				}
				DatabaseClient.getInstance().addBuyerExpenseInfo(name, type, defAmount);
				btnSave.getScene().getWindow().hide();
			}
		});
	}

}
