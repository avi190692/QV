package com.quickveggies.controller;

import com.quickveggies.GeneralMethods;
import com.quickveggies.model.LotTableColumnNameEnum;

import java.util.ArrayList;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

public class FreshEntryTableData {
	public static int ADDLOTS = 0, GROWER = 1, BUYER = 2;

	public static ArrayList<String[]> growerEntryLinesSql = new ArrayList<String[]>();
	public static ArrayList<String[]> buyerEntryLinesSql = new ArrayList<String[]>();

	public class FreshEntryTableLineDataReference {
		protected boolean active = true;

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean val) {
			active = val;
		}
	}

	// add lot
	public class AddLotTableLine extends FreshEntryTableLineDataReference {
		private String qualityType;
		private String qty = "0", fruit;
		private final BooleanProperty newRecord;

		public AddLotTableLine(String qualityType, String qty, String fruit) {
			this.setQualityType(qualityType);
			this.setQty(qty);
			this.setFruit(fruit);
			this.newRecord = new SimpleBooleanProperty(true);
		}

		public void set(LotTableColumnNameEnum type, String val) {
			if (!active) {
				GeneralMethods.errorMsg("Can't change lines associated with committed values");
				return;
			}
			switch (type) {
			case qualityType:
				setQualityType(val);
				break;
			case qty:
				setQty(val);
				break;
			case fruit:
				setFruit(val);
				break;
			}
		}

		public String getQualityType() {
			return qualityType;
		}

		public void setQualityType(String qualityType) {
			this.qualityType = qualityType;
		}

		public String getQty() {
			return qty;
		}

		public void setQty(String qty) {
			this.qty = qty;
		}

		public void reset() {
			qualityType = "";
			qty = "0";
		}

		public String getFruit() {
			return fruit;
		}

		public void setFruit(String fruit) {
			this.fruit = fruit;
		}

		/**
		 * @return the isNewRecord
		 */
		public boolean isNewRecord() {
			return newRecord.get();
		}

		/**
		 * @param isNewRecord the isNewRecord to set
		 */
		public void setNewRecord(boolean isNewRecord) {
			newRecord.set(isNewRecord);
		}
		
		public BooleanProperty newRecordProperty() {
			return newRecord;
		}

	}

	// grower entry
	public class GrowerEntryTableLine extends FreshEntryTableLineDataReference {
		private final String defaultGrowerQty = "0";
		private String boxSize;
		private String growerQty = "0";
		private String growerRate;
		private String quality;
		private final BooleanProperty newRecord;
		private boolean isNewRecord = true;

		public int getGrossIncome() {
			return Integer.parseInt(growerQty) * Integer.parseInt(growerRate);
		}

		public String getDefaultGrowerQty() {
			return defaultGrowerQty;
		}

		public void set(String type, String val) {
			if (!active) {
				GeneralMethods.errorMsg("Can't change lines associated with committed values");
				return;
			}
			switch (type) {
			case "boxSize":
				setBoxSize(val);
				break;
			case "growerQty":
				setGrowerQty(val);
				break;
			case "growerRate":
				setGrowerRate(val);
				break;
			case "quality":
				setQuality(val);
				break;
			}
		}

		public GrowerEntryTableLine(String boxSize, String growerQty, String growerRate, String quality) {
			this.boxSize = boxSize;
			this.growerQty = growerQty;
			this.growerRate = growerRate;
			this.quality = quality;
			newRecord = new SimpleBooleanProperty(true);
		}

		public String getBoxSize() {
			return boxSize;
		}

		public void setBoxSize(String boxSize) {
			this.boxSize = boxSize;
		}

		public String getGrowerQty() {
			return growerQty;
		}

		public void setGrowerQty(String growerQty) {
			this.growerQty = growerQty;
		}

		public String getGrowerRate() {
			return growerRate;
		}

		public void setGrowerRate(String growerRate) {
			this.growerRate = growerRate;
		}

		public void reset() {
			quality = "";
			boxSize = "";
			growerQty = "0";
			growerRate = "0";
		}

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

		public String getQuality() {
			return quality;
		}

		public void setQuality(String quality) {
			System.out.println("new quality=" + quality);
			this.quality = quality;
		}

		/**
		 * @return the isNewRecord
		 */
		public boolean isNewRecord() {
			return newRecord.get();
		}

		/**
		 * @param isNewRecord the isNewRecord to set
		 */
		public void setNewRecord(boolean isNewRecord) {
			newRecord.set(isNewRecord);
		}
		
		public BooleanProperty newRecordProperty() {
			return newRecord;
		}
	}

	// buyer entry
	public class BuyerEntryTableLine extends FreshEntryTableLineDataReference {
		private String buyerSelect;
		private String buyerQty = "0";
		private String buyerRate;
		private boolean active = true;
		private String boxSize;
		private String fruitQuality;

		public void set(String type, String val) {
			if (!active) {
				GeneralMethods.errorMsg("Can't change lines associated with committed values");
				return;
			}
			switch (type) {
			case "buyerSelect":
				setBuyerSelect(val);
				break;
			case "buyerQty":
				setBuyerQty(val);
				break;
			case "buyerRate":
				setBuyerRate(val);
				break;
			case "boxSize":
				setBoxSize(val);
				break;
			}
		}

		public String getBoxSize() {
			return boxSize;
		}

		public void setBoxSize(String boxSize) {
			this.boxSize = boxSize;
		}

		public BuyerEntryTableLine(String buyerSelect, String buyerQty, String buyerRate, String boxSize) {
			this.setBuyerSelect(buyerSelect);
			this.setBuyerQty(buyerQty);
			this.setBuyerRate(buyerRate);
			this.setBoxSize(boxSize);
		}

		public String getBuyerSelect() {
			return buyerSelect;
		}

		public void setBuyerSelect(String buyerSelect) {
			this.buyerSelect = buyerSelect;
		}

		public String getBuyerRate() {
			return buyerRate;
		}

		public void setBuyerRate(String buyerRate) {
			this.buyerRate = buyerRate;
		}

		public String getBuyerQty() {
			return buyerQty;
		}

		public void setBuyerQty(String buyerQty) {
			this.buyerQty = buyerQty;
		}

		public void reset() {
			buyerQty = "";
			buyerSelect = "";
			buyerQty = "0";
			buyerRate = "0";
		}

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}
		
		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append("buyer:");
			b.append(buyerSelect);
			b.append("-");
			b.append("Qty:");
			b.append(buyerQty);
			return b.toString();
		}

		public String getFruitQuality() {
			return fruitQuality;
		}

		public void setFruitQuality(String fruitQuality) {
			this.fruitQuality = fruitQuality;
		}

	}

	public AddLotTableLine getAddLotTableLine(String qualityType, String qty, String fruit) {
		return new AddLotTableLine(qualityType, qty, fruit);
	}

	public GrowerEntryTableLine getGrowerEntryTableLine(String boxSize, String growerQty, String growerRate,
			String quality) {
		System.out.println("Quality is:" + quality);
		return new GrowerEntryTableLine(boxSize, growerQty, growerRate, quality);
	}

	@SuppressWarnings("rawtypes")
	public void disableCommittedLines(ObservableList lines) {
		for (int k = 0; k < lines.size() - 1; k++) {
			// System.out.print("lst
			// try:"+((FreshEntryTableData.FreshEntryTableLineDataReference)(lines.get(k))).isActive()+"\n");
			((FreshEntryTableData.FreshEntryTableLineDataReference) (lines.get(k))).setActive(false);
		}
	}

}
