package com.quickveggies.controller;

import com.quickveggies.DTO.BuyerDTO;
import com.quickveggies.service.BuyerService;

import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class BudgetSelectorController {

	@FXML
	private TableView<BuyerDTO> budgetSelectionTable;

	@FXML
	private TableColumn<BuyerDTO, String> buyerEmail;

	@FXML
	private TableColumn<BuyerDTO, Double> milestoneProgress;

	@FXML
	private void initialize() {
		budgetSelectionTable.setItems(new BuyerService().getBuyerDto());
		
		buyerEmail.setCellValueFactory(cellData -> cellData.getValue().getEmailProp());
		milestoneProgress.setCellValueFactory(cellData -> cellData.getValue().getMilestoneProp().asObject());
		milestoneProgress.setCellFactory(column -> new TableCell<BuyerDTO, Double>(){
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if(item != null && !empty){
                    setText(item.toString());

                    double percent = item * 100;

                    String color = "169f16";
                    if(percent > 90){
                        color = "ff0303";
                    }else if(percent > 75){
                        color = "ffd742";
                    }

                    setStyle("-fx-background-color: linear-gradient(from 0% 100% to " + (percent) +"% 100%, #" + color + ", #" + color + " 99.99%, transparent);");
                }
            }
        });
	}
	
	
}
