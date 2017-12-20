package com.quickveggies.controller;
 
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.quickveggies.GeneralMethods;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.DBuyerTableLine;
import com.quickveggies.entities.DSalesTableLine;
import com.quickveggies.entities.DSupplierTableLine;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
 
 
public class UpdateEntryController implements Initializable {
     
    @FXML
    private Button commitButton;
    @FXML
    private Button cancelButton;
    @FXML
    private TableView updateEntryTable;
     
    private int lineId;
    private String tableName=null;
    private String[] colNamesList=null;
    private String[] oldValuesList=null;
    private String[] cellValuesFactoryList=null;
    private String[] sqlNames=null;
    
    private Object line;
    private ObservableList newLineWrapper=null;
    private String tableLineType=null;
     
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public UpdateEntryController(String tableLineType,String[] colNamesList,String lineId,String[] valuesList,String[] cellValuesFactoryList){
    	System.out.println("In updateEntryContoller()..");
        this.lineId=Integer.parseInt(lineId);
        this.colNamesList=colNamesList;
        this.oldValuesList=valuesList;
        this.cellValuesFactoryList=cellValuesFactoryList;
        this.tableLineType=tableLineType;
    }
     
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public void initialize(URL location, ResourceBundle resources) {
        //determine table name
        switch (tableLineType){
         case "DBuyerTableLine":
             tableName="buyerDeals";
             line=new DBuyerTableLine(oldValuesList);
             sqlNames=SessionDataController.dBuyerTableSqlColNames;
             break;
         case "DSupplierTableLine":
             tableName="supplierDeals";
             line=new DSupplierTableLine(oldValuesList);
             sqlNames=SessionDataController.dSupplierTableSqlColNames;
             break;
         case "DSalesTransTableLine":
             tableName="arrival";
             break;
          default :
             	System.out.print("wrong tableLineType in initialize() in UpdateEntryController");	

        }
        newLineWrapper=FXCollections.observableArrayList(line);
    	   	
        updateEntryTable.setFixedCellSize(50.0);
        updateEntryTable.setEditable(true);
    	System.out.println(colNamesList);
/*    	for (int idx=0; idx < colNamesList.length; idx++) {
        	System.out.print(colNamesList[idx] + "- ");
        }	System.out.println();
    	for (int idx=0; idx < cellValuesFactoryList.length; idx++) {
        	System.out.print(cellValuesFactoryList[idx] + "- ");
        }	System.out.println();
        System.out.println();
*/
    	for(int i=0;i<colNamesList.length;i++){
        
        	final String cellValueFactoryName=cellValuesFactoryList[i];
        	final TableColumn col1=new TableColumn(colNamesList[i]);
        	col1.setCellValueFactory(new PropertyValueFactory<DBuyerTableLine,String>(cellValuesFactoryList[i]));
        	col1.setCellFactory(TextFieldTableCell.forTableColumn());
        	//disable the total amount columns, they are to be calculated from rate X boxes
        	if(col1.getText().equals("Total sum") || col1.getText().equals("Net sum"))col1.setEditable(false);
        	col1.setOnEditCommit(
        		    new EventHandler<CellEditEvent<DBuyerTableLine, String>>() {
        		        @Override
        		        public void handle(CellEditEvent event) {
        		            switch (tableLineType){
        		            case "DBuyerTableLine":
        		            	   ((DBuyerTableLine) event.getTableView().getItems().get(
        		            			   event.getTablePosition().getRow())
        		                           ).set(cellValueFactoryName,event.getNewValue().toString());
        		            	   //if the edited cell is rate/cases, recalculate the net sum
        		            	   if(col1.getText().equals("Rate") || col1.getText().equals("Cases")){
        		            		   DBuyerTableLine line=
        		            		    ((DBuyerTableLine)(event.getTableView().getItems().get(0)));
        		            		   int totAmount = Integer.parseInt(line.getBuyerRate()) * line.getCases();
        		            		   line.setAmountedTotal(String.valueOf(totAmount));   
        		            		   
                                       GeneralMethods.refreshTableView(updateEntryTable, FXCollections.observableArrayList(line));
        		            	   }
        		                break;
        		            case "DSupplierTableLine":
     		            	   ((DSupplierTableLine) event.getTableView().getItems().get(
     		            			   event.getTablePosition().getRow())
     		                           ).set(cellValueFactoryName,event.getNewValue().toString());
     		             	   //if the edited cell is rate/cases, recalculate the net sum
    		            	   if(col1.getText().equals("Rate") || col1.getText().equals("Cases")){
    		            		   DSupplierTableLine line=
    		            		    ((DSupplierTableLine)(event.getTableView().getItems().get(0)));
    		            		   line.setNet(Integer.parseInt(line.getSupplierRate())*Integer.parseInt(line.getCases())-getSupplierCharges(line)+"");   
                                   GeneralMethods.refreshTableView(updateEntryTable, FXCollections.observableArrayList(line));
    		            	   }
        		                break;
        		            case "DSalesTransTableLine":
        		                tableName="arrival";
        		                break;
        		                default :
        		                	System.out.print("wrong tableLineType in getValuesFromTableLine in UpdateEntryController");	
        		           }
        		            
        		        }
        		    }
        		);
            updateEntryTable.getColumns().add(col1);
        }
        
        
        updateEntryTable.setItems(newLineWrapper);
        
        commitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //UPDATE THE CHANGES IN SQL
              DatabaseClient dbclient=DatabaseClient.getInstance();
             
              dbclient.updateTableEntry(tableName, UpdateEntryController.this.lineId,
                      sqlNames, 
                      getValuesFromTableLine(updateEntryTable,lineId,tableLineType)
                      ,true);
             commitButton.getScene().getWindow().hide();
            }
        });
         
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	
             cancelButton.getScene().getWindow().hide();
            }
        });
    }
     
    private String[] getValuesFromTableLine(TableView table,int lineId,String tableLineType){
        String[] result=null;
        Object line=table.getItems().get(0);
         
        switch (tableLineType){
        case "DBuyerTableLine":
            result=(((DBuyerTableLine)line).getAll());
            break;
        case "DSupplierTableLine":
        	result=(((DSupplierTableLine)line).getAll());
        	break;
        default :
        	System.out.print("wrong tableLineType -"+tableLineType+" in getValuesFromTableLine in UpdateEntryController\n");	
        }
         
        return result;
    }
    
    private double getSupplierCharges(DSupplierTableLine supplierLine){
    	double result=0;
    	DSalesTableLine salesline = getSaleTableLineById(supplierLine.getDealID());
    	String charges=salesline.getCharges();
    	result+=Double.parseDouble(charges);
    	return result;
    }
    
    private DSalesTableLine getSaleTableLineById(String dealId) {
		DatabaseClient dbclient = DatabaseClient.getInstance();
		DSalesTableLine salesline = null;
		try {
			salesline = dbclient.getSalesEntryLineByDealId(Integer.parseInt(dealId));
		} catch (SQLException e) {
			System.out.print("sqlexception in getSupplierCharges");
		}
		return salesline;
   	
    }
     
}