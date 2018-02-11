package com.quickveggies.controller;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.quickveggies.Main;
import com.quickveggies.controller.dashboard.DBuyerController;
import com.quickveggies.dao.DatabaseClient;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class AccountCodeImport implements Initializable{

	
	 @FXML
	 private Button downloadTemplate;
	 
	 @FXML
	 private Button browse;
	 
	 @FXML
	 private Button uploadTemplate;
	 
	 @FXML
	 private Button approveList;
	 
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
	
		
	    browse.setOnAction(new EventHandler<ActionEvent>(){

		   @Override
		   public void handle(ActionEvent event) 
		   {
			
			   FileChooser chooser = new FileChooser();
			   File selectedFile = chooser.showOpenDialog(null);
			   if(selectedFile !=null)
			   {
				   selectedTemplate.setText(selectedFile.getAbsolutePath());
			   }
			   else   
			   {
				   System.out.println("no files are selected...");
			   }
			
		   }
	   });
	   
	    uploadTemplate.setOnAction(new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent event) {
				
				 System.out.println("selected File :::"+selectedTemplate.getText());
				 if(!selectedTemplate.getText().equalsIgnoreCase(""))
				 {
					  //String currentWorkingDir = System.getProperty("user.dir");
				      //System.out.println("Current working directory in Java : " + currentWorkingDir);
				      //System.out.println(currentWorkingDir+"\\UploadFiles")
				    		   //## code for saving the directory.
					  		  DatabaseClient.getInstance().dataClean();
				    		   try 
				    		   {
								FileInputStream  file = new FileInputStream(new File(selectedTemplate.getText()));
								XSSFWorkbook workbook = new XSSFWorkbook(file);//OPENING EXCEL WORKBOOK BY CREATING AN INSTANCE
								XSSFSheet sheet = workbook.getSheetAt(0);//OPENING THE PARTICULAR SHEET
								                
							    DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
							    Cell 	accountcode =null,
							    		accountname=null,
							    		amount=null,
							    	    report_flag=null,	
							    	    account_type=null,		
							    	    subgl_link=null;
							    Row row;
							    for(int i=1; i<=sheet.getLastRowNum();i++)
					        	{
							    	accountcode = sheet.getRow(i).getCell(0);
							    	accountname = sheet.getRow(i).getCell(1);
							    	amount = sheet.getRow(i).getCell(2);
							    	report_flag = sheet.getRow(i).getCell(3);
							    	account_type = sheet.getRow(i).getCell(4);
							    	subgl_link = sheet.getRow(i).getCell(5);
							    	
							    	String account_code = formatter.formatCellValue(accountcode);
			        		        String account_name = formatter.formatCellValue(accountname);
			        		        String amt = formatter.formatCellValue(amount);
			        		        String reportFlag = formatter.formatCellValue(report_flag);
			        		        String accountType = formatter.formatCellValue(account_type);
			        		        String subglLink = formatter.formatCellValue(subgl_link);
			        		        
			        		        //System.out.println(account_code+"--"+account_name+"--"+amt+"--"+reportFlag+"--"+accountType+"--"+subglLink);
			        		        
			        		        //## call the method for saving the details.
			        		        DatabaseClient.getInstance().accountCodeUpoadDraftMode(account_code, account_name,amt,reportFlag,accountType,subglLink);
					        	}
								     
							   }
				    		   catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				    	  }
				 		else
				 		{
				 				Alert alert = new Alert(Alert.AlertType.WARNING);
	 		 	   		          	  alert.setTitle("Failure!");
	 		 	   		          	  alert.setHeaderText("File Not Selected");
	 		 	   		          	  alert.setContentText("please select a file...");
	 		 	   		          	  alert.showAndWait();
					 
				        }
			
				 }

		});
	    
	    
	    approveList.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
				final Stage accoutCodeApproval = new Stage();
				accoutCodeApproval.centerOnScreen();
				accoutCodeApproval.setTitle("Account Code Approval");
				accoutCodeApproval.initModality(Modality.APPLICATION_MODAL);
				accoutCodeApproval.setOnCloseRequest(new EventHandler<WindowEvent>() 
		        {
		            public void handle(WindowEvent event) 
		            {
		                Main.getStage().getScene().getRoot().setEffect(null);
		            }
		        });
				
				Parent parent;
				try {
					parent = FXMLLoader.load(DBuyerController.class.getResource("/fxml/accountlistapproval.fxml"));
				
	            Scene scene = new Scene(parent, 687, 400);
	            scene.setOnKeyPressed(new EventHandler<KeyEvent>() 
	            {
	                public void handle(KeyEvent event)
	                {                	
	                    if (event.getCode() == KeyCode.ESCAPE) 
	                    {
	                        Main.getStage().getScene().getRoot().setEffect(null);
	                        accoutCodeApproval.close();
	                    }
	                }
	            });
	            accoutCodeApproval.setScene(scene);
	            accoutCodeApproval.show();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
				
			
		});
	    
		
	}

}
