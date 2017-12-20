package com.quickveggies.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.TreeSet;

import com.ai.util.dates.DateUtil;
import com.quickveggies.GeneralMethods;
import com.quickveggies.PaymentMethodSource;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.MoneyPaidRecd;
import com.quickveggies.misc.AutoCompleteTextField;
import com.quickveggies.misc.SearchPartyButton;
import com.quickveggies.model.EntityType;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import com.quickveggies.model.DaoGeneratedKey;

public class MoneyPaidRecdController implements Initializable, DaoGeneratedKey {

    public enum AmountType {
        PAID, RECEIVED;
    }

    private static final String STR_ADD_NEW = "Add new...";

    @FXML
    private Label lblTitle;

    @FXML
    private Label lblPartyType;

    @FXML
    private AutoCompleteTextField txtParty;

    @FXML
    private TextField txtAmount;

    @FXML
    private ComboBox<String> cboPaymentType;

    @FXML
    private Button btnSave;

    @FXML
    private SearchPartyButton btnSearchParty;

    private static final String EMPTY_STR = "";

    private EntityType partType;

    private AmountType amountType;

    private TreeSet<String> partyList;

    @FXML
    private DatePicker dpDate;

    @FXML
    private DatePicker dpDepositDate;

    @FXML
    private CheckBox chkAdvanced;

    @FXML
    private TextField txtChequeNo;

    @FXML
    private TextField txtBankName;

    @FXML
    private Button btnAddSnapshot;

    @FXML
    private Pane paneBankDetails;

    private InputStream receiptImageStream;

    private boolean isAdvanced = false;
    private Integer generatedKey = null;

    private final BooleanProperty advancedPayProperty = new SimpleBooleanProperty();

    public static final String PAID = "Paid to (Dr.)";

    public static final String RECEIVED = "Received from (Cr.)";

    private PaymentMethodSource defPayMethodSource;
    private String defaultAmt, defPartyTitle, defDate, chequeNo, bankName;

    public MoneyPaidRecdController(EntityType partyType, AmountType amountType) {
        this.partType = partyType;
        this.amountType = amountType;
        advancedPayProperty.set(false);
    }

    public MoneyPaidRecdController(EntityType partyType, AmountType amountType, boolean isAdvanced) {
        this.partType = partyType;
        this.amountType = amountType;
        this.isAdvanced = isAdvanced;
        advancedPayProperty.set(isAdvanced);
    }

    public MoneyPaidRecdController(EntityType partyType, AmountType amountType, boolean isAdvanced, String defaultAmt,
            PaymentMethodSource defPayMethodSource, String defPartyTitle, String defDate, String chequeNo, String bankName) {
        this.partType = partyType;
        this.amountType = amountType;
        this.isAdvanced = isAdvanced;
        advancedPayProperty.set(isAdvanced);
        this.defaultAmt = defaultAmt;
        this.defPayMethodSource = defPayMethodSource;
        this.defPartyTitle = defPartyTitle;
        this.defDate = defDate;
        this.chequeNo = chequeNo;
        this.bankName = bankName;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        generatedKey = null;
        LocalDate date = LocalDate.now();
        chkAdvanced.visibleProperty().bindBidirectional(advancedPayProperty);
        chkAdvanced.setSelected(isAdvanced);

        if (this.partType == null) {
            throw new IllegalStateException("Party type must be set before calling this method");
        }
        if (this.amountType == null) {
            throw new IllegalStateException("Amount type must be set before calling this method");
        }
        switch (amountType) {
            case PAID:
                lblTitle.setText("Money Paid");
                break;
            case RECEIVED:
                lblTitle.setText("Money Received");
                chkAdvanced.setVisible(false);
                break;
        }
        lblPartyType.setText(partType.getValue().concat(" Title"));
        dpDate.setValue(date);

        partyList = updatePartyList(partType);
        txtParty.setEntries(partyList);
        txtParty.setLinkedFieldsReturnType(AutoCompleteTextField.ENTRY_IND);
        switch (partType) {
            case BUYER:
            case LADAAN:
            case BIJAK:
                txtParty.linkToWindow(this, "/buyeradd.fxml", "Add new Buyer",
                        STR_ADD_NEW, new AddBuyerController());
                break;
            case SUPPLIER:
                txtParty.linkToWindow(this, "/supplieradd.fxml", "Add new supplier",
                        STR_ADD_NEW, new AddSupplierController());
        }
        txtParty.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue,
                    Boolean newValue) {
                if (txtParty.getMenu().isShowing()) {
                    txtParty.getMenu().hide();
                }
                if (newValue) {
                    partyList = updatePartyList(MoneyPaidRecdController.this.partType);
                    txtParty.setEntries(partyList);
                }
            }
        });
        txtParty.setPromptText("Enter ".concat(this.partType.getValue()).concat(" here"));
        cboPaymentType.setItems(FXCollections.observableArrayList(PaymentMethodSource.getValueList()));
        cboPaymentType.setValue(PaymentMethodSource.Cash.toString());
        cboPaymentType.setEditable(false);

        if (defPartyTitle != null && !defPartyTitle.trim().isEmpty()) {
            txtParty.setText(defPartyTitle);
            txtParty.setEditable(false);
        }

        if (defaultAmt != null && !defaultAmt.trim().isEmpty()) {
            txtAmount.setText(defaultAmt);
            txtAmount.setEditable(false);
        }

        DateTimeFormatter formatter = null;

        try {
            String format = DateUtil.determineDateFormat(defDate);
            formatter = DateTimeFormatter.ofPattern(format);
        } catch (Exception x) {
            x.printStackTrace();
        }
        if (defDate != null && formatter != null) {
            try {
                date = LocalDate.parse(defDate, formatter);
                dpDate.setValue(date);
                dpDate.setDisable(true);
            } catch (Exception x) {
                System.err.println("Incorrect date format " + x.getMessage());
            }
        }
        if (defPayMethodSource != null) {
            cboPaymentType.setValue(defPayMethodSource.toString());
            if (defPayMethodSource.equals(PaymentMethodSource.Bank)) {
                paneBankDetails.setVisible(true);
                paneBankDetails.setDisable(false);
                if (chequeNo != null) {
                    txtChequeNo.setText(chequeNo);
                    txtChequeNo.setDisable(true);
                }
                if (defDate != null) {
                    dpDepositDate.setValue(date);
                    dpDepositDate.setDisable(true);
                }
                if (bankName != null) {
                    txtBankName.setText(bankName);
                    dpDepositDate.setDisable(true);
                }
            }
        }
        cboPaymentType.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!newValue.equals(oldValue) && newValue.equalsIgnoreCase("Bank")) {
                paneBankDetails.setDisable(false);
            } else {
                paneBankDetails.setDisable(true);
            }
        });
        btnSave.setOnAction((ActionEvent event) -> {
            if (!areFieldsValid()) {
                return;
            }
            saveMprObject();
            btnSave.getScene().getWindow().hide();
        });
        txtAmount.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (txtAmount.isFocused() && txtParty.getText().trim().isEmpty()) {
                txtParty.requestFocus();
            }
        });
        txtParty.setOnAction((ActionEvent event) -> {
            String text = txtParty.getText().trim();
            if (partyList.isEmpty()) {
                // stop event propagation here
                event.consume();
                return;
            }
            if (!partyList.contains(text)) {
                GeneralMethods.errorMsg("Please enter or select the correct property from list");
                event.consume();
                return;
            }
            dpDate.requestFocus();
        });
        dpDate.focusedProperty().addListener(new MyChangeListener<>(dpDate));
        dpDepositDate.focusedProperty().addListener(new MyChangeListener<>(dpDepositDate));

        dpDate.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                if (dpDate.isShowing()) {
                    dpDate.hide();
                } else {
                    txtAmount.requestFocus();
                }
            }
        });
        txtAmount.setOnAction((ActionEvent event) -> {
            if (!isAmountValid()) {
                event.consume();
                return;
            }
            cboPaymentType.requestFocus();
        });
        cboPaymentType.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                if (cboPaymentType.getValue().isEmpty()) {
                    cboPaymentType.setValue(PaymentMethodSource.Cash.toString());
                }
                if (cboPaymentType.getValue().equals(PaymentMethodSource.Bank.toString())) {
                    txtChequeNo.requestFocus();
                } else {
                    btnSave.requestFocus();
                }
            }
        });
        txtChequeNo.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    dpDepositDate.requestFocus();
                }
            }
        });
        dpDepositDate.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    if (dpDepositDate.isShowing()) {
                        dpDepositDate.hide();
                    } else {
                        txtBankName.requestFocus();
                    }
                }
            }
        });
        txtBankName.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    btnAddSnapshot.requestFocus();
                }
            }
        });
        btnAddSnapshot.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                uploadImage();
            }
        });
    }
    
    @Override
    public Integer getGeneratedKey() {
        return generatedKey;
    }

    private boolean areFieldsValid() {
        if (!isAmountValid()) {
            txtAmount.requestFocus();
        }
        String partyName = txtParty.getText() == null ? EMPTY_STR : txtParty.getText().trim();
        if (partyName.isEmpty()) {
            GeneralMethods.errorMsg("Please enter a valid party");
            txtParty.requestFocus();
            return false;
        }
        return true;
    }

    private boolean isAmountValid() {
        String amt = ((txtAmount.getText() == null) || txtAmount.getText().trim().isEmpty()) ? "0"
                : txtAmount.getText().trim();
        try {
            Double.parseDouble(amt);
        } catch (NumberFormatException nfe) {
            GeneralMethods.errorMsg("Please enter a valid number in amount field");
            return false;
        }
        return true;
    }

    private java.util.TreeSet<String> updatePartyList(EntityType pType) {
        DatabaseClient dbclient = DatabaseClient.getInstance();
        int rowsNum = dbclient.getRowsNum(pType.getTableName());
        java.util.TreeSet<String> result = new java.util.TreeSet<String>();
        for (int partyId = 1; partyId <= rowsNum; partyId++) {
            try {
                String title = "";
                switch (pType) {
                    case BIJAK:
                    case BUYER:
                    case LADAAN:
                        title = dbclient.getBuyerById(partyId).getTitle();
                        break;
                    case SUPPLIER:
                        title = dbclient.getSupplierById(partyId).getTitle();
                }
                result.add(title);
            } catch (java.sql.SQLException e) {
                System.out.print("sqlexception in populating party list");
            }
        }
        result.add(STR_ADD_NEW);
        return result;
    }

    private String getNormalizedValue(String str) {
        if (str == null) {
            return "0";
        }
        try {
            return ((Integer) Double.valueOf(str).intValue()).toString();

        } catch (Exception ex) {
            return "0";
        }
    }

    private void uploadImage() {
        Stage mainStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select payment file");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        File file = fileChooser.showOpenDialog(mainStage);
        try {
            if (file != null) {
                btnAddSnapshot.setText("1 File attached");
                receiptImageStream = new BufferedInputStream(new FileInputStream(file));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveMprObject() {
        generatedKey = null;
        DatabaseClient dbc = DatabaseClient.getInstance();
        String amtPaid = getNormalizedValue(txtAmount.getText());
        String amtReceived = amtPaid;
        switch (amountType) {
            case PAID:
                amtReceived = "0";
                break;
            case RECEIVED:
                amtPaid = "0";
        }
        MoneyPaidRecd mpr = new MoneyPaidRecd();
        mpr.setDate(dpDate.getValue().toString());
        if (cboPaymentType.getValue().equalsIgnoreCase("bank")) {
            mpr.setBankName(txtBankName.getText());
            mpr.setChequeNo(txtChequeNo.getText());
            mpr.setDepositDate(dpDepositDate.getValue().toString());
        }
        mpr.setIsAdvanced(String.valueOf(chkAdvanced.isSelected()));
        mpr.setPaid(amtPaid);
        mpr.setPartyType(partType.getValue());
        mpr.setPaymentMode(cboPaymentType.getValue());
        mpr.setReceipt(receiptImageStream);
        mpr.setReceived(amtReceived);
        mpr.setTitle(txtParty.getText().trim());
        if ("bank".equalsIgnoreCase(mpr.getPaymentMode())) {
            generatedKey = dbc.addMoneyPaidRecdInfo(mpr, "Added bank account entry to party ledger");
        }
        else {
            generatedKey = dbc.addMoneyPaidRecdInfo(mpr);
        }
    }

    private class MyChangeListener<T> implements ChangeListener<Boolean> {

        private DatePicker dp;

        public MyChangeListener(DatePicker dp) {
            this.dp = dp;
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (dp.getValue() == null) {
                dp.setValue(LocalDate.now());
            }
            if (!dp.isShowing()) {
                dp.show();
            }
        }
    }
    
    public static String[][] buildTableView(MoneyPaidRecd line) {
        List<String[]> table = new ArrayList<>();
        table.add(new String[]{"DATE", "CASES", "DESCRIPTION", "AMOUNT DR.", "AMOUNT CR."});
        String cases = "---";
        String desc = line.getDescription() == null ? "" : line.getDescription();
        if (desc.isEmpty()) {
            if (!ignoreMoneyCash(line.getPaid())) {
                desc = MoneyPaidRecd.MONEY_PAID;
            }
            if (!ignoreMoneyCash(line.getReceived())) {
                desc = MoneyPaidRecd.MONEY_RECEIVED;
            }
        }
        if ("Bank".equalsIgnoreCase(line.getPaymentMode())) {
            desc += " (Bank)";
        }
        desc += " (" + line.getTitle() + ")";
        String[] dealLine = new String[]{line.getDate(), cases,
            desc, line.getPaid(), line.getReceived()};
        table.add(dealLine);
        String[][] invArr = new String[table.size()][5];
        for (int i = 0; i < table.size(); i++) {
            invArr[i] = table.get(i);
        }
        return invArr;
    }
    
    private static boolean ignoreMoneyCash(String value) {
        return (value == null || value.trim().isEmpty() || value.trim().equals("0"));
    }
}
