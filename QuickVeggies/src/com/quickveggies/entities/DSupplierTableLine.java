package com.quickveggies.entities;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class DSupplierTableLine {

    private String supplierTitle = "";
    private String saleNo = "";
    private String date = "";
    private String proprietor = "";
    private String cases = "";
    private String supplierRate = "";
    private String net = "";
    private String agent = "";
    private String dealID = "";
    private String amanat = "";

    private String amountReceived = "";

    private String orchard = "";

    private String qualityType;
    private String boxSizeType;
    private String fruit;

    private BooleanProperty isSelected = new SimpleBooleanProperty(false);

    public DSupplierTableLine(String id, String supplierTitle, String date,
            String supplierRate, String net, String cases, String agent,
            String amountReceived, String dealID, String proprietor, String amanat,
            String fruit, String qualityType, String boxSizeType) {
        this.saleNo = id;
        this.setSupplierTitle(supplierTitle);
        this.setDate(date);
        this.setSupplierRate(supplierRate);
        this.setNet(net);
        this.setCases(cases);
        this.setAgent(agent);
        this.setAmountReceived(amountReceived);
        this.setDealID(dealID);
        this.setProprietor(proprietor);
        this.setAmanat(amanat);
        this.fruit = fruit;
        this.qualityType = qualityType;
        this.boxSizeType = boxSizeType;
    }

    public DSupplierTableLine(String[] values) {
        if (values == null) {
            return;
        }
        if (values.length < 14) {
            System.out.println("Error in DSupplierTableLine constructor, array passed has size less than require one.");
        }
        this.setSaleNo(values[0]);
        this.setSupplierTitle(values[1]);
        this.setDate(values[2]);
        this.setSupplierRate(values[3]);
        this.setNet(values[4]);
        this.setCases(values[5]);
        this.setAgent(values[6]);
        this.setAmountReceived(values[7]);
        this.setDealID(values[8]);
        this.setProprietor(values[9]);
        this.setAmanat(values[10]);
        this.setFruit(values[11]);
        this.setQualityType(values[12]);
        this.setBoxSizeType(values[13]);
    }

    public String[] getAll() {
        return new String[]{getSaleNo(), getSupplierTitle(), getDate(), getSupplierRate(), getNet(), getCases(), getAgent(), getAmountReceived(), getDealID(), getProprietor(), getAmanat(), getFruit(), getQualityType(), getBoxSizeType()};
    }

    public void set(String dataToEdit, String newValue) {
        switch (dataToEdit) {
            case "supplierTitle":
                setSupplierTitle(newValue);
                break;
            case "date":
                setDate(newValue);
                break;
            case "net":
                setNet(newValue);
                break;
            case "cases":
                setCases(newValue);
                break;
            case "agent":
                setAgent(newValue);
                break;
            case "supplierRate":
                setSupplierRate(newValue);
                break;
            case "proprietor":
                setProprietor(newValue);
                break;
            case "amountReceived":
                setAmountReceived(newValue);
                break;
            case "dealID":
                setDealID(newValue);
                break;
            case "amanat":
                setAmanat(newValue);
                break;

            default:
                System.out.print("dataToEdit for DSupplierTableLine wasn't found\n");
        }
    }
    
    public String serialize() {
        Properties properties = new Properties();
        StringWriter stream = new StringWriter();
        String result;
        properties.setProperty("DealID", getDealID());
        properties.setProperty("SupplierTitle", getSupplierTitle());
        properties.setProperty("Date", date == null ? "" : date);
        properties.setProperty("Agent", getAgent());
        properties.setProperty("Amanat", getAmanat());
        properties.setProperty("AmountReceived", amountReceived == null ? "" : amountReceived);
        properties.setProperty("BoxSizeType", getBoxSizeType());
        properties.setProperty("Cases", getCases());
        properties.setProperty("Fruit", getFruit());
        properties.setProperty("Net", getNet());
        properties.setProperty("Orchard", getOrchard());
        properties.setProperty("Proprietor", getProprietor());
        properties.setProperty("QualityType", getQualityType());
        properties.setProperty("SaleNo", getSaleNo());
        properties.setProperty("SupplierRate", getSupplierRate());
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
        supplierTitle = properties.getProperty("SupplierTitle", null);
        date = properties.getProperty("Date", null);
        agent = properties.getProperty("Agent", null);
        amanat = properties.getProperty("Amanat", null);
        amountReceived = properties.getProperty("AmountReceived", null);
        boxSizeType = properties.getProperty("BoxSizeType", null);
        cases = properties.getProperty("Cases", null);
        fruit = properties.getProperty("Fruit", null);
        net = properties.getProperty("Net", null);
        orchard = properties.getProperty("Orchard", null);
        proprietor = properties.getProperty("Proprietor", null);
        qualityType = properties.getProperty("QualityType", null);
        saleNo = properties.getProperty("SaleNo", null);
        supplierRate = properties.getProperty("SupplierRate", null);
        return true;
    }

    public String getCases() {
        return cases;
    }
    
    public Integer getCasesInt() {
        return cases == null || cases.isEmpty() ? 0 : Integer.valueOf(cases);
    }

    public void setCases(String cases) {
        this.cases = cases;
    }

    public String getSaleNo() {
        return saleNo;
    }

    public void setSaleNo(String saleNo) {
        this.saleNo = saleNo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOrchard() {
        return orchard;
    }

    public void setOrchard(String orchard) {
        this.orchard = orchard;
    }

    public String getProprietor() {
        return proprietor;
    }

    public void setProprietor(String proprietor) {
        this.proprietor = proprietor;
    }

    public String getNet() {
        return net;
    }
    
    public Integer getNetInt() {
        return net == null || net.isEmpty()
                ? 0 : Integer.valueOf(net);
    }

    public void setNet(String net) {
        this.net = net;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getSupplierTitle() {
        return supplierTitle;
    }

    public void setSupplierTitle(String supplierTitle) {
        this.supplierTitle = supplierTitle;
    }

    public String getSupplierRate() {
        return supplierRate;
    }

    public void setSupplierRate(String supplierRate) {
        this.supplierRate = supplierRate;
    }

    public String getAmountReceived() {
        return amountReceived;
    }
    
    public Integer getAmountReceivedInt() {
        return amountReceived == null || amountReceived.isEmpty()
                ? 0 : Integer.valueOf(amountReceived);
    }

    public void setAmountReceived(String amountReceived) {
        this.amountReceived = amountReceived;
    }

    public String getDealID() {
        return dealID;
    }

    public void setDealID(String dealID) {
        this.dealID = dealID;
    }

    /**
     * @return the amanat
     */
    public String getAmanat() {
        return amanat;
    }

    /**
     * @param amanat the amanat to set
     */
    public void setAmanat(String amanat) {
        this.amanat = amanat;
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
