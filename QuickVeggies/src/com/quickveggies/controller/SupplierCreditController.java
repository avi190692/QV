package com.quickveggies.controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.MoneyPaidRecd;
import com.quickveggies.entities.Supplier;
import com.quickveggies.model.EntityType;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.util.Callback;

public class SupplierCreditController implements Initializable {

	@FXML
	private TableView<MoneyPaidRecd> tvCredits;

	@FXML
	private Pane dialog_pane;

	@FXML
	private ScrollPane scrPane;

	@FXML
	private AnchorPane ancPane;
	
	private Map<String, String> supplierTitlePropMap = new LinkedHashMap<>(); 


	private ObservableList<MoneyPaidRecd> paymentList;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		scrPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		DatabaseClient dbc = DatabaseClient.getInstance();
		List<MoneyPaidRecd> moneyPaidList = dbc.getAdvanceMoneyPaidList(EntityType.SUPPLIER);
		for (MoneyPaidRecd mpr : moneyPaidList) {
			String title = mpr.getTitle();
			if (!supplierTitlePropMap.containsKey(title)) {
				try {
					Supplier supplier = dbc.getSupplierByName(title);
					supplierTitlePropMap.put(title, supplier.getProprietor());
				} catch (NoSuchElementException | SQLException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		paymentList = FXCollections.observableArrayList(moneyPaidList);
		// uncomment below for quick testing
		/*
		 * for (int i=0; i < 40; i++) { paymentList.addAll(moneyPaidList); }
		 */
		int size = paymentList.size();
		if (size > 7) {
			double height = tvCredits.getPrefHeight();
			int ratio = size / 5;
			if (ratio > 5) {
				ratio = 5;
			}
			Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
			double scrHeight = primaryScreenBounds.getHeight();
			height = (ratio * height);
			if (height >= (scrHeight - 25)) {
				height = scrHeight - 25;
			}

			dialog_pane.setPrefHeight(height);
			ancPane.setPrefHeight(height - 10);
			scrPane.setPrefHeight(height - 13);
			// tvCredits.prefHeight(height + 7);
			tvCredits.setMinHeight(height - 15);
		}
		tvCredits.setItems(paymentList);
		setupColumns();
	}

	@SuppressWarnings("unchecked")
	private void setupColumns() {
		TableColumn<MoneyPaidRecd, String> serialCol = new TableColumn<>("S.No.");
		serialCol.setCellValueFactory(new Callback<CellDataFeatures<MoneyPaidRecd, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<MoneyPaidRecd, String> p) {
				return new ReadOnlyObjectWrapper<String>(tvCredits.getItems().indexOf(p.getValue()) + 1 + "");
			}
		});
		TableColumn<MoneyPaidRecd, String> titleCol = new TableColumn<>("Title");
		titleCol.setCellValueFactory(new PropertyValueFactory<MoneyPaidRecd, String>("title"));
		//
		TableColumn<MoneyPaidRecd, String> proprietorCol = new TableColumn<>("Propreitor");
		proprietorCol.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<MoneyPaidRecd, String>, ObservableValue<String>>() {
					public ObservableValue<String> call(CellDataFeatures<MoneyPaidRecd, String> param) {
						return new ReadOnlyStringWrapper(supplierTitlePropMap.get(param.getValue().getTitle()));
					}
				});

		//
		TableColumn<MoneyPaidRecd, String> dateCol = new TableColumn<>("Payment Date");
		dateCol.setCellValueFactory(new PropertyValueFactory<MoneyPaidRecd, String>("date"));
		//
		TableColumn<MoneyPaidRecd, String> amountCol = new TableColumn<>("Advance Pay");
		amountCol.setCellValueFactory(new PropertyValueFactory<MoneyPaidRecd, String>("paid"));

		tvCredits.getColumns().addAll(new TableColumn[] { serialCol, titleCol, proprietorCol, dateCol, amountCol });
	}

}
