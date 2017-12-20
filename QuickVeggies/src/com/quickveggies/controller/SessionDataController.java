package com.quickveggies.controller;

import java.util.Map;

import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.User;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

public class SessionDataController {
    //constant parameters (indexes)

    public static final int AMANAT = 0, SIT = 1, FREIGHT = 2, LABOR = 3;
    private static Initializable linkedController = null;

    //GENERAL PURPOSE SECTION
    //-----------------------------------------
    private int godown = 0;
    private int coldstore = 0;

    private String amanatTotal = "";

    private int unsavedWindows = 0;

    private String newFruitName;

    public void linkController(Initializable controller) {
        linkedController = controller;
    }

    public int getGodown() {
        return godown;
    }

    public void setGodown(int godown) {
        this.godown = godown;
    }

    public int getColdstore() {
        return coldstore;
    }

    public void setColdstore(int coldstore) {
        this.coldstore = coldstore;
    }
    //------------------------------------------

    //LOGIN GLOBAL DATA
    //-------------------------------------------
    private User currentUser;

    private static SessionDataController instance = null;

    private final DatabaseClient dbc;
    
    private Pane settingsPagePane;

    protected SessionDataController() {
        charges = null;
        dbc = DatabaseClient.getInstance();
    }

    public static SessionDataController getInstance() {
        if (instance == null) {
            instance = new SessionDataController();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
    //-------------------------------------------
    //FRESH ENTRY CONSTANT COLUMNS

    //-------------------------------------------
    public String fruitType = "Mango";
    public String quality = "";
    public String boxSize = "";

    //-------------------------------------------
    //FRESH ENTRY CHARGES DATA
    //-------------------------------------------
    public String getFruitType() {
        return fruitType;
    }

    public void setFruitType(String fruitType) {
        this.fruitType = fruitType;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getBoxSize() {
        return boxSize;
    }

    public void setBoxSize(String boxSize) {
        this.boxSize = boxSize;
    }

    public Map<String, ChargeTypeValueMap> getCharges() {
        return charges;
    }

    public void setCharges(Map<String, ChargeTypeValueMap> charges2) {
        this.charges = charges2;
    }

    private int supplierGrossSum = 0;
    private int totalBoxes = 0;
    private Map<String, ChargeTypeValueMap> charges = null;

    public int getSupplierGrossSum() {
        return supplierGrossSum;
    }

    public void setSupplierGrossSum(int supplierGrossSum) {
        this.supplierGrossSum = supplierGrossSum;
    }

    public void freshEntryUpdateAmountCalc() {
        ((FreshEntryController) linkedController).updateAmountCalc();
    }

    public int getTotalBoxes() {
        return totalBoxes;
    }

    public void setTotalBoxes(int totalBoxes) {
        this.totalBoxes = totalBoxes;
    }

    public int getUnsavedWindows() {
        return unsavedWindows;
    }

    public void setUnsavedWindows(int unsavedWindows) {
        this.unsavedWindows = unsavedWindows;
    }

    /**
     * @return the amanatTotal
     */
    public String getAmanatTotal() {
        return amanatTotal;
    }

    /**
     * @param amanatTotal the amanatTotal to set
     */
    public void setAmanatTotal(String amanatTotal) {
        this.amanatTotal = amanatTotal;
    }

    /**
     * @return the newFruitName
     */
    public String getNewFruitName() {
        return newFruitName;
    }

    /**
     * @param newFruitName the newFruitName to set
     */
    public void setNewFruitName(String newFruitName) {
        this.newFruitName = newFruitName;
    }

    public Pane getSettingPagePane() {
        return settingsPagePane;
    }

    public void setSettingPagePane(Pane settingPagePane) {
        this.settingsPagePane = settingPagePane;
    }

    public Property<String> pendingLadaanEntriesProp = new SimpleStringProperty("");

    public Property<String> pendingGodownEntriesProp = new SimpleStringProperty("");

    public Property<String> pendingColdStoreEntriesProp = new SimpleStringProperty("");

    public void resetPendingLadaanEntries() {
        int count = dbc.getNonEditedLadaanEntries();
        pendingLadaanEntriesProp.setValue(String.valueOf(count));
    }

    public void resetPendingColdStoreEntries() {
        int coldStorePending = dbc.getStorageDealsCount("Cold Store");
        pendingColdStoreEntriesProp.setValue(String.valueOf(coldStorePending));
    }

    public void resetPendingGodownEntries() {
        int godownPending = dbc.getStorageDealsCount("Godown");
        pendingGodownEntriesProp.setValue(String.valueOf(godownPending));
    }

    //-------------------------------------------
    //EDIT ENTRY DATA
    //-----------------------------------------
    //public final static String[] dBuyerTableSqlColNames=new String[]{"buyerTitle","dealDate","buyerRate","buyerPay","boxes"};
    public final static String[] dBuyerTableSqlColNames = new String[]{"dealDate", "dealID", "buyerRate", "boxes", "aggregatedAmount"};
    public final static String[] dSupplierTableSqlColNames = new String[]{"supplierTitle", "date", "supplierRate", "net", "cases", "agent"};

    //bank accounts
    //------------------------------------------
    public final static String[] accountXlsTemplateHeaders = new String[]{
        "Transaction Id", "Date", "Cheque No.", "Comment", "Withdrawal", "Deposit", "Balance"
    };
}
