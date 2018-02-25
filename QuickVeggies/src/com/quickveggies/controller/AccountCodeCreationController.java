package com.quickveggies.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.AccountMaster;
import com.quickveggies.entities.AuditLog;
import com.quickveggies.misc.CommonFunctions;
import com.quickveggies.misc.MyContext;

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
	 private Button savebtn;
	 
	 /*@FXML
	 private Button importbtn;*/
	 
	 @FXML
	 private TextField accountcode;
	 
	 @FXML
	 private TextField subGlLink;
	 
	 @FXML
	 private TextField accountname;
	 
	 @FXML
	 private TextField amt;
	 
	 @FXML
	 private TextField financialyear;
	 
	 private final Runnable onCreationFinished;
	 
	 ObservableList<String> accTypeList = FXCollections.observableArrayList("Gl","SubGl");
	 @FXML
	 private ComboBox<String> accounttype;
	 
	 ObservableList<String> accountCategoryList = FXCollections.observableArrayList("Accounts Payable (A/R)","Accounts Recievable (A/R)","Bank","Cost of Goods Sold","Credit Card",
			                                                                    "Equity","Expenses","Fixed Assets","Income","Long Term Liabilities","Other Assets",
			                                                                    "Other Current Assets","Other Current Liabilities","Other Expenses","Other Income");
	 @FXML
	 private ComboBox<String> accountcat;
	 
	 ObservableList<String> accountActiveStatus = FXCollections.observableArrayList("Active","Inactive");
	 @FXML
	 private ComboBox<String> activestatus;
	 

	 
	 public AccountCodeCreationController(Runnable onCreationFinished) {
		super();
		this.onCreationFinished = onCreationFinished;
		System.out.println("Inside AccountCodeCreationController.  We need to put runnable controller here");
	}



	@Override
	 public void initialize(URL location, ResourceBundle resources) 
	 {
		 String btnAction = MyContext.getInstance().getButtonFlagIndicator().getButtonType();
	   	 activestatus.setValue("Active"); 
		 accounttype.setItems(accTypeList);
		 accountcat.setItems(accountCategoryList);
		 activestatus.setItems(accountActiveStatus);
		 
		 
		 accountcode.setText(MyContext.getInstance().getAccountMaster().getAccountcode());
		 accountname.setText(MyContext.getInstance().getAccountMaster().getAccountname());
		 
		 Double amount = MyContext.getInstance().getAccountMaster().getAmount();		 
		 amt.setText(amount.toString());
		 
		 Boolean statusFlag = MyContext.getInstance().getAccountMaster().isActive_flag();
		 if(statusFlag.toString().equals("true"))
		 {
			 activestatus.setValue("Active"); 
		 }
		 if(statusFlag.toString().equals("false"))
		 {
			 activestatus.setValue("InActive"); 
		 }
		 if(statusFlag.toString().equals(null))
		 {
			 activestatus.setValue("Active"); 
		 }
		 accountcat.setValue(MyContext.getInstance().getAccountMaster().getReport_flag());
		 accounttype.setValue(MyContext.getInstance().getAccountMaster().getAccountType());
		 
		 //System.out.println("show me this value:::"+MyContext.getInstance().getAccountMaster().getAccountType());
		 financialyear.setText(MyContext.getInstance().getAccountMaster().getFin_year());
		 
		 if(MyContext.getInstance().getAccountMaster().getAccountType() == null)
		 {
			 accounttype.setValue("Gl");
		 }
		 
		 if(MyContext.getInstance().getAccountMaster().getFin_year()==null)
		 {
			 financialyear.setText(new CommonFunctions().financialYear());
		 }
		 
		 if(btnAction.equals("create"))
		 {
			 accountcode.setText("");
    	   	 accountname.setText("");
    	   	 amt.setText("");
    	   	 subGlLink.setText("");
    	   	 accounttype.setValue("Gl");
    	   	 accountcat.setItems(accountCategoryList);
    	   	 activestatus.setValue("Active"); 
    		 
    	   	
		 }
		 
		 //System.out.println("dashboard controller");
		 savebtn.setOnAction(new EventHandler<ActionEvent>() 
		 {
             public void handle(ActionEvent event) 
             {
            	 String currentUserType = SessionDataController.getInstance().getCurrentUser().getUsertype();
            	 String accCode = accountcode.getText();
            	 String accName = accountname.getText();
            	 String accType = accounttype.getValue();
            	 String reportFlag = accountcat.getValue();
            	 //## by default it will be true.
            	 boolean active_flag = true;
            	 if(!amt.getText().equals(""))
            	 {
            		 double amount = Double.parseDouble(amt.getText());
            	 }
            	 else
            	 {
            		 Alert alert = new Alert(Alert.AlertType.WARNING);
            		 alert.setTitle("Warning!");
 		 	   		 alert.setHeaderText("Amount can't be empty.");
 		 	   		 alert.setContentText("please provide an amount and proceed...");
 		 	   		 alert.showAndWait();
            	 }
            	 
            	 
            	 Alert alert = new Alert(Alert.AlertType.WARNING);
            	 if(btnAction.equals("create"))
            	 {
            		 
            		 
                	 //## defining rules for account codes
                	 String accCodeExistChk = "no";
                	 List<String> acCode =  DatabaseClient.getInstance().accountCodeSearch(accCode);
                	 System.out.println("size:::"+acCode.size());
                	 if(acCode.size()>0)
                	 {
                	 	accCodeExistChk="yes";
                	 }
                	 AuditLog auditlog = new AuditLog();
                	 if(accCodeExistChk.equals("no"))
                	 {
		                	 AccountMaster acm = new AccountMaster(accCode, accName, amount,new CommonFunctions().financialYear(), new CommonFunctions().currentTime_withtimeZone(),reportFlag, active_flag, new CommonFunctions().currentMonth(),accType);
		            		 DatabaseClient.getInstance().accountCodeEntry(acm);
		            		 
		            		 auditlog.setUserId(currentUserType);
		            		 auditlog.setEventDetail("Account Code added :"+accCode);
		            		 auditlog.setEventObject(accType);
		            		 auditlog.setEventObjectId(0);
		            		 auditlog.setAmount(amount);
		            		           		 
		            		 DatabaseClient.getInstance().insertLog_Audit(auditlog);
		            		 
		            		 
		            		
		 		 	   		 alert.setTitle("Success!");
		 		 	   		 alert.setHeaderText("Successfully Saved!");
		 		 	   		 alert.setContentText("please check the log for details.");
		 		 	   		 alert.showAndWait();
		 		 	   		 
		 		 	   		 
		 		 	   		 //## clearing and changing all field to default.
		 		 	   		 accountcode.setText("");
		 		 	   	     accountname.setText("");
		 		 	   	     amt.setText("");
		 		 	   	     subGlLink.setText("");
		 		 	   	     accounttype.setValue("");
		 		 	   	     accountcat.setValue("");
		 		 	   	     activestatus.setValue("Active"); 
		 		 	   		 onCreationFinished.run();
                	 }
                	 else
                	 {
                		   	 alert.setTitle("Failure!");
                		   	 alert.setHeaderText("Check Account Code");
                		   	 alert.setContentText("Account Code already exists!");
                		   	 alert.showAndWait();
                	 }
            	 }
            	 if(btnAction.equals("edit"))
            	 {
            		 
            		 boolean activeStatusFlag = true;
            		 if(activestatus.getValue().equals("Inactive"))
            		 {
            			 activeStatusFlag = false;
            		 }
            		 
            		 
            		 AccountMaster accountMaster = new AccountMaster(accCode, accName,Double.parseDouble(amt.getText()),reportFlag,activeStatusFlag,accType,subGlLink.getText());
        			 DatabaseClient.getInstance().accountInfoUpdate(accountMaster);
        			 
        			 AuditLog auditlog = new AuditLog();
        			 
        			 auditlog.setUserId(currentUserType);
            		 auditlog.setEventDetail("Account Code edited :"+accCode);
            		 auditlog.setEventObject(accType);
            		 auditlog.setEventObjectId(0);
            		 auditlog.setAmount(amount);
        			 
        			 alert.setTitle("Success!");
 		 	   		 alert.setHeaderText("Successfully Saved!");
 		 	   		 alert.setContentText("please check the log for details.");
 		 	   		 alert.showAndWait();
 		 	   		 
 		 	   		 
 		 	   		 //## clearing and changing all field to default.
 		 	   		 accountcode.setText("");
 		 	   	     accountname.setText("");
 		 	   	     amt.setText("");
 		 	   	     subGlLink.setText("");
 		 	   	     accounttype.setValue("");
 		 	   	     accountcat.setValue("");
 		 	   	     activestatus.setValue("Active"); 
 		 	   	     onCreationFinished.run();
 		 	   	     
            	 }
             }
         });
		 
		 
		 
	 }
}
