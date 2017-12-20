package com.quickveggies.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;


public class PreviewAccTableController implements Initializable {
   
	@FXML
	private Button close;
	@FXML
	private Pane prevTablePane;
	
	private TableView previewTable;
	
	public PreviewAccTableController(TableView previewTable){
	     this.previewTable=previewTable;
	     previewTable.setPrefHeight(250.0);
	     previewTable.setPrefWidth(600);
	     previewTable.setEditable(false);
		}
  
    @Override
    public void initialize(URL location, ResourceBundle resources) {
      prevTablePane.getChildren().add(previewTable);
    	
      close.setOnAction(new EventHandler<ActionEvent>() {
          public void handle(ActionEvent event) {
        	  close.getScene().getWindow().hide(); 
          	}
      });
    }


}
