package com.quickveggies.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.quickveggies.misc.FruitButtonEventHandler;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class SettingsController implements Initializable {
	
	@FXML
	private Button btnAddFruit;
	
	@FXML
	private Pane settingsPane;
	
	@FXML
	private ScrollPane paneProducts; 
	
	@FXML
	private AnchorPane ancPaneProducts;
	
	@FXML
	private Pane fruitSettingsPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	try {
    		VBox content = new VBox();
    		content.setPrefHeight(paneProducts.getHeight());
    		content.setPrefWidth(paneProducts.getWidth());
    		content.getChildren().add((Node) FXMLLoader.load(getClass().getResource("/fxml/fruitviewer.fxml")));
    		//## blocked by ss(some error in .fxml)
    		//content.getChildren().add((Node) FXMLLoader.load(getClass().getResource("/fxml/growerexpensesviewer.fxml")));
    		content.getChildren().add((Node) FXMLLoader.load(getClass().getResource("/fxml/buyerexpensesviewer.fxml")));
    		content.getChildren().add((Node) FXMLLoader.load(getClass().getResource("/fxml/companyviewer.fxml")));
    		content.getChildren().add((Node) FXMLLoader.load(getClass().getResource("/fxml/expendituretypesviewer.fxml")));
    		content.getChildren().add((Node) FXMLLoader.load(getClass().getResource("/fxml/smsTemplate.fxml")));
    		
    		paneProducts.setContent(content);
		} 
    	catch (IOException e1) 
    	{
			e1.printStackTrace();
		}
    	SessionDataController.getInstance().setSettingPagePane(fruitSettingsPane);
    	//System.out.println(fruitSettingsPane.getChildren().indexOf(paneProducts));
    	btnAddFruit.setOnAction(new FruitButtonEventHandler("/fxml/fruitadd.fxml", "New Fruit Entry", fruitSettingsPane));

    }
}
