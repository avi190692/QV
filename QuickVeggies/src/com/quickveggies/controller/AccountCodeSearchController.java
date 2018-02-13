package com.quickveggies.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.quickveggies.Main;
import com.quickveggies.controller.dashboard.DBuyerController;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.AccountMaster;
import com.quickveggies.misc.MyContext;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/*
 * author : ss
 * creation date:13-Jan-2018
*/
public class AccountCodeSearchController implements Initializable{
	
	 @FXML
	 private TextField searchstring;
	 
	 @FXML
	 private TextField accountcodeedit;
	  
	 @FXML
	 private Button searchbutton;
	 
	 @FXML
	 private Button importbtn;
	 
	 @FXML
	 private Button edit;
	 
	 @FXML
	 private Button importcode;
	 
	 @FXML
	 private Button createnew;
	 
	 @FXML
	 private ComboBox<String> searchtype;
	 
	 ObservableList<String> searchcriteria = FXCollections.observableArrayList("Code","Name","Type");//,"Balance");
	   
	 @FXML
	 private TableView<AccountMaster> detailstable;
	 
	 @FXML
	 private TableColumn<AccountMaster,String> code;
	 
	 @FXML
	 private TableColumn<AccountMaster,String> name;
	 
	 @FXML
	 private TableColumn<AccountMaster,String> type;
	 
	 @FXML
	 private TableColumn<AccountMaster,Double> balance;
	 
	 ObservableList<AccountMaster> searchDetailsList;
	 
	 
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources){
		
		 searchtype.setItems(searchcriteria);
		 searchtype.setValue("Code");
		
		searchbutton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				detailstable.refresh();
				//## for refreshing the table view 
				detailstable.getItems().clear();
				String serachString = searchstring.getText();

				String searchChoice = searchtype.getValue();;
				
			
				
				List<AccountMaster> acm = DatabaseClient.getInstance().accountCodeSearch(serachString,searchChoice);
				
				code.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("accountcode"));
				name.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("accountname"));
				type.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("report_flag"));
				balance.setCellValueFactory(new PropertyValueFactory<AccountMaster,Double>("amount"));
				//## adding dynamic values to observable list
				searchDetailsList = FXCollections.observableArrayList(acm); 
				detailstable.getItems().addAll(searchDetailsList);
			
				
			}
			
		});
		
		//## for creation of new account code.
		createnew.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				MyContext.getInstance().getButtonFlagIndicator().setButtonType("create");
				
				final Stage accoutCodeCreation = new Stage();
				accoutCodeCreation.centerOnScreen();
				accoutCodeCreation.setTitle("Account Code Creation");
				accoutCodeCreation.initModality(Modality.APPLICATION_MODAL);
				accoutCodeCreation.setOnCloseRequest(new EventHandler<WindowEvent>() 
		        {
		            public void handle(WindowEvent event) 
		            {
		                Main.getStage().getScene().getRoot().setEffect(null);
		            }
		        });
				
				Parent parent;
				try {
					parent = FXMLLoader.load(DBuyerController.class.getResource("/fxml/accountCodeCreation.fxml"));
				
	            Scene scene = new Scene(parent, 687, 400);
	            scene.setOnKeyPressed(new EventHandler<KeyEvent>() 
	            {
	                public void handle(KeyEvent event)
	                {                	
	                    if (event.getCode() == KeyCode.ESCAPE) 
	                    {
	                        Main.getStage().getScene().getRoot().setEffect(null);
	                        accoutCodeCreation.close();
	                    }
	                }
	            });
	            accoutCodeCreation.setScene(scene);
	            accoutCodeCreation.show();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		//## for editing the details of account code.
		edit.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
				MyContext.getInstance().getButtonFlagIndicator().setButtonType("edit");
				
				String accCode = accountcodeedit.getText();
				List<AccountMaster> acm = DatabaseClient.getInstance().accountCodeSearch(accCode,"accountCodeEdit");
				
				//## setting context for forwarding request to another controller
				MyContext.getInstance().getAccountMaster().setAccountcode(acm.get(0).getAccountcode());
				MyContext.getInstance().getAccountMaster().setAccountname(acm.get(0).getAccountname());
				MyContext.getInstance().getAccountMaster().setAmount(acm.get(0).getAmount());
				MyContext.getInstance().getAccountMaster().setReport_flag(acm.get(0).getReport_flag());
				Boolean activeFlagStatus = true;
				if(acm.get(0).getReport_flag().equals("false"))
				{
					activeFlagStatus=false;
				}
				MyContext.getInstance().getAccountMaster().setActive_flag(activeFlagStatus);
				MyContext.getInstance().getAccountMaster().setFin_year(acm.get(0).getFin_year());
				MyContext.getInstance().getAccountMaster().setAccountType(acm.get(0).getAccountType());
				
				final Stage accoutCodeCreation = new Stage();
				accoutCodeCreation.centerOnScreen();
				accoutCodeCreation.setTitle("Account Code Creation");
				accoutCodeCreation.initModality(Modality.APPLICATION_MODAL);
				accoutCodeCreation.setOnCloseRequest(new EventHandler<WindowEvent>() 
		        {
		            public void handle(WindowEvent event) 
		            {
		                Main.getStage().getScene().getRoot().setEffect(null);
		            }
		        });
				
				Parent parent;
				try {
					parent = FXMLLoader.load(DBuyerController.class.getResource("/fxml/accountCodeCreation.fxml"));
				
	            Scene scene = new Scene(parent, 687, 400);
	            scene.setOnKeyPressed(new EventHandler<KeyEvent>() 
	            {
	                public void handle(KeyEvent event)
	                {                	
	                    if (event.getCode() == KeyCode.ESCAPE) 
	                    {
	                        Main.getStage().getScene().getRoot().setEffect(null);
	                        accoutCodeCreation.close();
	                    }
	                }
	            });
	            accoutCodeCreation.setScene(scene);
	            accoutCodeCreation.show();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			
			
		});
		
		importbtn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
				final Stage accoutCodeImport = new Stage();
				accoutCodeImport.centerOnScreen();
				accoutCodeImport.setTitle("Account Code Creation");
				accoutCodeImport.initModality(Modality.APPLICATION_MODAL);
				accoutCodeImport.setOnCloseRequest(new EventHandler<WindowEvent>() 
		        {
		            public void handle(WindowEvent event) 
		            {
		                Main.getStage().getScene().getRoot().setEffect(null);
		            }
		        });
				
				Parent parent;
				try {
					parent = FXMLLoader.load(DBuyerController.class.getResource("/fxml/accountCodeImport.fxml"));
				
	            Scene scene = new Scene(parent, 687, 400);
	            scene.setOnKeyPressed(new EventHandler<KeyEvent>() 
	            {
	                public void handle(KeyEvent event)
	                {                	
	                    if (event.getCode() == KeyCode.ESCAPE) 
	                    {
	                        Main.getStage().getScene().getRoot().setEffect(null);
	                        accoutCodeImport.close();
	                    }
	                }
	            });
	            accoutCodeImport.setScene(scene);
	            accoutCodeImport.show();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
				
			
		});
		
	}
	
	
	
}
