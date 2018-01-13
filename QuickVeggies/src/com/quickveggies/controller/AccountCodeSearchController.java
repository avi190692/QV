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
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/*
 * author : ss
 * creation date:13-Jan-2018
*/
public class AccountCodeSearchController implements Initializable{
	
	 @FXML
	 private TextField accountcode;
	 
	 @FXML
	 private TextField accountname;
	 
	 @FXML
	 private Button search;
	 
	 @FXML
	 private TableView<AccountMaster> detailstable;
	 
	 @FXML
	 private TableColumn<AccountMaster,String> code;
	 
	 @FXML
	 private TableColumn<AccountMaster,String> name;
	 
	 @FXML
	 private TableColumn<AccountMaster,Double> amount;
	 
	 ObservableList<AccountMaster> searchDetailsList;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources){
		
		
		search.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				detailstable.refresh();
				//## for refreshing the table view 
				detailstable.getItems().clear();
				String accCode = accountcode.getText();

				String accName = accountname.getText();
					
				
				List<AccountMaster> acm = DatabaseClient.getInstance().accountCodeSearch(accCode,accName);
				
				code.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("accountcode"));
				name.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("accountname"));
				amount.setCellValueFactory(new PropertyValueFactory<AccountMaster,Double>("amount"));
				//## adding dynamic values to observable list
				searchDetailsList = FXCollections.observableArrayList(acm); 
				detailstable.getItems().addAll(searchDetailsList);
			
				
			}
			
		});
		
	}
	
}
