package com.quickveggies.misc;

import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;

public class CustomTextFieldTableCell<S, T> extends TextFieldTableCell<S, T> {
	
	public TextField getTextField() {
		return (TextField) super.getGraphic();
	}

}
