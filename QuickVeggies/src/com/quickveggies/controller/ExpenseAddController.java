package com.quickveggies.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.TreeSet;

import com.ai.util.dates.DateUtil;
import com.quickveggies.GeneralMethods;
import com.quickveggies.PaymentMethodSource;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Expenditure;
import com.quickveggies.entities.MoneyPaidRecd;
import com.quickveggies.misc.AutoCompleteTextField;
import com.quickveggies.misc.SearchPartyButton;
import com.quickveggies.misc.Utils;
import com.quickveggies.model.EntityType;
import com.quickveggies.entities.PartyType;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import com.quickveggies.model.DaoGeneratedKey;

public class ExpenseAddController implements Initializable, DaoGeneratedKey {

    @FXML
    private TextField amountField;

    @FXML
    private DatePicker dateField;

    @FXML
    private TextField commentField;

    @FXML
    private SearchPartyButton payeeSearchButton;

    @FXML
    private Button btnUpload;

    @FXML
    private ImageView imvExpense;

    @FXML
    private ComboBox<String> cboPaymentType;

    @FXML
    private DatePicker dpDepositDate;

    @FXML
    private TextField txtChequeNo;

    @FXML
    private TextField txtBankName;
    
    @FXML
    private Pane paneBankDetails;

    @FXML
    private AutoCompleteTextField searchExpenseType;
    
    @FXML
    private AutoCompleteTextField payeeField;

    @FXML
    private Button create;

    private File imgFile;
    
    private Integer generatedKey = null;
    
    private final PaymentMethodSource defPayMethodSource;
    
    private final DatabaseClient dbclient = DatabaseClient.getInstance();

    private String defaultAmt, defPartyTitle, expenseType, payeeType, defDate,
            defComment, chequeNo, bankName;

    public ExpenseAddController(String defaultAmt, String defPartyTitle,
            String expenseType, String payeeType, String defDate, String chequeNo,
            String bankName, String defComment, PaymentMethodSource paymentMethod) {
        this.defaultAmt = defaultAmt;
        this.defPartyTitle = defPartyTitle;
        this.expenseType = expenseType;
        this.payeeType = payeeType;
        this.defDate = defDate;
        this.defComment = defComment;
        this.chequeNo = chequeNo;
        this.bankName = bankName;
        this.defPayMethodSource = paymentMethod;
    }
    
    public ExpenseAddController() {
        this.defPayMethodSource = PaymentMethodSource.Cash;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        generatedKey = null;
        imvExpense.setFitWidth(((Pane) imvExpense.getParent()).getPrefWidth() - 5);
        imvExpense.setFitHeight(((Pane) imvExpense.getParent()).getPrefHeight() - 5);
        //
        if (defPartyTitle != null && !defPartyTitle.trim().isEmpty()) {
            payeeField.setText(defPartyTitle);
            payeeField.setEditable(false);
            if (payeeType != null) {
                payeeField.setUserData(EntityType.getEntityTypeForValue(payeeType));
            }
        }
        if (defaultAmt != null && !defaultAmt.trim().isEmpty()) {
            amountField.setText(String.valueOf(Double.valueOf(defaultAmt).intValue()));
            amountField.setEditable(false);
        }
        if (expenseType != null && !expenseType.trim().isEmpty()) {
            searchExpenseType.setText(expenseType);
            searchExpenseType.setEditable(false);
        }
        DateTimeFormatter formatter = null;

        try {
            String format = DateUtil.determineDateFormat(defDate);
            formatter = DateTimeFormatter.ofPattern(format);
        }
        catch (Exception x) {
            x.printStackTrace();
        }
        LocalDate date = null;
        if (defDate != null && formatter != null) {
            try {
                date = LocalDate.parse(defDate, formatter);
                dateField.setValue(date);
                dateField.setDisable(true);
            } catch (Exception x) {
                dateField.setValue(LocalDate.now());
                System.err.println("Incorrect date format " + x.getMessage());
            }

        }
        if (!Utils.isEmptyString(defComment)) {
            commentField.setText(defComment);
            commentField.setEditable(false);
        }
        cboPaymentType.setItems(FXCollections.observableArrayList(PaymentMethodSource.getValueList()));
        cboPaymentType.setValue(PaymentMethodSource.Cash.toString());
        cboPaymentType.setEditable(false);
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
                txtBankName.setDisable(true);
            }
        }
        cboPaymentType.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!newValue.equals(oldValue) && newValue.equalsIgnoreCase("Bank")) {
                paneBankDetails.setDisable(false);
            } else {
                paneBankDetails.setDisable(true);
            }
        });
        // Upload Image
        btnUpload.setOnAction((ActionEvent event) -> {
            FileChooser fc = new FileChooser();
            ExtensionFilter ef = new ExtensionFilter("Pictures",
                    new String[]{"*.png", "*.jpeg", "*.jpg", "*.bmp", "*.tif", "*.yuv", "*.psd"});
            fc.setTitle("Select receipt picture");
            fc.getExtensionFilters().add(ef);
            imgFile = fc.showOpenDialog(btnUpload.getScene().getWindow());
            if (imgFile == null) {
                return;
            }
            try {
                Image image = new Image(new BufferedInputStream(new FileInputStream(imgFile)));
                imvExpense.setImage(image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
        TreeSet<String> entriesList = new TreeSet<>(dbclient.getExpenditureTypeList());
        searchExpenseType.setEntries(entriesList);
        amountField.focusedProperty().addListener((ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) -> {
            if (aBoolean2) {
            } else if (!amountField.getText().equals("")) {
                try {
                    Integer.parseInt(amountField.getText());
                } catch (NumberFormatException e) {
                    GeneralMethods.errorMsg("Value must be an integer!");
                    amountField.setText("");
                }
            }
        });
        payeeSearchButton.setPartyType(PartyType.BUYER_SUPPLIERS);
        payeeSearchButton.setLinkedObject(payeeField);

        create.setOnAction((ActionEvent event) -> {
            if (!areFieldsValid()) {
                return;
            }
            saveExpenditure();
            create.getScene().getWindow().hide();
        });
    }

    private void saveExpenditure() {
        generatedKey = null;
        Expenditure xpr = new Expenditure();
        xpr.setAmount(amountField.getText());
        xpr.setDate(dateField.getValue().toString());
        xpr.setComment(commentField.getText());
        xpr.setPayee(payeeField.getText());
        xpr.setType(searchExpenseType.getText());
        if (imgFile != null && imgFile.exists()) {
            try {
                xpr.setReceipt(new FileInputStream(imgFile));
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        DatabaseClient dbc = DatabaseClient.getInstance();
        MoneyPaidRecd mpr = new MoneyPaidRecd();
        String partyType = ((EntityType) payeeField.getUserData()).getValue();
        mpr.setDate(dateField.getValue().toString());
        mpr.setIsAdvanced(Boolean.FALSE.toString());
        mpr.setPartyType(partyType);
        mpr.setPaymentMode(PaymentMethodSource.Bank.toString());
        mpr.setReceived(amountField.getText().trim());
        mpr.setPaid("0");
        mpr.setTitle(payeeField.getText().trim());
        mpr.setDescription(searchExpenseType.getText());
        String title = "Expense entry recorded in supplier ledger";
        if (cboPaymentType.getValue().equalsIgnoreCase("bank")) {
            mpr.setBankName(txtBankName.getText());
            mpr.setChequeNo(txtChequeNo.getText());
            mpr.setDepositDate(dpDepositDate.getValue().toString());
            title = "Assigned expense entry from bank account";
        }
        mpr.setPaymentMode(cboPaymentType.getValue());
        // save transaction
        if (dbc.addExpenditure(xpr)) {
            generatedKey = dbc.addMoneyPaidRecdInfo(mpr, title);
        }
    }

    private boolean areFieldsValid() {
        boolean result = true;
        try {
            Double.valueOf(amountField.getText());
        } catch (Exception x) {
            GeneralMethods.errorMsg("Please enter correct number in amount field");
            result = false;
        }
        if (payeeField.getText().trim().isEmpty()) {
            GeneralMethods.errorMsg("Please enter payee info");
            result = false;
        }
        if (searchExpenseType.getText().trim().isEmpty()) {
            GeneralMethods.errorMsg("Please enter expense type info");
            result = false;
        }
        return result;
    }

    @Override
    public Integer getGeneratedKey() {
        return generatedKey;
    }
}
