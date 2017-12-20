package com.quickveggies.controller;


import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

import com.quickveggies.GeneralMethods;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Charge;
import com.quickveggies.entities.DSalesTableLine;
import com.quickveggies.entities.DSupplierTableLine;
import com.quickveggies.misc.Utils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class SupplierAdjustRateController implements Initializable{
	
	@FXML
	private TextField txtNewRate ;
	
	@FXML
	private TextField txtOldRate;
	
	@FXML
	private Button btnSave ;
	
	private DSalesTableLine salesLine;
	
	private final DSupplierTableLine inProcessSupplierDeal;
	
	
	private static DatabaseClient dbClient = DatabaseClient.getInstance();
	
	public SupplierAdjustRateController(DSalesTableLine salesLine,DSupplierTableLine inProcessSupplierDeal ) {
		this.inProcessSupplierDeal = inProcessSupplierDeal;
		this.salesLine = salesLine;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		txtOldRate.setText(inProcessSupplierDeal.getSupplierRate());
		txtOldRate.setEditable(false);
		
		txtNewRate.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode().equals(KeyCode.ENTER)) {
					validateInput();
				}
			}
		});
		btnSave.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (validateInput()) {
					if (Utils.toInt(txtOldRate.getText().trim()).equals(Utils.toInt(txtNewRate.getText().trim())))  {
						GeneralMethods.msg("New rate is same as old rate, please recheck!");
						event.consume();
						txtNewRate.requestFocus();
						return;
					}
					List<Charge> chargesList = dbClient.getDealCharges(Utils.toInt(inProcessSupplierDeal.getDealID()));
					
					Integer oldRate = Utils.toInt(txtOldRate.getText().trim());
					Integer quantity = Utils.toInt(inProcessSupplierDeal.getCases());
					Integer oldDealGross = Utils.toInt(salesLine.getGross());
					Integer newGross = oldDealGross - (oldRate * quantity);
					Integer newRate = Utils.toInt(txtNewRate.getText().trim());
					newGross += newRate * quantity; 
				//	System.out.println("old gross" + oldGross +  ": new gross :" + newGross);
					Integer newCharges = 0;
					Integer chargeRate = 0;
					Integer saleDealTotQty = Utils.toInt(salesLine.getTotalQuantity()); 
					for (Charge charge : chargesList) {
						Integer tmpCharge = 0;
						chargeRate  = Utils.toInt(charge.getRate());
						if (charge.getType().trim().equals("@")) {
							tmpCharge = chargeAsMult(chargeRate, saleDealTotQty);
							charge.setAmount(tmpCharge.toString());
						}
						if (charge.getType().trim().equals("%")) {
				//			System.out.println("old charge amount" + charge.getAmount());
							tmpCharge = chargeAsPercent(chargeRate, newGross);
				//			System.out.println("new charge amount" + tmpCharge);
						}
						dbClient.updateTableEntry("charges", charge.getId(), "value", tmpCharge.toString(), null);
						newCharges += tmpCharge;
					}
					Integer newNet =  newGross - newCharges;
					//System.out.println("old net" + oldNet +  ": new net :" + newNet);

					String columns[] = new String[] {"gross", "net","charges"};
					String values[] = new String[] {newGross.toString(), newNet.toString(), newCharges.toString()};
					dbClient.updateTableEntry("arrival", Utils.toInt(salesLine.getSaleNo()), columns, values, false);
					dbClient.updateTableEntry("supplierDeals", Utils.toInt(inProcessSupplierDeal.getSaleNo()), new String[]{"supplierRate", "net"}, new String[]{newRate.toString(), newNet.toString()}, false);
					GeneralMethods.msg("Supplier rates have been updated. Please check logs for any errors");
					btnSave.getScene().getWindow().hide();
				}
			}
		});
	}
	

	private boolean validateInput() {
		boolean continueProcess = true;
		if (isValidNumber(txtNewRate.getText())) {
			btnSave.requestFocus();
		} else {
			GeneralMethods.errorMsg("Please enter correct value");
			txtNewRate.requestFocus();
			continueProcess = false;
		}
		return continueProcess;
	}
	
	private boolean isValidNumber(String num) {
		boolean isValid = true;
		try {
			Double.parseDouble(num);
		} catch (Exception ex ) {
			isValid = false;
		}
		return isValid;
	}
	
	private int chargeAsPercent(int percent, int total) {
		return (int) ((((float) percent) / 100) * total);
	}

	private int chargeAsMult(int mult, int total) {
		return mult * total;
	}



}
