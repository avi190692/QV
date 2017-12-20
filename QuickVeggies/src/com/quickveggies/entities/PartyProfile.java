package com.quickveggies.entities;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class PartyProfile {

    private String date;

    private String cases;

    private String amountPaid;

    private String amountReceived;

    private String description;

    private InputStream receipt;

    private Map<String, InputStream> descReceiptMap = new LinkedHashMap<>();

    public PartyProfile() {

    }

    public PartyProfile(String date, String cases, String amountPaid, String amountReceived,
            String desc) {
        this.date = date;
        this.cases = cases;
        this.amountPaid = amountPaid;
        this.amountReceived = amountReceived;
        this.description = desc;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCases() {
        return cases;
    }

    public Integer getCasesInt() {
        try {
            return cases == null ? 0 : Integer.valueOf(cases);
        }
        catch (NumberFormatException ex) {
            return 0;
        }
    }

    public void setCases(String cases) {
        this.cases = cases;
    }

    public String getAmountPaid() {
        return amountPaid;
    }

    public Integer getAmountPaidInt() {
        try {
            return amountPaid == null ? 0 : Integer.valueOf(amountPaid);
        }
        catch (NumberFormatException ex) {
            return 0;
        }
    }

    public void setAmountPaid(String amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getAmountReceived() {
        return amountReceived;
    }

    public Integer getAmountReceivedInt() {
        try {
            return amountReceived == null ? 0 : Integer.valueOf(amountReceived);
        }
        catch (NumberFormatException ex) {
            return 0;
        }
    }

    public void setAmountReceived(String amountReceived) {
        this.amountReceived = amountReceived;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public InputStream getReceipt() {
        return receipt;
    }

    public void setReceipt(InputStream receipt) {
        this.receipt = receipt;
    }

    public Map<String, InputStream> getDescReceiptMap() {
        if (descReceiptMap.isEmpty()) {
            descReceiptMap.put(description, receipt);
        }
        return descReceiptMap;
    }

    public void setDescReceiptMap(Map<String, InputStream> descReceiptMap) {
        this.descReceiptMap = descReceiptMap;
    }

    public boolean isTotalLine() {
        return false;
    }
}
