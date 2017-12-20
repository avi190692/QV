package com.quickveggies.misc;

import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

public class TableButtonCell<S, T> extends TableCell<S, T> {

    private final Button button = new Button();
    private String cellProperty;

    public TableButtonCell(String displayText) {

        if (displayText != null) {
            button.setText(displayText);
        }
    }

    private void setCellProperty(String value) {
        cellProperty = value;
    }

    public Button getButton() {
        return button;
    }

    public String getCellProperty() {
        return cellProperty;
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            try {
                setCellProperty(item.toString());
            } catch (NullPointerException e) {
            }
            setGraphic(button);
        }
    }

}
