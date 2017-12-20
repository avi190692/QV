package com.quickveggies.misc;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 * Wrapper class for numeric text field table cell. This table cell uses bounded
 * numeric text fields. To set the limit of characters/digits to be entered call the method getTextField
 * of this class, and set the maxLength property as described the java doc of BoundedNumericTextField class.  
 * 
 * 
 * @author Shoeb
 *
 * 
 */
public class NumericTextFieldTableCell<S,T> extends TextFieldTableCell<S,T>{
	
	    private Label textLabel = new Label();

        private BoundedNumericTextField txtField;

        public NumericTextFieldTableCell(BoundedNumericTextField txtField) {
            this.txtField = txtField;
            super.setGraphic(txtField);
            //setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
 
        public BoundedNumericTextField getTxtField(){
        	return txtField;
        }
        
        public void setTxtField(BoundedNumericTextField txtField){
        	this.txtField=txtField;
        }
        
        
        @Override
        public void updateItem(T item, boolean empty) {
            super.updateItem(item, empty); 
            if (empty) {
                setGraphic(null);
            } else {
            	textLabel.setText(item.toString());
                setGraphic(textLabel);
            }
        }
        
}
