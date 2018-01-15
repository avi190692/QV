package com.quickveggies.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.quickveggies.GeneralMethods;
import com.quickveggies.Main;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.ExpenseInfo;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class AddChargesController implements Initializable {

	@FXML
	private TextField amanatRate;

	@FXML
	private Label amanatName;

	@FXML
	private Button save;

	@FXML
	private Text amanatValue;

	@FXML
	private ComboBox<String> amanatType;

	@FXML
	private VBox vbChargeTypes;

	@FXML
	private HBox hbAmanatCharge;

	@FXML
	private HBox hbAmanatCValue, hbAmanatCName, hbAmanatCType, hbAmanatCRate;

	private Map<String, ChargeTypeValueMap> chargeValueMap;

	private String[] chargeTypes = new String[] { "%", "@" };
	
	private SessionDataController sessionController = SessionDataController.getInstance();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		final List<Node[]> chargeControls = new ArrayList<>();
		chargeValueMap = new LinkedHashMap<>();
		ChargeTypeValueMap ctvMap = new ChargeTypeValueMap();
		ctvMap.totalValue = amanatValue;
		ctvMap.type= amanatType;
		ctvMap.rate = amanatRate;
		chargeValueMap.put(amanatName.getText(), ctvMap);
		amanatType.setItems(FXCollections.observableArrayList(chargeTypes));
		amanatType.setValue("%");
		amanatRate.focusedProperty().addListener(createUpdateChargeListener(amanatRate, amanatValue, amanatType));
		List<ExpenseInfo> eiList = DatabaseClient.getInstance().getExpenseInfoList();
		int count = 0;
		if(eiList.size() >= 2){
		for (ExpenseInfo ei : eiList) {
			if (ei != null && ei.getName().trim().equalsIgnoreCase("amanat")) {
				amanatName.setText(ei.getName().trim());
				amanatType.setValue(ei.getType());
				amanatRate.setText(ei.getDefaultAmount());
				chargeControls.add(new Node[] { amanatRate, amanatValue, amanatType });
				continue;
			}
			Label name = new Label(ei.getName());
			ComboBox<String> type = new ComboBox<>();
			type.setId("type" + count);
			type.setItems(FXCollections.observableArrayList(chargeTypes));
			type.setValue(ei.getType());
			TextField rate = new TextField();
			rate.setId("rate" + count);
			rate.setText(ei.getDefaultAmount());
			Text totalValue = new Text("0");
			ctvMap = new ChargeTypeValueMap();
			ctvMap.totalValue = totalValue;
			ctvMap.type = type;
			ctvMap.rate = rate;
			chargeValueMap.put(ei.getName(), ctvMap);
			totalValue.setId("totalValue" + count);
			chargeControls.add(new Node[] { rate, totalValue, type });
			rate.focusedProperty().addListener(createUpdateChargeListener(rate, totalValue, type));
			HBox currChargeHB = new HBox();
			currChargeHB.setPrefHeight(hbAmanatCharge.getPrefHeight());
			currChargeHB.setPrefWidth(hbAmanatCharge.getPrefWidth());
			for (Node acHbChild : hbAmanatCharge.getChildren()) {
				if (acHbChild instanceof HBox) {
					HBox hb = (HBox) acHbChild;
					HBox nhb = new HBox();
					currChargeHB.getChildren().add(nhb);
					nhb.setAlignment(hb.getAlignment());
					nhb.setPrefHeight(hb.getPrefHeight());
					nhb.setPrefWidth(hb.getPrefWidth());
					Node childControl = hb.getChildren().get(0);
					Node newNode = null;
					if (childControl == amanatRate) {
						newNode = rate;
					} else if (childControl == amanatType) {
						newNode = type;
					} else if (childControl == amanatValue) {
						newNode = totalValue;
					} else if (childControl == amanatName) {
						newNode = name;
					}
					if (newNode != null) {
						/*
						 * this block is necessary as some of the controls may
						 * not be of type control, so they may not get added to
						 * Hbox
						 */
						nhb.getChildren().add(newNode);
					}
					if (newNode instanceof Control) {
						Control newC = (Control) newNode;
						Control oldC = (Control) childControl;
						newC.setPrefWidth(oldC.getPrefWidth());
						newC.setPrefHeight(oldC.getPrefHeight());
					}
				}
			}
		
			vbChargeTypes.getChildren().add(currChargeHB);
		}
		}

		save.setOnAction(new EventHandler<ActionEvent>() {
			@SuppressWarnings("unchecked")
			@Override
			public void handle(ActionEvent event) {
				Map<String, ChargeTypeValueMap> charges = new LinkedHashMap<>();
				for (Node[] nodes : chargeControls) {
					TextField rate = (TextField) nodes[0];
					String strRate = rate.getText() == null ? "" : rate.getText().trim();
					if (!strRate.isEmpty() && !strRate.equals("0")) {
						Text totalVale = (Text) nodes[1];
						String strTV = totalVale.getText() == null ? "0" : totalVale.getText().trim();
						if (strTV.isEmpty() || strTV.equals("0")) {
							ComboBox<String> type = (ComboBox<String>) nodes[2];
							calculateTotal(rate, totalVale, type);
						}
					}
				}
				for (String name : chargeValueMap.keySet()) {
					charges.put(name, chargeValueMap.get(name));
				}
				sessionController.setCharges(charges);
				sessionController.freshEntryUpdateAmountCalc();
				Main.closeCurrentStage(FreshEntryController.getChargesView());
			}
		});
	}

	private ChangeListener<Boolean> createUpdateChargeListener(final TextField rateTextField, final Text valueText,
			final ComboBox<String> chargeCalcType) {
		return new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean,
					Boolean aBoolean2) {
				if (aBoolean2) {
				} else {
					calculateTotal(rateTextField, valueText, chargeCalcType);
				}
			}
		};
	}

	private void calculateTotal(final TextField rateTextField, final Text valueText,
			final ComboBox<String> chargeCalcType) {
		if (rateTextField != null && !rateTextField.getText().trim().isEmpty()) {
			if (isNumberEntered(rateTextField.getText())) {
				sessionController.freshEntryUpdateAmountCalc();
				String calcType = chargeCalcType.getValue().toString();
				String fieldTotal = null;
				if (calcType.equals("%")) {
					fieldTotal = chargeAsPercent(Integer.parseInt(rateTextField.getText()),
							sessionController.getSupplierGrossSum()) + "";
				}

				else if (calcType.equals("@")) {
					fieldTotal = chargeAsMult(Integer.parseInt(rateTextField.getText()),
							sessionController.getTotalBoxes()) + "";
				} else {
					throw new IllegalArgumentException("Invalid type detected");
				}
				if (rateTextField == amanatRate) {
					sessionController.setAmanatTotal(fieldTotal);
				}
				valueText.setText(fieldTotal);

			} else {
				GeneralMethods.errorMsg("Value must be an integer!");
			}
		}

	}

	private int chargeAsPercent(int percent, int total) {
		return (int) ((((float) percent) / 100) * total);
	}

	private int chargeAsMult(int mult, int total) {
		return mult * total;
	}

	private static boolean isNumberEntered(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
