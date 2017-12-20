package com.quickveggies.entities;

import java.sql.SQLException;
import java.util.Properties;
import java.util.NoSuchElementException;
import java.io.StringReader;
import java.io.StringWriter;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

import com.quickveggies.dao.DatabaseClient;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBuyerTableLine {

    private final ReadOnlyObjectWrapper<Integer> amountedTotalProperty;
    private final ReadOnlyObjectWrapper<Integer> casesProperty;
    
    //Todo: add buyer Id from database
    private String buyerTitle = "";
    private String date = "";
    private String saleNo = "";
    private String buyerRate = "0";
    private String aggregatedAmount = "0";
    private String dealID = "";
    private String buyerType;
    private String qualityType;
    private String boxSizeType;
    private String fruit;

    private String amountReceived = "0";

    private BooleanProperty isSelected = new SimpleBooleanProperty(false);

    public DBuyerTableLine(String buyerTitle, String date, String buyerRate,
                           String amountedTotal, String cases, String saleNo, String amountReceived,
                           String aggregateAmt, String dealID, String fruit, String qualityType, String boxSizeType) 
    {
        amountedTotalProperty = new ReadOnlyObjectWrapper<>();
        casesProperty = new ReadOnlyObjectWrapper<>();
        this.setBuyerTitle(buyerTitle);
        this.setDate(date);
        this.setAmountedTotal(amountedTotal);
        this.setCases(cases);
        this.setBuyerRate(buyerRate);
        this.setAmountReceived(amountReceived);
        this.setDealID(dealID);
        this.saleNo = saleNo;
        this.aggregatedAmount = aggregateAmt;
        this.qualityType = qualityType;
        this.boxSizeType = boxSizeType;
        this.fruit = fruit;
        //get buyer type
        if (buyerTitle.length() == 0) {
            return;
        }
        try {
            setBuyerType(DatabaseClient.getInstance().getBuyerByName(buyerTitle).getBuyerType());
        } catch (SQLException | NoSuchElementException e) {
            System.out.println("sqlexception when fetching buyer type: " + e.getMessage());
        }
    }

    public DBuyerTableLine(String[] values) {
        amountedTotalProperty = new ReadOnlyObjectWrapper<>();
        casesProperty = new ReadOnlyObjectWrapper<>();
        if (values == null) {
            return;
        }
        this.setSaleNo(values[0]);
        this.setBuyerTitle(values[1]);
        this.setDate(values[2]);
        this.setBuyerRate(values[3]);
        this.setAmountedTotal(values[4]);
        this.setCases(values[5]);
        this.setAmountReceived(values[6]);
        this.setAggregatedAmount(values[7]);
        this.setDealID(values[8]);
        this.setFruit(values[9]);
        this.setQualityType(values[10]);
        this.setBoxSizeType(values[11]);
        // get buyer type
        if (getBuyerTitle().length() == 0) {
            return;
        }
        try {
            DatabaseClient dbClient = DatabaseClient.getInstance();
            String title = getBuyerTitle();
            Buyer buyer = dbClient.getBuyerByName(title);
            setBuyerType(buyer.getBuyerType());
        } catch (SQLException e) {
            System.out.println("sqlexception when fetching buyer type: " + e.getMessage());
        }
    }

    public String[] getAll() {
        return new String[]{getSaleNo(), getBuyerTitle(), getDate(), getBuyerRate(),
            amountedTotalProperty().getValue().toString(), casesProperty().getValue().toString(),
            getAmountReceived(), getAggregatedAmount(), getDealID(), getFruit(), getQualityType(), getBoxSizeType()};
    }

    public void set(String fieldToEdit, String newValue) {
        switch (fieldToEdit) {
            case "buyerTitle":
                setBuyerTitle(newValue);
                break;
            case "date":
                setDate(newValue);
                break;
            case "amountedTotal":
                setAmountedTotal(newValue);
                break;
            case "cases":
                setCases(newValue);
                break;
            case "buyerRate":
                setBuyerRate(newValue);
                break;
            case "amountRecieved":
                setAmountReceived(newValue);
                break;
            case "dealID":
                setDealID(newValue);
                break;
            default:
                System.out.print("dataToEdit for DBuyerTableLine wasn't found\n");
        }
    }
    
    public String serialize() {
        Properties properties = new Properties();
        StringWriter stream = new StringWriter();
        String result;
        properties.setProperty("DealID", getDealID());
        properties.setProperty("BuyerTitle", getBuyerTitle());
        properties.setProperty("Date", date == null ? "" : date);
        properties.setProperty("AmountedTotal", amountedTotalProperty.isNull().getValue()
                ? "" : amountedTotalProperty.getValue().toString());
        properties.setProperty("Cases", casesProperty.isNull().getValue()
                ? "" : casesProperty.getValue().toString());
        properties.setProperty("BuyerRate", buyerRate == null ? "" : buyerRate);
        properties.setProperty("AmountReceived", amountReceived == null ? "" : amountReceived);
        properties.setProperty("AggregatedAmount", aggregatedAmount == null ? "" : aggregatedAmount);
        properties.setProperty("BoxSizeType", boxSizeType == null ? "" : boxSizeType);
        properties.setProperty("BuyerType", buyerType == null ? "" : buyerType);
        properties.setProperty("Fruit", fruit == null ? "" : fruit);
        properties.setProperty("QualityType", qualityType == null ? "" : qualityType);
        properties.setProperty("SaleNo", getSaleNo());
        try {
            properties.store(stream, "");
            result = stream.getBuffer().toString();
        }
        catch (IOException ex) {
            result = "";
            Logger.getLogger(DBuyerTableLine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public boolean deserialize(String data) {
        if (data == null) {
            return false;
        }
        Properties properties = new Properties();
        StringReader stream = new StringReader(data);
        try {
            properties.load(stream);
        } catch (IOException ex) {
            Logger.getLogger(DBuyerTableLine.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        dealID = properties.getProperty("DealID", null);
        buyerTitle = properties.getProperty("BuyerTitle", null);
        date = properties.getProperty("Date", null);
        amountedTotalProperty.set(Integer.valueOf(properties.getProperty("AmountedTotal", null)));
        casesProperty.set(Integer.valueOf(properties.getProperty("Cases", null)));
        buyerRate = properties.getProperty("BuyerRate", null);
        amountReceived = properties.getProperty("AmountReceived", null);
        aggregatedAmount = properties.getProperty("AggregatedAmount", null);
        boxSizeType = properties.getProperty("BoxSizeType", null);
        buyerType = properties.getProperty("BuyerType", null);
        fruit = properties.getProperty("Fruit", null);
        qualityType = properties.getProperty("QualityType", null);
        saleNo = properties.getProperty("SaleNo", null);
        return true;
    }
    
    @Override
    public DBuyerTableLine clone() throws CloneNotSupportedException {
        DBuyerTableLine newLine = new DBuyerTableLine(null);
        newLine.setAggregatedAmount(aggregatedAmount);
        newLine.setAmountReceived(amountReceived);
        newLine.setAmountedTotal(amountReceived);
        newLine.setBoxSizeType(boxSizeType);
        newLine.setBuyerRate(buyerRate);
        newLine.setBuyerTitle(buyerTitle);
        newLine.setBuyerType(buyerType);
        newLine.setCases(getCases().toString());
        newLine.setDate(date);
        newLine.setDealID(dealID);
        newLine.setFruit(fruit);
        newLine.setQualityType(qualityType);
        newLine.setSaleNo(saleNo);
        newLine.setIsSelected(isSelected);
        return newLine;
    }

    public String getBuyerTitle() {
        return buyerTitle;
    }

    public void setBuyerTitle(String buyerTitle) {
        this.buyerTitle = buyerTitle;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    
    public Integer getAmountedTotal() {
        return this.amountedTotalProperty.get();
    }

    public void setAmountedTotal(String amountedTotal) {
        try {
            this.amountedTotalProperty.setValue(Integer.parseInt(amountedTotal));
        }
        catch (NumberFormatException ex) {
            this.amountedTotalProperty.setValue(0);
        }
    }
    
    public ReadOnlyObjectProperty<Integer> amountedTotalProperty() {
        return this.amountedTotalProperty.getReadOnlyProperty();
    }
    
    public Integer getCases() {
        return this.casesProperty.get();
    }

    public void setCases(String cases) {
        try {
            this.casesProperty.setValue(Integer.parseInt(cases));
        }
        catch (NumberFormatException ex) {
            this.casesProperty.setValue(0);
        }
    }
    
    public ReadOnlyObjectProperty<Integer> casesProperty() {
        return this.casesProperty.getReadOnlyProperty();
    }

    public String getSaleNo() {
        return saleNo;
    }

    public void setSaleNo(String saleNo) {
        this.saleNo = saleNo;
    }

    public String getBuyerRate() {
        return buyerRate;
    }

    public void setBuyerRate(String buyerRate) {
        this.buyerRate = buyerRate;

    }

    public String getAmountReceived() {
        return amountReceived;
    }

    public void setAmountReceived(String amountReceived) {
        this.amountReceived = amountReceived;
    }

    public String getBuyerType() {
        return buyerType;
    }

    public void setBuyerType(String buyerType) {
        this.buyerType = buyerType;
    }

    public String getDealID() {
        return dealID;
    }

    public void setDealID(String dealID) {
        this.dealID = dealID;
    }

    public String getAggregatedAmount() {
        return aggregatedAmount;
    }
    
    public void setAggregatedAmount(String aggregatedAmount) {
        this.aggregatedAmount = aggregatedAmount;
    }

    public boolean isLadaanEdited() {
        LadaanBijakSaleDeal deal = DatabaseClient.getInstance().getLadBijSaleDeal(Integer.valueOf(this.dealID));
        if (deal == null) {
            return false;
        }
        return true;
    }

    public BooleanProperty getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(BooleanProperty isSelected) {
        this.isSelected = isSelected;
    }

    public String getQualityType() {
        return qualityType;
    }

    public void setQualityType(String qualityType) {
        this.qualityType = qualityType;
    }

    public String getBoxSizeType() {
        return boxSizeType;
    }

    public void setBoxSizeType(String boxSizeType) {
        this.boxSizeType = boxSizeType;
    }

    public String getFruit() {
        return fruit;
    }

    public void setFruit(String fruit) {
        this.fruit = fruit;
    }

    public boolean isTotalLine() {
        return false;
    }
}
