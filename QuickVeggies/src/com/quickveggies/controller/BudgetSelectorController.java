package com.quickveggies.controller;

import com.quickveggies.DTO.BuyerDTO;
import com.quickveggies.service.BuyerService;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;

public class BudgetSelectorController {

	/**
	 * Buyer Mileston Table
	 * */
	@FXML
	private TableView<BuyerDTO> buyerMileStonTable;

	@FXML
	private TableColumn<BuyerDTO, String> buyerNameMT;

	@FXML
	private TableColumn<BuyerDTO, Double> buyerMilestoneProgressMT;

	@FXML
	private TableColumn<BuyerDTO, String> buyerCompanyNameMT;

	
	/**
	 * Buyer Budget Table
	 * */
	@FXML
	private TableView<BuyerDTO> buyerBudgetTable;

	@FXML
	private TableColumn<BuyerDTO, String> buyerNameBT;

	@FXML
	private TableColumn<BuyerDTO, String> buyerCompanyNameBT;

	@FXML
	private TableColumn<BuyerDTO, String> buyerTypeBT;

	@FXML
	private TableColumn<BuyerDTO, Double> buyerMilestonBT;

	@FXML
	private TableColumn<BuyerDTO, Double> buyerbudgetProgressBT;

	@FXML
	private TableColumn<BuyerDTO, String> buyerOverDueBT;

	@FXML
	private TableColumn<BuyerDTO, String> buyerActionBT;

	@FXML
	private void initialize() {
		setBuyerMileston();
		setBuyerBudget();
	}

	
	public void setBuyerMileston() {
		buyerMileStonTable.setItems(new BuyerService().getBuyerDto());
		buyerNameMT.setCellValueFactory(cellData -> cellData.getValue().getNameProp());
		buyerCompanyNameMT.setCellValueFactory(cellData -> cellData.getValue().getCompanyProp());
		buyerMilestoneProgressMT.setCellValueFactory(cellData -> cellData.getValue().getMilestoneProp().asObject());
		
	}
	
	public void setBuyerBudget(){
		buyerBudgetTable.setItems(new BuyerService().getBuyerDto());
		buyerNameBT.setCellValueFactory(cellData -> cellData.getValue().getNameProp());
		buyerCompanyNameBT.setCellValueFactory(cellData -> cellData.getValue().getCompanyProp());
		buyerMilestonBT.setCellValueFactory(cellData -> cellData.getValue().getMilestoneProp().asObject());
		buyerTypeBT.setCellValueFactory(cellData -> cellData.getValue().getBuyerTypeProp());
		buyerOverDueBT.setCellValueFactory(cellData -> cellData.getValue().getCreditPerioadProp());
		buyerActionBT.setCellValueFactory(cellData -> cellData.getValue().getBuyerActionProp());
		buyerbudgetProgressBT.setCellValueFactory(cellData -> cellData.getValue().getBudgetProgressProp().asObject());
		buyerbudgetProgressBT.setCellFactory(column -> new TableCell<BuyerDTO, Double>() {
			@Override
			protected void updateItem(Double item, boolean empty) {
				super.updateItem(item, empty);
				if (item != null && !empty) {
					setText(item.toString());

					double percent = item;

					String color = "169f16";
					if (percent > 90) {
						color = "ff0303";
					} else if (percent > 75) {
						color = "ffd742";
					}

					setStyle("-fx-background-color: linear-gradient(from 0% 100% to " + (percent) + "% 100%, #" + color
							+ ", #" + color + " 99.99%, transparent);");
				}
			}
		});
		
	}
}
