package com.quickveggies.controller;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.quickveggies.GeneralMethods;
import com.quickveggies.Main;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.ExpenseInfo;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ExpenditureTypeViewController implements Initializable {

	@FXML
	private TextField txtXtype;

	@FXML
	private Button btnDelete;

	@FXML
	private VBox vboxParent;

	@FXML
	private VBox vboxExpenseChild1;

	@FXML
	private Button btnAddExpenditure;

	@FXML
	private ScrollPane paneProducts;

	@FXML
	private AnchorPane ancPaneProducts;

	private Map<String, VBox> expenseContainerMap = new LinkedHashMap<>();

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		final DatabaseClient dbc = DatabaseClient.getInstance();

		EventHandler<ActionEvent> deleteEventBtnAction = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				final Button btn = (Button) event.getSource();
				final Stage dialogStage = new Stage();
				Button ok = new Button("OK");
				ok.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						String expenseToDelete = btn.getUserData().toString();
						// System.out.println(expenseToEdit);
						VBox vbToRemove = expenseContainerMap.get(expenseToDelete);
						if (vbToRemove != null) {
							vboxParent.getChildren().remove(vbToRemove);
							dbc.deleteExpenditureType(expenseToDelete);
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

				String msg = "You are going to delete selected Expenditure type.\n Are you sure?";
				// --------------------
				GeneralMethods.confirm(new Button[] { ok, cancel }, dialogStage, ExpenditureTypeViewController.this,
						msg);

			}
		};

		btnDelete.setOnAction(deleteEventBtnAction);
		paneProducts.setFitToHeight(true);
		paneProducts.setFitToWidth(true);
		
		addExpenditureList(dbc.getExpenditureTypeList());


		btnAddExpenditure.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				handleAddEditButton("/expendituretypeadd.fxml", "Add Expenditure Type");
			}
		});

	}
	
	private void addExpenditureList(List<String> expenses ) {
		
		
		VBox vb = vboxExpenseChild1;
		vboxParent.getChildren().remove(vb);
		int hbCount = 0;
		int vbCount = 0;
		for (String ei : expenses) {

			VBox vbChild = new VBox();
			vbCount++;
			vbChild.setId("vb" + vbCount);
			vboxParent.getChildren().add(vbChild);
			vbChild.setPrefHeight(vb.getPrefHeight());
			vbChild.setPrefWidth(vb.getPrefWidth());
			expenseContainerMap.put(ei, vbChild);
			for (Node cnHB : vb.getChildren()) {
				if (cnHB instanceof HBox) {
					hbCount++;
					HBox oldHb = (HBox) cnHB;
					HBox newHbox = new HBox();
					newHbox.setPrefHeight(oldHb.getPrefHeight());
					newHbox.setPrefWidth(oldHb.getPrefWidth());
					newHbox.setMinHeight(oldHb.getMinHeight());
					newHbox.setId("hb" + hbCount);
					for (Node cn : ((HBox) cnHB).getChildren()) {
						// System.out.println(cn.getClass());
						Node newNode = null;
						if (cn instanceof Label) {
							Label oldLabel = (Label) cn;
							Label newLabel = new Label();
							newLabel.setText(oldLabel.getText());
							newLabel.setFont(oldLabel.getFont());
							newLabel.setAlignment(oldLabel.getAlignment());
							newLabel.setContentDisplay(oldLabel.getContentDisplay());
							newLabel.setTextOverrun(oldLabel.getTextOverrun());
							newLabel.setPrefHeight(oldLabel.getPrefHeight());
							newLabel.setPrefWidth(oldLabel.getPrefWidth());
							newNode = newLabel;
						} else if (cn instanceof TextField) {
							TextField oldTf = (TextField) cn;
							TextField newTf = new TextField();
							switch (cn.getId()) {
							case "txtXtype": {
								newTf.setText(ei);
								break;
							}

							}
							newTf.setFont(oldTf.getFont());
							newTf.setEditable(false);
							newTf.setPrefWidth(oldTf.getPrefWidth());
							newTf.setPrefHeight(oldTf.getPrefHeight());
							newNode = newTf;
						} else if (cn instanceof HBox) {
							if (cn.getId().equals("hbDeleteBtn")) {
								HBox hbBtn = (HBox) cn;
								Button oldB = (Button) hbBtn.getChildren().get(0);
								HBox newHbBtn = new HBox();
								newHbBtn.setAlignment(hbBtn.getAlignment());
								newHbBtn.setPrefHeight(hbBtn.getPrefHeight());
								newHbBtn.setPrefWidth(hbBtn.getPrefWidth());
								Button newButton = new Button();
								newButton.setText(oldB.getText());

								newButton.setUserData(ei);
								newButton.setOnAction(oldB.getOnAction());
								newHbBtn.getChildren().add(newButton);
								newNode = newHbBtn;
							}
						}
						if (newNode != null) {
							//
							newNode.setLayoutX(cn.getLayoutX());
							newNode.setLayoutY(cn.getLayoutY());
							newHbox.getChildren().add(newNode);
						}
					}
					// System.out.println(childHbox.getId());
					vbChild.getChildren().add(newHbox);

					if (vbChild.getChildren().size() + 1 == vboxExpenseChild1.getChildren().size()) {
						HBox placeHolder = new HBox();
						placeHolder.setMinHeight(((HBox) vb.getChildren().get(0)).getPrefHeight() / 2);
						placeHolder.setPrefWidth(((HBox) vb.getChildren().get(0)).getPrefWidth());
						placeHolder.setId("ph" + ++hbCount);
						vbChild.getChildren().add(placeHolder);
					}
				}
			}
		}
		
	}

	private void handleAddEditButton(String resource, String title) {
		try {
			final Stage expenseStage = new Stage();
			expenseStage.centerOnScreen();
			expenseStage.setTitle(title);
			expenseStage.initModality(Modality.APPLICATION_MODAL);
			expenseStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent event) {
					Main.getStage().getScene().getRoot().setEffect(null);
				}
			});
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));

				Scene scene = new Scene((Parent) loader.load());
				scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
					public void handle(KeyEvent event) {
						if (event.getCode() == KeyCode.ESCAPE) {
							Main.getStage().getScene().getRoot().setEffect(null);
							expenseStage.close();
						}
					}
				});
				EventHandler<WindowEvent> expenseWindowEvent = new EventHandler<WindowEvent>() {
					public void handle(WindowEvent event) {
						try {
							SessionDataController session = SessionDataController.getInstance();
							ScrollPane paneProducts = (ScrollPane) session.getSettingPagePane().getChildren().get(1);
							VBox content = (VBox) paneProducts.getContent();
							content.getChildren().set(4,
									(Node) FXMLLoader.load(getClass().getResource("/fxml/expendituretypesviewer.fxml")));

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				expenseStage.setOnCloseRequest(expenseWindowEvent);
				expenseStage.setOnHidden(expenseWindowEvent);
				expenseStage.setScene(scene);
				expenseStage.show();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
