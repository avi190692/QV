package com.quickveggies.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Charge;
import com.quickveggies.entities.DBuyerTableLine;
import com.quickveggies.entities.DSupplierTableLine;
import com.quickveggies.entities.ExpenseInfo;
import com.quickveggies.entities.TeepDetail;
import com.quickveggies.misc.Utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class TeepDetailController implements Initializable {

	@FXML
	private TableView<TeepDetail> tvChargeDetails;

	@FXML
	private Label lblDate;

	@FXML
	private Label lblOrchard;

	private DBuyerTableLine buyerDeal;


	private ObservableList<TeepDetail> teepDetailList = FXCollections.observableArrayList();
	
	private String supplierNetAmount;
	
	private DSupplierTableLine supplierDeal;

	private static DatabaseClient dbClient = DatabaseClient.getInstance();

	public TeepDetailController(DBuyerTableLine buyerDeal, DSupplierTableLine supplierDeal, String supplierNetAmount) {
		this.buyerDeal = buyerDeal;
		this.supplierDeal = supplierDeal;
		this.supplierNetAmount = supplierNetAmount;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		lblOrchard.setText(supplierDeal.getSupplierTitle());
		lblDate.setText(buyerDeal.getDate());
		TeepDetail td = new TeepDetail();
		td.setColumn("Gross Amount");
		td.setBuyer(buyerDeal.getAggregatedAmount());
		td.setSupplier(supplierNetAmount);

		teepDetailList.add(td);
		List<Charge> supplierChargeList = dbClient.getDealCharges(Utils.toInt(supplierDeal.getDealID()));
		for (Charge ei : supplierChargeList) {
			td = new TeepDetail();
			td.setColumn(ei.getName());
			td.setSupplier(ei.getAmount());
			td.setBuyer("0");
			teepDetailList.add(td);
		}
		List<ExpenseInfo> expenseInfoList = dbClient.getBuyerExpenseInfoList();
		for (ExpenseInfo ei : expenseInfoList) {
			td = new TeepDetail();
			td.setColumn(ei.getName());
			td.setSupplier("0");
			td.setBuyer(ei.getDefaultAmount());
			teepDetailList.add(td);
		}
		tvChargeDetails.setItems(teepDetailList);
		setupColumns();

	}

	private void setupColumns() {
		TableColumn<TeepDetail, String> columnCol = new TableColumn<>("Column");
		columnCol.setCellValueFactory(new PropertyValueFactory<TeepDetail, String>("column"));
		//
		TableColumn<TeepDetail, String> supplierCol = new TableColumn<>("Supplier");
		supplierCol.setCellValueFactory(new PropertyValueFactory<TeepDetail, String>("supplier"));
		//
		TableColumn<TeepDetail, String> buyerCol = new TableColumn<>("Buyer");
		buyerCol.setCellValueFactory(new PropertyValueFactory<TeepDetail, String>("buyer"));
		//
		tvChargeDetails.getColumns().addAll(new TableColumn[] { columnCol, supplierCol, buyerCol });

	}

}
