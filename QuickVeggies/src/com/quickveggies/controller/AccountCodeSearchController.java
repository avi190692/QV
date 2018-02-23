package com.quickveggies.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.quickveggies.Main;
import com.quickveggies.controller.dashboard.DBuyerController;
import com.quickveggies.controller.dashboard.DashboardController;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

/*
 * author : ss
 * creation date:13-Jan-2018
*/
public class AccountCodeSearchController implements Initializable{
	
	 @FXML
	 private TextField searchstring;

	  
	 @FXML
	 private Button searchbutton;
	 
	 @FXML
	 private Button importbtn;
	 
	 
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
	 
	 @FXML
	 private TableColumn<AccountMaster, Void> action; // = new TableColumn("Action");
	 
	 ObservableList<AccountMaster> searchDetailsList;
	 
	 public AccountCodeCreationController controller;
	 
	 public AccountCodeSearchController () {
		 tableRefresher ();
	 }
	 public void tableRefresher () {
			controller = new AccountCodeCreationController(() -> {
				
				
				detailstable.refresh();
				//## for refreshing the table view 
				detailstable.getItems().clear();
			
                List<AccountMaster> acm = DatabaseClient.getInstance().accountCodeSearch();
				
				code.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("accountcode"));
				name.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("accountname"));
				type.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("report_flag"));
				balance.setCellValueFactory(new PropertyValueFactory<AccountMaster,Double>("amount"));
				
				Callback<TableColumn<AccountMaster, Void>, TableCell<AccountMaster, Void>> cellFactory = new Callback<TableColumn<AccountMaster, Void>, TableCell<AccountMaster, Void>>() 
				{
		            @Override
		            public TableCell<AccountMaster, Void> call(final TableColumn<AccountMaster, Void> param) {
		                final TableCell<AccountMaster, Void> cell = new TableCell<AccountMaster, Void>() {
		                	
		                    private final Button btn = new Button("Edit Details");

		                    {
		                        btn.setOnAction((ActionEvent event) -> 
		                        {
		                        	AccountMaster data = getTableView().getItems().get(getIndex());
		                            System.out.println("selectedData: " + data.getAccountcode());
		                            
		                            /* ## for editing the details of account code. */
		                            
		                            MyContext.getInstance().getButtonFlagIndicator().setButtonType("edit");
		                            List<AccountMaster> acm = DatabaseClient.getInstance().accountCodeSearch(data.getAccountcode(),"accountCodeEdit");
		            				
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
		            				
		            				DashboardController.showPopup("/fxml/accountCodeCreation.fxml",
		            						"Account Code Creation", controller);
		            				tableRefresher ();
		                        });
		                    }

		                    @Override
		                    public void updateItem(Void item, boolean empty) {
		                        super.updateItem(item, empty);
		                        if (empty) 
		                        {
		                            setGraphic(null);
		                        } 
		                        else 
		                        {
		                            setGraphic(btn);
		                        }
		                    }
		                };
		                return cell;
		            }
		        };

		        
		        //## adding the action button on the table view.
		        action.setCellFactory(cellFactory);
				//## adding dynamic values to observable list
				searchDetailsList = FXCollections.observableArrayList(acm); 
				detailstable.getItems().addAll(searchDetailsList);

            });

	 }
	
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
				String searchChoice = searchtype.getValue();
				
			
				
				List<AccountMaster> acm = DatabaseClient.getInstance().accountCodeSearch(serachString,searchChoice);
				
				code.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("accountcode"));
				name.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("accountname"));
				type.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("report_flag"));
				balance.setCellValueFactory(new PropertyValueFactory<AccountMaster,Double>("amount"));
				
				Callback<TableColumn<AccountMaster, Void>, TableCell<AccountMaster, Void>> cellFactory = new Callback<TableColumn<AccountMaster, Void>, TableCell<AccountMaster, Void>>() 
				{
		            @Override
		            public TableCell<AccountMaster, Void> call(final TableColumn<AccountMaster, Void> param) {
		                final TableCell<AccountMaster, Void> cell = new TableCell<AccountMaster, Void>() {
		                	
		                    private final Button btn = new Button("Edit Details");

		                    {
		                        btn.setOnAction((ActionEvent event) -> 
		                        {
		                        	AccountMaster data = getTableView().getItems().get(getIndex());
		                            System.out.println("selectedData: " + data.getAccountcode());
		                            
		                            /* ## for editing the details of account code. */
		                            
		                            MyContext.getInstance().getButtonFlagIndicator().setButtonType("edit");
		                            List<AccountMaster> acm = DatabaseClient.getInstance().accountCodeSearch(data.getAccountcode(),"accountCodeEdit");
		            				
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
		            				
		            				DashboardController.showPopup("/fxml/accountCodeCreation.fxml",
		            						"Account Code Creation", controller);
		            				
		            				tableRefresher ();
		                        });
		                    }

		                    @Override
		                    public void updateItem(Void item, boolean empty) {
		                        super.updateItem(item, empty);
		                        if (empty) 
		                        {
		                            setGraphic(null);
		                        } 
		                        else 
		                        {
		                            setGraphic(btn);
		                        }
		                    }
		                };
		                return cell;
		            }
		        };

		        
		        //## adding the action button on the table view.
		        action.setCellFactory(cellFactory);
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
				DashboardController.showPopup("/fxml/accountCodeCreation.fxml",
						"Account Code Creation", controller);
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
				
	            Scene scene = new Scene(parent,Main.DASHBOARD_WIDTH,Main.DASHBOARD_HEIGHT);
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
