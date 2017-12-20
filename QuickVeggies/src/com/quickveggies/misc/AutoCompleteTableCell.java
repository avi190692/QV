package com.quickveggies.misc;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;

//WRAPPER CLASS FOR AUTOCOMPLETE TEXT FIELD FOR IMPLEMENTATION IN TABLEVIEW
public class AutoCompleteTableCell<S, T> extends TableCell<S, T> {

    private com.quickveggies.misc.AutoCompleteTextField txtField;

    public AutoCompleteTableCell(AutoCompleteTextField txtField) {
        this.txtField = txtField;
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    public AutoCompleteTextField getTxtField() {
        return txtField;
    }

    public void setTxtField(AutoCompleteTextField txtField) {
        this.txtField = txtField;
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            txtField.setText(item.toString());
            setGraphic(txtField);
        }
    }

}
