package com.quickveggies.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.quickveggies.GeneralMethods;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.BoxSize;
import com.quickveggies.entities.QualityType;
import com.quickveggies.misc.CustomTextFieldTableCell;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

public class FruitEditController implements Initializable {

	@FXML
	private TableView<QualityType> tvQualityType;

	@FXML
	private TableView<BoxSize> tvBoxSizes;

	@FXML
	private TextField txtFruitType;

	@FXML
	private TextField txtQuality;

	@FXML
	private TextField txtBoxTypes;

	@FXML
	private Button btnAddNewQuality;

	@FXML
	private Button btnAddNewBoxSize;

	@FXML
	private Button btnSave;

	private ObservableList<QualityType> qualTypes = FXCollections.observableArrayList();

	private ObservableList<BoxSize> boxTypes = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		txtFruitType.setEditable(false);
		final String fruitToEdit = SessionDataController.getInstance().getNewFruitName();
		// reset to null, so if user closes the window, then this new fruit
		// won't be in session
		SessionDataController.getInstance().setNewFruitName(null);
		if (fruitToEdit == null || fruitToEdit.trim().isEmpty()) {
			throw new IllegalStateException("This controller should be used when modifying the fruit");
		}
		txtFruitType.setText(fruitToEdit);
		tvBoxSizes.setEditable(true);
		tvQualityType.setEditable(true);
		DatabaseClient dbClient = DatabaseClient.getInstance();
		boxTypes.addAll(dbClient.getBoxSizesForFruit(fruitToEdit));
		qualTypes.addAll(dbClient.getQualityTypesForFruit(fruitToEdit));
		tvQualityType.setItems(qualTypes);
		@SuppressWarnings("unchecked")
		TableColumn<QualityType, String> qTypeCol = (TableColumn<QualityType, String>) tvQualityType.getColumns()
				.get(0);
		qTypeCol.setCellValueFactory(new PropertyValueFactory<QualityType, String>("name"));
		qTypeCol.setCellFactory(new Callback<TableColumn<QualityType, String>, TableCell<QualityType, String>>() {
			@Override
			public TableCell<QualityType, String> call(TableColumn<QualityType, String> param) {
				CustomTextFieldTableCell<QualityType, String> qualTabCell = new CustomTextFieldTableCell<>();
				qualTabCell.setConverter(new DefaultStringConverter());
				return qualTabCell;
			}
		});
		qTypeCol.setEditable(true);
		@SuppressWarnings("unchecked")
		TableColumn<BoxSize, String> boxTypeCol = (TableColumn<BoxSize, String>) tvBoxSizes.getColumns().get(0);
		boxTypeCol.setCellValueFactory(new PropertyValueFactory<BoxSize, String>("name"));
		boxTypeCol.setCellFactory(new Callback<TableColumn<BoxSize, String>, TableCell<BoxSize, String>>() {
			@Override
			public TableCell<BoxSize, String> call(TableColumn<BoxSize, String> param) {
				CustomTextFieldTableCell<BoxSize, String> boxTabCell = new CustomTextFieldTableCell<>();
				boxTabCell.setConverter(new DefaultStringConverter());
				return boxTabCell;
			}
		});
		boxTypeCol.setEditable(true);
		qTypeCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<QualityType, String>>() {
			@Override
			public void handle(CellEditEvent<QualityType, String> event) {
				QualityType qt = event.getRowValue();
				qt.setName(event.getNewValue());
			}
		});

		boxTypeCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<BoxSize, String>>() {
			@Override
			public void handle(CellEditEvent<BoxSize, String> event) {
				BoxSize bs = event.getRowValue();
				bs.setName(event.getNewValue());
			}
		});
		btnAddNewQuality.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (!isValidFruit())
					return;
				String qualityType = txtQuality.getText();
				if (qualityType == null || qualityType.trim().isEmpty()) {
					GeneralMethods.errorMsg("Quality type cannot be empty");
					return;
				}
				qualTypes.add(new QualityType(qualityType));
				txtQuality.clear();
			}
		});
		tvBoxSizes.setItems(boxTypes);
		btnAddNewBoxSize.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String boxSizeType = txtBoxTypes.getText();
				if (boxSizeType == null || boxSizeType.trim().isEmpty()) {
					GeneralMethods.errorMsg("Box type cannot be empty");
					return;
				}
				boxTypes.add(new BoxSize(boxSizeType));
				txtBoxTypes.clear();
			}

		});

		btnSave.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				tvBoxSizes.refresh();
				tvQualityType.refresh();
				DatabaseClient dc = DatabaseClient.getInstance();
				List<String> listQualities = new ArrayList<>();
				List<String> listBoxes = new ArrayList<>();
				for (BoxSize bs : boxTypes) {
					listBoxes.add(bs.getName());
				}
				for (QualityType qt : qualTypes) {
					listQualities.add(qt.getName());
				}
				dc.deleteFruitDetails(fruitToEdit);
				//System.out.println(boxTypes);
				//System.out.println(qualTypes);
				dc.addFruitBoxSizes(fruitToEdit, listBoxes);
				dc.addFruitQualities(fruitToEdit, listQualities);
				SessionDataController session = SessionDataController.getInstance();
				if (!fruitToEdit.isEmpty())
					session.setNewFruitName(fruitToEdit);
				GeneralMethods.msg("Save process completed, please see the logs if there are any unreported errors!");
				txtFruitType.clear();
				tvBoxSizes.getItems().clear();
				tvQualityType.getItems().clear();
				btnSave.getScene().getWindow().hide();
			}
		});

	}

	private boolean isValidFruit() {
		boolean valid = true;
		String fruit = txtFruitType.getText();
		if (fruit == null || fruit.trim().isEmpty()) {
			GeneralMethods.errorMsg("Fruit name(type) cannot be empty");
			valid = false;
		}
		return valid;
	}

}
