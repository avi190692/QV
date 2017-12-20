package com.quickveggies.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

import com.quickveggies.GeneralMethods;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Account;
import com.quickveggies.entities.AccountEntryLine;
import com.quickveggies.entities.Template;
import com.quickveggies.misc.Utils;
import com.quickveggies.misc.XlsTableReader;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class UploadAccountActivityController implements Initializable {

    @FXML
    private TextField filePathField;
    @FXML
    private Button browseButton;
    @FXML
    private ChoiceBox<String> chooseAccount;
    @FXML
    private TextField templateName;
    @FXML
    private Button addTemplate;
    @FXML
    private Button removeTemplate;
    @FXML
    private Button preview;
    @FXML
    private CheckBox duplicateChkbox;
    @FXML
    private CheckBox skipChkbox;
    @FXML
    private CheckBox replaceChkbox;
    @FXML
    private Button create;
    @FXML
    private ComboBox<String> cboTransColType;
    @FXML
    private Pane paneColSelection;
    @FXML
    private TextField txtCrDrColumn;
    @FXML
    private TextField txtTransactionColumn;
    @FXML
    private TextField txtCrAbb;
    @FXML
    private TextField txtDrAbb;
    @FXML
    private TextField headerRowNumberText;

    private static final String COL_TYPE_SINGLE = "One Column (CR/DR)";
    private static final String COL_TYPE_DOUBLE = "Two Column (Withdrawal/Deposit)";

    private DatabaseClient dbclient = DatabaseClient.getInstance();
    private XlsTableReader xlsreader;
    private TableView previewTable = null;
    private Integer headerRowNumber = 0;
    private final String[][] addedEntriesRawData = null;
    private final ArrayList<AccountEntryLine> accountEntries = new ArrayList<>();
    private String[][] filteredData = null;

    private static final Map<String, Integer> currSheetColHeadIndices = new LinkedHashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cboTransColType.setItems(FXCollections.observableArrayList(new String[]{COL_TYPE_SINGLE, COL_TYPE_DOUBLE}));
        cboTransColType.getSelectionModel().select(0);
        filePathField.setEditable(false);
        preview.setDisable(true);
        addTemplate.setDisable(true);
        chooseAccount.setDisable(true);
        templateName.setEditable(false);
        removeTemplate.setDisable(true);
        removeTemplate.setVisible(false);

        chooseAccount.setItems(FXCollections.observableArrayList(getAccountNames()));

        addTemplate.setOnAction((ActionEvent event) -> {
            if (chooseAccount.getValue() == null) {
                GeneralMethods.errorMsg("Choose account first!");
                return;
            }
            
            final Stage stage = new Stage();
            stage.centerOnScreen();
            stage.setTitle("Add/Change Template");
            stage.initModality(Modality.APPLICATION_MODAL);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/save_template.fxml"));
                SaveTemplateController controller = new SaveTemplateController(chooseAccount.getValue(),
                        xlsreader.getDataAsTableView(), removeTemplate, addTemplate, templateName, preview);
                loader.setController(controller);
                Parent parent = loader.load();
                Scene scene = new Scene(parent);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        // updateTemplatesList();
        browseButton.setOnAction((ActionEvent event) -> {
            boolean isSingleColSheet = false;
            if (cboTransColType.getValue().equals(COL_TYPE_SINGLE)) {
                isSingleColSheet = true;
                xlsreader = new XlsTableReader(isSingleColSheet);
                String crDrColName = txtCrDrColumn.getText();
                if (crDrColName == null || crDrColName.trim().isEmpty()) {
                    GeneralMethods.errorMsg("Please provide the name for CR/DR column name");
                    return;
                }
                String transAmtColName = txtTransactionColumn.getText();
                if (transAmtColName == null || transAmtColName.trim().isEmpty()) {
                    GeneralMethods.errorMsg("Please provide the name for transaction amount column name");
                    return;
                }
                String crAbbreviation = txtCrAbb.getText();
                if (crAbbreviation == null || crAbbreviation.trim().isEmpty()) {
                    GeneralMethods.errorMsg("Please provide the value for Credit abbreviation");
                    return;
                }
                String drAbbreviation = txtDrAbb.getText();
                if (drAbbreviation == null || drAbbreviation.trim().isEmpty()) {
                    GeneralMethods.errorMsg("Please provide the value for Debit abbreviation");
                    return;
                }
                xlsreader.setCR(crAbbreviation);
                xlsreader.setDR(drAbbreviation);
                xlsreader.setCrDrColName(crDrColName);
                xlsreader.setTransAmtColName(transAmtColName);
                
            } else {
                xlsreader = new XlsTableReader(false);
            }
            xlsreader.setHeaderRowNumber(headerRowNumber);
            xlsreader.browseXls();
            filePathField.setText(xlsreader.getParsedFilePath());
            if (filePathField.getText() != null) {
                chooseAccount.setDisable(false);
                templateName.setText(null);
                cboTransColType.setDisable(false);
            }
        });
        cboTransColType.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.equals(COL_TYPE_SINGLE)) {
                paneColSelection.setDisable(false);
            } else {
                paneColSelection.setDisable(true);
            }
        });
        // chooseTemplate.setOnAction(new EventHandler<ActionEvent>() {
        // public void handle(ActionEvent event) {
        // if(chooseTemplate.getValue()!=null)preview.setDisable(false);
        // }
        // });
        chooseAccount.setOnAction((ActionEvent event) -> {
            if (chooseAccount.getValue() == null) {
                return;
            }
            // check if there is already a template associated with this
            // account
            try {
                Template template = dbclient.getTemplate(chooseAccount.getValue());
                templateName.setText(template.getAccountName());
            } catch (SQLException e) {
                System.out.println("sqlexception when checking existing template");
            } catch (NoSuchElementException ex) {
                preview.setDisable(true);
                addTemplate.setDisable(false);
                return;
            }
            // if such template exists, allow the user to remove it, or
            // preview
            preview.setDisable(false);
            addTemplate.setDisable(true);
            addTemplate.setVisible(false);
            removeTemplate.setVisible(true);
            removeTemplate.setDisable(false);
        });

        removeTemplate.setOnAction((ActionEvent event) -> {
            dbclient.deleteTableEntries("templates", "accountName", chooseAccount.getValue(), false);
            removeTemplate.setVisible(false);
            removeTemplate.setDisable(true);
            addTemplate.setDisable(false);
            addTemplate.setVisible(true);
            templateName.setText(null);
            preview.setDisable(true);
            // updateTemplatesList();
        });

        preview.setOnAction((ActionEvent event) -> {
            // apply the template to the uploaded xls and display in a
            // seperate window
            try {
                
                filteredData = applyTemplateToXls(dbclient.getTemplate(chooseAccount.getValue()),
                        xlsreader.getDataAs2dArray(), SessionDataController.accountXlsTemplateHeaders);
                previewTable = xlsreader.arrayToTableView(filteredData,
                        Arrays.asList(SessionDataController.accountXlsTemplateHeaders), null);
                
                // open the preview window
                final Stage prevWindow = new Stage();
                prevWindow.centerOnScreen();
                prevWindow.setTitle("Preview Template");
                prevWindow.initModality(Modality.APPLICATION_MODAL);
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/preview_template.fxml"));
                    PreviewAccTableController controller = new PreviewAccTableController(previewTable);
                    loader.setController(controller);
                    Parent parent = loader.load();
                    Scene scene = new Scene(parent);
                    scene.setOnKeyPressed((KeyEvent event1) -> {
                        if (event1.getCode() == KeyCode.ESCAPE) {
                            prevWindow.close();
                        }
                    });
                    prevWindow.setScene(scene);
                    prevWindow.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (SQLException e) {
                System.out.println("sqlexception while making teableview for preview");
                e.printStackTrace();
            }
        });
        // upload the transactions table to sql
        create.setOnAction((ActionEvent event) -> {
            // filter the columns according to the ones assigned by the user
            try {
                
                filteredData = applyTemplateToXls(dbclient.getTemplate(chooseAccount.getValue()),
                        xlsreader.getDataAs2dArray(), SessionDataController.accountXlsTemplateHeaders);
                if (isAlreadyUploaded(filteredData)) {
                    GeneralMethods.errorMsg("This sheet has already been uploaded");
                    return;
                }
                // save each line as a new entity in the database table
                AccountEntryLine entryline;
                
                for (int rowInd = 1; rowInd < filteredData.length; rowInd++) {
                    String acName = chooseAccount.getValue();
                    String transIdCol = filteredData[rowInd][0];
                    String dateCol = filteredData[rowInd][1];
                    String chqNoCol = filteredData[rowInd][2];
                    String descCol = filteredData[rowInd][3];
                    Double withdrawalCol = Double.parseDouble(filteredData[rowInd][4]);
                    Double depositCol = Double.parseDouble(filteredData[rowInd][5]);
                    Double balanceCol = Double.parseDouble(filteredData[rowInd][6]);
                    
                    entryline = new AccountEntryLine(acName, transIdCol,
                            dateCol, chqNoCol, descCol,
                            withdrawalCol, depositCol, balanceCol, AccountEntryLine.DEFAULT, "", "", "");
                    dbclient.saveAccountEntryLine(entryline);
                }
                Account account = dbclient.getAccountByName(chooseAccount.getValue());
                int currentTime = (int) (System.currentTimeMillis() / (1000 * 3600 * 24));
//                dbclient.updateTableEntry("accounts", account.getId(), new String[]{"lastupdated"},
//                        new String[]{"" + currentTime}, false);
                account.setLastupdated(currentTime);
                dbclient.updateAccount(account);
                
                create.getScene().getWindow().hide();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        headerRowNumberText.textProperty().addListener(
                (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            try {
                headerRowNumber = Integer.valueOf(newValue);
            }
            catch (NumberFormatException ex) {
                headerRowNumber = 0;
            }
        });
    } // end of initialize()

    public static boolean isAlreadyUploaded(String[][] xtractedTableData) {
        DatabaseClient dbc = DatabaseClient.getInstance();
        int maxSize = xtractedTableData.length > 5 ? 5 : xtractedTableData.length;
        int count = 0;
        int dateColIdx = currSheetColHeadIndices.get("Date");
        int commentColIdx = currSheetColHeadIndices.get("Comment");
        int withdrawalColIdx = currSheetColHeadIndices.get("Withdrawal");
        int depositColIdx = currSheetColHeadIndices.get("Deposit");
        for (int rowInd = 1; rowInd <= maxSize; rowInd++) {
            boolean hasRecord = dbc.hasAccountEntry(xtractedTableData[rowInd][dateColIdx], Utils.toDbl(xtractedTableData[rowInd][withdrawalColIdx]),
                    Utils.toDbl(xtractedTableData[rowInd][depositColIdx]), xtractedTableData[rowInd][commentColIdx]);
            if (hasRecord) {
                count++;
            }
        }
        if (count >= 5) {
            return true;
        } else {
            return false;
        }

    }

    private String[] getAccountNames() {
        DatabaseClient dbclient = DatabaseClient.getInstance();
        int accountsNum = dbclient.getRowsNum("accounts");
        String[] result = new String[accountsNum];
        try {
            for (int i = 0; i < accountsNum; i++) {
                Account acc = dbclient.getAccountById(i + 1);
                result[i] = acc.getAccountName();
            }
        } catch (SQLException e) {
            System.out.println("sqlexception in getAccountNamesList");
            e.printStackTrace();
        }
        return result;
    }

    private static String[][] applyTemplateToXls(Template template, String[][] rawData, String[] headers) {
        String[] defColumns = SessionDataController.accountXlsTemplateHeaders;
        String[][] result = new String[rawData.length][defColumns.length];
        int[] rawColsIndArray = template.getColsIndexesArray();
        for (int colInd = 0; colInd < defColumns.length; colInd++) { // columns
            String currColumn = defColumns[colInd];
            currSheetColHeadIndices.put(currColumn, colInd);
            //	System.out.println("Current column:" + currColumn);
            for (int rowInd = 1; rowInd < rawData.length; rowInd++) {// rows.
                switch (currColumn) {
                    case "Withdrawal":
                    case "Deposit":
                    case "Balance": {
                        if (rawColsIndArray[colInd] == -1) {
                            result[rowInd][colInd] = "0";
                        } else {
                            String tmpVal = rawData[rowInd][rawColsIndArray[colInd]];
                            try {
                                tmpVal = Utils.extractNumberFromString(tmpVal).toString();
                            } catch (Exception x) {
                                GeneralMethods.errorMsg("Invalid number value found in the excel sheet");
                                return null;
                            }
                            result[rowInd][colInd] = Utils.extractNumberFromString(tmpVal).toString();
                        }
                        break;
                    }
                    default:
                        if (rawColsIndArray[colInd] == -1) {
                            result[rowInd][colInd] = "";
                        } else {
                            result[rowInd][colInd] = rawData[rowInd][rawColsIndArray[colInd]];
                        }
                        break;

                }
                // copy from the uploaded xls only the assigned columns in the
                // template
            }
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(Utils.extractNumberFromString("  	1,22,33.99# Cr. "));
        XlsTableReader xtr = new XlsTableReader(false);
        // xtr.processXlsFile(new
        // File("C:/Users/Shoeb/Documents/0618XXXXXXXXX123416-10-2017.xls"));;
        // xtr.processXlsFile(new File("C:/Users/Shoeb/Documents/demo.xlsx"));;
        xtr.processXlsFile(new File("C:/Users/Shoeb/Documents/demo-2-col.xlsx"));
        ;

        // TableView<String[]> tableView = xtr.getDataAsTableView();
        System.out.println(xtr.getColumnHeaders());

        String[][] data = xtr.getDataAs2dArray();
        //Template t = new Template(null, -1, 0, 1, 5, 2, 3, 4);
        Template t = new Template(null, -1, 0, 1, 5, 2, 3, 4);
        String[][] result = applyTemplateToXls(t, data, xtr.getColumnHeaders().toArray(new String[]{}));
        isAlreadyUploaded(result);
        for (int i = 1; i < result.length; i++) {
            System.out.println(Arrays.toString(result[i]));
        }
    }

}
