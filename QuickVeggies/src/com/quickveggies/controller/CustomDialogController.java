package com.quickveggies.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

public class CustomDialogController implements Initializable{
	@FXML
	private Pane dialog_pane;
	@FXML
	private Label dialogText;
	
    private int choice;
    
    private String displayText=null;
    private Button[] buttons;
    private TextField inputField;
    
    public CustomDialogController(Button[] buttons,String displayText){
           this.displayText=displayText;
           this.buttons=buttons;
    }
    
    public CustomDialogController(Button[] buttons,TextField inputField,String displayText){
        this.displayText=displayText;
        this.buttons=buttons;
        this.inputField=inputField;
 }
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
    	dialogText.setText(displayText);
    	for(Button button : buttons)dialog_pane.getChildren().add(button);
    	//System.out.println("Custom dialog, children :" + dialog_pane.getChildren());
    	if (inputField !=null ) 
    		dialog_pane.getChildren().add(inputField);
	}

}
