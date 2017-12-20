package com.quickveggies.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.quickveggies.GeneralMethods;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.BoxSize;
import com.quickveggies.entities.QualityType;
import com.quickveggies.misc.FruitButtonEventHandler;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FruitViewController implements Initializable {

	@FXML
	private ComboBox<String> cboFruitTypes;

	@FXML
	private ScrollPane scrollPaneFruitQuals;

	@FXML
	private ScrollPane scrollPaneFruitBoxSizes;

	@FXML
	private TextField txtFruitType;

	@FXML
	private Pane parentPane;

	@FXML
	private Button btnEdit;

	@FXML
	private Button btnDelete;

	private FruitViewController fruitViewControllerInstance;

	private SessionDataController session = SessionDataController.getInstance();
	final private DatabaseClient dbclient = DatabaseClient.getInstance();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cboFruitTypes.setVisible(true);
		fruitViewControllerInstance = this;
		parentPane.setStyle("-fx-background-color: #f2f6ff;");
		parentPane.setDisable(false);
		final List<String> fruitList = dbclient.getAllFruitTypes();
		if (!fruitList.isEmpty()) {
			String fruit = null;
			if (session.getNewFruitName() != null && !session.getNewFruitName().trim().isEmpty()) {
				fruit = session.getNewFruitName().trim();
			} else {
				fruit = fruitList.get(0);
			}
			if (fruit != null && !fruit.trim().isEmpty()) {
				cboFruitTypes.setValue(fruit);
				repopulateFruitInfo(fruit);
			}
		} else {
			parentPane.setDisable(true);
			return;
		}
		cboFruitTypes.setItems(FXCollections.observableArrayList(fruitList));
		cboFruitTypes.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue != null && (!newValue.equals(oldValue) || scrollPaneFruitQuals.getContent() == null)) {
					String fruit = newValue;
					repopulateFruitInfo(fruit);
				}
			}
		});
		/*
		 * if (fruit == null || fruit.isEmpty()) {
		 * cboFruitTypes.setVisible(true); txtFruitType.setVisible(false); }
		 * else { txtFruitType.setVisible(true);
		 * cboFruitTypes.setVisible(false);
		 * txtFruitType.setText(session.getNewFruitName());
		 * repopulateFruitInfo(fruit); session.setNewFruitName(null); }
		 */
		btnEdit.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String fruitName = null;
				fruitName = cboFruitTypes.getValue();
				if (fruitName == null || fruitName.trim().isEmpty()) {
					GeneralMethods.errorMsg("Please select a fruit to start the edit process");
					return;
				}
				Pane settingsScrollingPane = session.getSettingPagePane();
				session.setNewFruitName(fruitName);
				new FruitButtonEventHandler("/fruitEdit.fxml", "Edit Fruit Settings", settingsScrollingPane)
						.handle(event);
			}
		});

		btnDelete.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String fruitName = null;
				if (cboFruitTypes.isVisible()) {
					fruitName = cboFruitTypes.getValue();
				} else if (txtFruitType.isVisible()) {
					fruitName = txtFruitType.getText();
				}
				if (fruitName == null || fruitName.trim().isEmpty()) {
					GeneralMethods.errorMsg("Please select a fruit to delete");
					return;
				}
				final String finalFruitName = fruitName;
				final Stage dialogStage = new Stage();
				Button ok = new Button("OK");
				ok.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						fruitList.remove(finalFruitName);
						dbclient.deleteFruit(finalFruitName);
						cboFruitTypes.setItems(FXCollections.observableArrayList(fruitList));
						if (!fruitList.isEmpty()) {
							cboFruitTypes.setValue(fruitList.get(0));
						}
						dialogStage.close();
					}
				});

				Button cancel = new Button("Cancel");
				cancel.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						dialogStage.close();
					}
				});
				ok.setLayoutX(25.0);
				ok.setLayoutY(150.0);
				cancel.setLayoutX(150.0);
				cancel.setLayoutY(150.0);

				String msg = "You are going to delete selected fruit.\n Are you sure?";
				// --------------------
				GeneralMethods.confirm(new Button[] { ok, cancel }, dialogStage, FruitViewController.this, msg);

			}
		});

	}

	private void repopulateFruitInfo(String fruit) {
		List<BoxSize> boxSizes = dbclient.getBoxSizesForFruit(fruit);
		List<QualityType> qualityTypes = dbclient.getQualityTypesForFruit(fruit);
		List<String> qualityTypesList = new ArrayList<>();
		for (QualityType qt : qualityTypes) {
			qualityTypesList.add(qt.getName());
		}
		List<String> boxSizesList = new ArrayList<>();
		for (BoxSize bs : boxSizes) {
			boxSizesList.add(bs.getName());
		}
		VBox fruitBoxVB = new VBox();
		scrollPaneFruitBoxSizes.setFitToWidth(true);
		fruitBoxVB.setMinWidth(scrollPaneFruitBoxSizes.getMinWidth() - 5);
		fruitBoxVB.setPrefWidth(scrollPaneFruitBoxSizes.getPrefWidth() - 5);
		scrollPaneFruitBoxSizes.setContent(fruitBoxVB);
		for (int idx = 0; idx < boxSizesList.size(); idx++) {
			fruitBoxVB.getChildren().add(buildTextField(fruitBoxVB, boxSizesList.get(idx)));
		}
		VBox fruitQualVBox = new VBox();
		scrollPaneFruitQuals.setFitToWidth(true);
		scrollPaneFruitQuals.setContent(fruitQualVBox);
		fruitQualVBox.setMinWidth(scrollPaneFruitQuals.getMinWidth() - 5);
		fruitQualVBox.setPrefWidth(scrollPaneFruitQuals.getPrefWidth() - 5);
		for (int idx = 0; idx < qualityTypesList.size(); idx++) {
			fruitQualVBox.getChildren().add(buildTextField(fruitQualVBox, qualityTypesList.get(idx)));
		}

	}

	private TextField buildTextField(VBox vbox, String value) {
		TextField tf = new TextField();
		tf.setEditable(false);
		tf.setText(value);
		tf.setPrefWidth(vbox.getMaxWidth() - 5);
		return tf;
	}

}
