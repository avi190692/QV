package com.quickveggies.misc;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;

public class CustomComboboxListCell<T> extends ListCell<T> {

    private final ComboBox<T> comboBox ;
    private boolean autosync=true;
    
    public void setAutosync(boolean val){
    	autosync=val;
    }
    

    public CustomComboboxListCell(javafx.collections.ObservableList<T> items) {
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
            if(autosync)comboBox.setValue(item);
            setGraphic(comboBox);
        }
    }

}
