package com.quickveggies.controller;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import com.quickveggies.Main;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.AccountMaster;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class AccountCodeCreationController implements Initializable {

	
	 @FXML
	 private Button createbtn;
	 @FXML
	 private Button editbtn;
	 
	 @FXML
	 private TextField accountcode;
	 
	 @FXML
	 private TextField accountname;
	 
	 @FXML
	 private TextField amt;
	 
	 ObservableList<String> accType = FXCollections.observableArrayList("Gl","SubGL");
	 @FXML
	 private ComboBox<String> accounttype;
	 
	 ObservableList<String> accountCategory = FXCollections.observableArrayList("Asset","Liabilities","Income","Expenditure");
	 @FXML
	 private ComboBox<String> accountcat;
	 
	
	 @Override
	 public void initialize(URL location, ResourceBundle resources) 
	 {
		 accounttype.setItems(accType);
		 accountcat.setItems(accountCategory);
		 
		 System.out.println("dashboard controller");
		 createbtn.setOnAction(new EventHandler<ActionEvent>() 
		 {
             public void handle(ActionEvent event) 
             {
            	 String accCode = accountcode.getText();
            	 String accName = accountname.getText();
            	 double amount = Double.parseDouble(amt.getText());
            	 String accType = accounttype.getValue();
            	 String reportFlag = accountcat.getValue();
            	 //## by default it will be true.
            	 boolean active_flag = true;
            	 
            	 //## generating financial year.
            	 Date currDt = Calendar.getInstance().getTime();
            	 DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            	 String currDt_withTimeZone = currDt.toString();
            	 String currDt_str = df.format(currDt);
            	 
            	 //System.out.println(currDt_str);
            	 String dt[]=currDt_str.split("/");
            	 String month = dt[1];
            	 System.out.println("month:::"+month);
            	 String currYr = dt[2];
            	 Integer nxtYr = Integer.parseInt(currYr)+1;
            	 String finYr = currYr+"-"+nxtYr.toString();
            	// System.out.println(finYr);
            	 
            	 //## defining rules for account codes
            	 String accCodeExistChk = "no";
            	 String chkLength = accountCodeLengthChk(accCode,accType);
            	 List<String> acCode =  DatabaseClient.getInstance().accountCodeSearch(accCode);
            	 System.out.println("size:::"+acCode.size());
            	 if(acCode.size()>0)
            	 {
            	 	accCodeExistChk="yes";
            	 }
            	 
            	 if(accCodeExistChk.equals("no") && chkLength.equals("yes"))
            	 {
            		 AccountMaster acm = new AccountMaster(accCode, accName, amount,finYr, currDt_withTimeZone, reportFlag, active_flag, month);
            		 DatabaseClient.getInstance().accountCodeEntry(acm);
            		 
            		 Alert alert = new Alert(Alert.AlertType.WARNING);
 		 	   		       alert.setTitle("Success!");
 		 	   		       alert.setHeaderText("Successfully Saved!");
 		 	   		       alert.setContentText("please check the log for details.");
 		 	   		       alert.showAndWait();
            	 }
            	 else
            	 {
            		 //System.out.println("here here");
            		 Alert alert = new Alert(Alert.AlertType.WARNING);
            		 
            		 if(accCode.length()!=7)
            		 {
		 	   		       alert.setTitle("Failure!");
		 	   		       alert.setHeaderText("Please check the account code length!");
		 	   		       alert.setContentText("Data not Saved	");
		 	   		       alert.showAndWait();
            		 }
            		else if(accounttype.getValue().equalsIgnoreCase("Gl") && !accCode.substring(5,7).equals("00"))
            		{
 		 	   		       alert.setTitle("Failure!");
 		 	   		       alert.setHeaderText("Check Account Code and Account Type");
 		 	   		       alert.setContentText("This is not a Gl code");
 		 	   		       alert.showAndWait();
            		}
            		else if(accounttype.getValue().equalsIgnoreCase("SubGL") && accCode.substring(5,7).equals("00"))
            		{
 		 	   		       alert.setTitle("Failure!");
 		 	   		       alert.setHeaderText("Check Account Code and Account Type");
 		 	   		       alert.setContentText("This is not a SubGL code");
 		 	   		       alert.showAndWait();
            		}
            		else if(accCodeExistChk.equals("yes"))
            		{
            			   alert.setTitle("Failure!");
		 	   		       alert.setHeaderText("Check Account Code");
		 	   		       alert.setContentText("Account Code already exists!");
		 	   		       alert.showAndWait();
            		}
            	 }
	
            	 
            	 
            	 String currentUserType = SessionDataController.getInstance().getCurrentUser().getUsertype();
             	 if(currentUserType.equals("Admin"))
             	 {
             		// new Main().replaceSceneContent("/fxml/accountcode_creation.fxml");
             		 new Main().newWindowSceneReplacementContecnt("/fxml/accountcode_creation.fxml");
             	 }
             	 else
             	 {
             		 Alert alert = new Alert(Alert.AlertType.WARNING);
             		 	   alert.setTitle("Restricted!");
             		 	   alert.setHeaderText("Access Restricted!");
             		 	   alert.setContentText("login as admin user.");
             		 	   alert.showAndWait();
             	 }
             }
         });
		
	 }
	 
	 //## checking account code length and gl-subgl check
	 public String accountCodeLengthChk(String accountCode,String accountType)
	 {
		 String returnResult =null;
		 //System.out.println("gl chk:::"+accountCode.substring(5,7));
		 int chkLength = accountCode.length();
		 if(chkLength==7)
		 {
			 if(accountType.equalsIgnoreCase("Gl") && accountCode.substring(5,7).equals("00"))
			 {
				 returnResult="yes";
			 }
			 else if(accountType.equalsIgnoreCase("SubGL") && !accountCode.substring(5,7).equals("00"))
			 {
				 returnResult="yes";
			 }
			 else
			 {
				 returnResult="no";
			 }
			 
		 }
		 else
		 {
			 returnResult="no";
		 }
		 return returnResult;
	 }

}
