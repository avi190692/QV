package com.quickveggies.entities;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DSalesTableLine {

    private String fruit = "";
    private String saleNo = "";
    private String date = "";
    private String type = "";
    private String challan = "";
    private String supplier = "";
    private String totalQuantity = "";
    private String fullCase = "";
    private String halfCase = "";
    private String agent = "";
    private String truck = "";
    private String driver = "";
    private String gross = "";
    private String charges = "";
    private String net = "";
    private String remarks = "";
    private String dealID = "";
    private String amanat = "";

    public DSalesTableLine() {
    }
    
    public DSalesTableLine(String fruit, String saleNo, String date, String challan, String supplier,
            String totalQuantity, String fullCase, String halfCase, String agent, String truck, String driver,
            String gross, String charges, String net, String remarks, String dealID, String type, String amanat) {
        super();
        this.fruit = fruit;
        this.saleNo = saleNo;
        this.date = date;
        this.challan = challan;
        this.supplier = supplier;
        this.totalQuantity = totalQuantity;
        this.fullCase = fullCase;
        this.halfCase = halfCase;
        this.agent = agent;
        this.truck = truck;
        this.driver = driver;
        this.gross = gross;
        this.charges = charges;
        this.net = net;
        this.remarks = remarks;
        this.dealID = dealID;
        this.type = type;
        this.amanat = amanat;
    }
    
    public String serialize() {
        Properties properties = new Properties();
        StringWriter stream = new StringWriter();
        String result;
        properties.setProperty("DealID", getDealID());
        properties.setProperty("SupplierTitle", getSupplier());
        properties.setProperty("Date", date == null ? "" : date);
        properties.setProperty("Agent", getAgent());
        properties.setProperty("Amanat", getAmanat());
        properties.setProperty("Challan", getChallan());
        properties.setProperty("Charges", getCharges());
        properties.setProperty("Driver", getDriver());
        properties.setProperty("Fruit", getFruit());
        properties.setProperty("FullCase", getFullCase());
        properties.setProperty("Gross", getGross());
        properties.setProperty("HalfCase", getHalfCase());
        properties.setProperty("SaleNo", getSaleNo());
        properties.setProperty("Net", getNet());
        properties.setProperty("Remarks", getRemarks());
        properties.setProperty("TotalQuantity", getTotalQuantity());
        properties.setProperty("Truck", getTruck());
        properties.setProperty("Type", getType());
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
        supplier = properties.getProperty("SupplierTitle", null);
        date = properties.getProperty("Date", null);
        agent = properties.getProperty("Agent", null);
        amanat = properties.getProperty("Amanat", null);
        fruit = properties.getProperty("Fruit", null);
        net = properties.getProperty("Net", null);
        saleNo = properties.getProperty("SaleNo", null);
        challan = properties.getProperty("Challan", null);
        charges = properties.getProperty("Charges", null);
        driver = properties.getProperty("Driver", null);
        fullCase = properties.getProperty("FullCase", null);
        gross = properties.getProperty("Gross", null);
        halfCase = properties.getProperty("HalfCase", null);
        remarks = properties.getProperty("Remarks", null);
        totalQuantity = properties.getProperty("TotalQuantity", null);
        truck = properties.getProperty("Truck", null);
        type = properties.getProperty("Type", null);
        return true;
    }

    public String getFruit() {
        return fruit;
    }

    public void setFruit(String fruit) {
        this.fruit = fruit;
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

    public String getChallan() {
        return challan;
    }

    public void setChallan(String challan) {
        this.challan = challan;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getTotalQuantity() {
        return totalQuantity;
    }
    
    public Integer getTotalQuantityInt() {
        return totalQuantity == null ? 0 : Integer.valueOf(totalQuantity);
    }

    public void setTotalQuantity(String totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public String getFullCase() {
        return fullCase;
    }

    public void setFullCase(String fullCase) {
        this.fullCase = fullCase;
    }

    public String getHalfCase() {
        return halfCase;
    }

    public void setHalfCase(String halfCase) {
        this.halfCase = halfCase;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getTruck() {
        return truck;
    }

    public void setTruck(String truck) {
        this.truck = truck;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getGross() {
        return gross;
    }

    public void setGross(String gross) {
        this.gross = gross;
    }

    public String getCharges() {
        return charges;
    }

    public void setCharges(String charges) {
        this.charges = charges;
    }

    public String getNet() {
        return net;
    }
    
    public Integer getNetInt() {
        return net == null ? 0 : Integer.valueOf(net);
    }

    public void setNet(String net) {
        this.net = net;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getDealID() {
        return dealID;
    }

    public void setDealID(String dealID) {
        this.dealID = dealID;
    }

    public String getType() {
        return type;
    }

    public String getAmanat() {
        return amanat;
    }

    public void setAmanat(String amanat) {
        this.amanat = amanat;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns charges excluding "Amanat"
     *
     * @return
     */
    public String getNoAmanatCharges() {
        String chargesMinusAmanat = charges;
        if (amanat != null && !amanat.trim().isEmpty()) {
            try {
                int intAmanat = Integer.valueOf(amanat);
                int remainingChgs = Integer.valueOf(charges) - intAmanat;
                chargesMinusAmanat = String.valueOf(remainingChgs);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }

        }
        return chargesMinusAmanat;
    }
    
    public boolean isTotalLine() {
        return false;
    }
}
