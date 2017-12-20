package com.quickveggies.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.quickveggies.UserGlobalParameters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class SMSTemplateController implements Initializable{
	
	@FXML
	private Button btnEditTemplate;
	
	@FXML
	private Button btnSaveTemplate;

	@FXML
	private TextArea taSMSTemplate;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		taSMSTemplate.setEditable(false);
		taSMSTemplate.setText(UserGlobalParameters.GET_SMS_TEMPLATE());
		taSMSTemplate.setWrapText(true);
		btnEditTemplate.setOnAction((event) -> {
			taSMSTemplate.setEditable(true);
			btnEditTemplate.setDisable(true);
			btnSaveTemplate.setDisable(false);
		});
		btnSaveTemplate.setOnAction((event) -> {
			btnEditTemplate.setDisable(false);
			UserGlobalParameters.SET_SMS_TEMPLATE(taSMSTemplate.getText(), true);
			btnSaveTemplate.setDisable(true);
			taSMSTemplate.setEditable(false);
		});
	}



}
