package com.quickveggies.misc;

import com.quickveggies.GeneralMethods;
import com.quickveggies.controller.FreshEntryTableData;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;

public class CustomComboboxTableCell<S,T> extends TableCell<S, T> {

    private final ComboBox<T> comboBox ;
    

    public CustomComboboxTableCell(javafx.collections.ObservableList<T> items) {
        this.comboBox = new ComboBox<>(items);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }
    
    public void setComboBoxHandler(ChangeListener<T> listener){
    	comboBox.valueProperty().addListener(listener);
    }
    
    public ComboBox<T> getComboBox(){
    	return comboBox;
    }
    
    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            comboBox.setValue(item);
            setGraphic(comboBox);
        }
    }

}
