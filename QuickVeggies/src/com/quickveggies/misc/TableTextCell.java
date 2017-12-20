package com.quickveggies.misc;

import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;



public class TableTextCell<S,T> extends TableCell<S,T>{
	private final TextField txtField=new TextField();
	private String cellProperty="";

	//ArrayList<MailTableButtonCell> buttonColumCellsList
	public TableTextCell(String displayText){
		 
		 if(displayText!=null)txtField.setText(displayText);
	}

	public void setCellProperty(String value){
		cellProperty=value;
	}
	
	public TextField getTextField(){
		return txtField;
	}
	
	public String getCellProperty(){
		return cellProperty;
	}
	
    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
        	if(item!=null){
        		txtField.setText(item.toString());}
            setGraphic(txtField);
        }
    }
  
}
