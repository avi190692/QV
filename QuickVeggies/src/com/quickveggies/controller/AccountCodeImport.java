package com.quickveggies.controller;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

public class AccountCodeImport implements Initializable{

	 
	 @FXML
	 private Button browse;
	 
	 @FXML
	 private Button importlist;
	 
	 @FXML
	 private Button approveList;
	 
	 @FXML
	 private Button addall;
	 
	 @FXML
	 private Button cancel;
	 
	 @FXML
	 private TextField selectedTemplate;
	 
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
	 
	 @FXML
	 private TableColumn<AccountMaster, Void> action;
	 
	 ObservableList<AccountMaster> searchDetailsList;
	 
	 public AccountCodeImport controller;
	 
	 /*private final Runnable onCreationFinished;
	 public AccountCodeImport(Runnable onCreationFinished) {
			super();
			this.onCreationFinished = onCreationFinished;
			System.out.println("Inside AccountCodeCreationController.  We need to put runnable controller here");
		}*/
	 
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		List<AccountMaster> acm = DatabaseClient.getInstance().accountCodeApprovalList();
		System.out.println(acm.size());
			
		accountcode.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("accountcode"));
		accountname.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("accountname"));
		amount.setCellValueFactory(new PropertyValueFactory<AccountMaster,Double>("amount"));
		accountflag.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("report_flag"));
		accountlink.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("subglLink"));
		Callback<TableColumn<AccountMaster, Void>, TableCell<AccountMaster, Void>> cellFactory = new Callback<TableColumn<AccountMaster, Void>, TableCell<AccountMaster, Void>>() 
		{
            @Override
            public TableCell<AccountMaster, Void> call(final TableColumn<AccountMaster, Void> param) {
                final TableCell<AccountMaster, Void> cell = new TableCell<AccountMaster, Void>() {
                	
                    private final Button btn = new Button("Add");

                    {
                        btn.setOnAction((ActionEvent event) -> 
                        {
                        	AccountMaster data = getTableView().getItems().get(getIndex());
                            System.out.println("selectedData: " + data.getAccountcode());		                           
                            
                             //## approving selected account details. 		                           
                             DatabaseClient.getInstance().approveUploadedList(data.getAccountcode());
            							
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) 
                    {
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
		//onCreationFinished.run();
		
		
	
		
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
	   
	    importlist.setOnAction(new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent event) {
				
				 System.out.println("selected File :::"+selectedTemplate.getText());
				 if(!selectedTemplate.getText().equalsIgnoreCase(""))
				 {
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
				 
				 
				// ## for displaying the list in the table view
				List<AccountMaster> acm = DatabaseClient.getInstance().accountCodeApprovalList();
				System.out.println(acm.size());
					
				accountcode.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("accountcode"));
				accountname.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("accountname"));
				amount.setCellValueFactory(new PropertyValueFactory<AccountMaster,Double>("amount"));
				accountflag.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("report_flag"));
				accountlink.setCellValueFactory(new PropertyValueFactory<AccountMaster,String>("subglLink"));
				
				
				Callback<TableColumn<AccountMaster, Void>, TableCell<AccountMaster, Void>> cellFactory = new Callback<TableColumn<AccountMaster, Void>, TableCell<AccountMaster, Void>>() 
				{
		            @Override
		            public TableCell<AccountMaster, Void> call(final TableColumn<AccountMaster, Void> param) {
		                final TableCell<AccountMaster, Void> cell = new TableCell<AccountMaster, Void>() {
		                	
		                    private final Button btn = new Button("Add");

		                    {
		                        btn.setOnAction((ActionEvent event) -> 
		                        {
		                        	AccountMaster data = getTableView().getItems().get(getIndex());
		                            System.out.println("selectedData: " + data.getAccountcode());		                           
		                            
		                             //## approving selected account details. 		                           
		                             DatabaseClient.getInstance().approveUploadedList(data.getAccountcode());
		                        });
		                    }

		                    @Override
		                    public void updateItem(Void item, boolean empty) 
		                    {
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
				//onCreationFinished.run();
			
			}

		});
	     
	    addall.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				
				//## approving all the imported list.
				//default value = all
				DatabaseClient.getInstance().approveUploadedList("all");	
			}
		});
	    
	    cancel.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) 
			{
				DatabaseClient.getInstance().dataClean();	
			}
		});
		
	}

}
