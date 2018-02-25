package com.quickveggies.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AccountListApproval implements Initializable {

	@FXML
	private Button approvelist;
	
	 @FXML
	 private ComboBox<String> accountlisttype;
	 
	 ObservableList<String> accountList = FXCollections.observableArrayList("Code","Name","Type","Balance");
	 
	 @FXML
	 private TableView<AccountMaster> detailstable;
	 
	 @FXML
	 private TableColumn<AccountMaster,String> accountcode;
	 
	 @FXML
	 private TableColumn<AccountMaster,String> accountname;
	 
	 @FXML
	 private TableColumn<AccountMaster,Double> amount;
	 
	 @FXML
	 private TableColumn<AccountMaster,String> accountflag;
	 
	 @FXML
	 private TableColumn<AccountMaster,String> accountlink;
	 
	 ObservableList<AccountMaster> searchDetailsList;
	 
	 
	 
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		
		List<AccountMaster> acm = DatabaseClient.getInstance().accountCodeApprovalList();
		
		System.out.println();
		
		accountcode.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("accountcode"));
		accountname.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("accountname"));
		amount.setCellValueFactory(new PropertyValueFactory<AccountMaster,Double>("amount"));
		accountflag.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("report_flag"));
		accountlink.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("subglLink"));
		
		//## adding dynamic values to observable list
		searchDetailsList = FXCollections.observableArrayList(acm); 
		detailstable.getItems().addAll(searchDetailsList);
		
		approvelist.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				if(acm.size()>0)
				{
					DatabaseClient.getInstance().approveUploadedList("all");
					Alert alert = new Alert(Alert.AlertType.WARNING);
   		          	      alert.setTitle("Success!");
   		          	      alert.setHeaderText("Approved");
   		          	      alert.setContentText("uploaded list is approved...");
   		          	      alert.showAndWait();
   		          	      
   		          	detailstable.refresh();
   					//## for refreshing the table view 
   					detailstable.getItems().clear();
				}
				else
				{
					Alert alert = new Alert(Alert.AlertType.WARNING);
		          	      alert.setTitle("Warning!");
		          	      alert.setHeaderText("Failure");
		          	      alert.setContentText("Nothing to approve.Please upload list first...");
		          	      alert.showAndWait();
				}
			}
		});
		
	}

}
