package com.quickveggies.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServlet;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class AccountCodeImport implements Initializable{

	
	 @FXML
	 private Button downloadTemplate;
	 
	 @FXML
	 private Button browse;
	 
	 @FXML
	 private Button uploadTemplate;
	 
	 @FXML
	 private TextField selectedTemplate;
	 
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		downloadTemplate.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) 
			{
				
				
			}
		});
	
		
	}

}
