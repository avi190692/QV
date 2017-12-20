package com.quickveggies.controller;

import com.ai.util.dates.DateUtil;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Iterator;
import java.util.Objects;
import java.util.Date;

import com.ai_int.utils.PDFUtil;

import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Account;
import com.quickveggies.entities.AccountEntryLine;
import com.quickveggies.entities.AuditLog;
import com.quickveggies.entities.Buyer;
import com.quickveggies.entities.DBuyerTableLine;
import com.quickveggies.entities.DSalesTableLine;
import com.quickveggies.entities.DSupplierTableLine;
import com.quickveggies.entities.ExpenseInfo;
import com.quickveggies.entities.LadaanBijakSaleDeal;
import com.quickveggies.controller.dashboard.DSupplierController;
import com.quickveggies.controller.popup.AuditLogEntryPopupController;
import com.quickveggies.controller.dashboard.DashboardController;
import com.quickveggies.controller.dashboard.DSalesTransController;
import com.quickveggies.entities.Expenditure;
import com.quickveggies.entities.MoneyPaidRecd;
import com.quickveggies.entities.Supplier;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import static com.quickveggies.controller.dashboard.DBuyerController.buildBuyerInvoicePdf;

public class AuditLogController implements Initializable {

    @FXML
    private TableView<AuditLog> auditLogTable;
    
    private final DatabaseClient dbclient = DatabaseClient.getInstance();
    
    private final ObservableList<AuditLog> logLines;

    private static DatabaseClient dbc = DatabaseClient.getInstance();
    
    private static DateTimeFormatter localFormatter;

    public AuditLogController(DashboardController dash) {
        this.logLines = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logLines.clear();
        List<AuditLog> logs = dbc.getAuditRecords();
        logLines.addAll(logs);
        localFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd"); //HH:mm:ss
        //Lookup additional information
        for (AuditLog log : logLines) {
            String name = "";
            String date = "";
            String amount = "";
            Object entryObject = null;
            if (log.getEventObject() != null && log.getEventObjectId() != null) {
                try {
                    switch (log.getEventObject()) 
                    {
                        case "partyMoney": 
                        {
                            MoneyPaidRecd value = dbc.getMoneyPaidRecd(log.getEventObjectId());
                            if (value != null) 
                            {
                                entryObject = value;
                                name = value.getTitle();
                                date = value.getDate();
                                amount = String.valueOf(Math.abs(Integer.valueOf(value.getReceived())
                                        - Integer.valueOf(value.getPaid())));
                            }
                        break;
                        }
                        case "arrival": 
                        {
                            DSalesTableLine value = dbc.getSalesEntryLineFromSql(log.getEventObjectId());
                            if (value != null) 
                            {
                                entryObject = value;
                                name = value.getAgent();
                                date = value.getDate();
                                amount = value.getNet();
                            }
                        break;
                        }
                        case "storagebuyerdeals":
                        case "buyerDeals": 
                        {
                            DBuyerTableLine value = dbc.getBuyerDealEntry(log.getEventObjectId());
                            if (value != null) 
                            {
                                entryObject = value;
                                name = value.getBuyerTitle();
                                date = value.getDate();
                                amount = value.getAggregatedAmount();
                            }
                        break;
                        }
                        case "ladaanBijakSaleDeals": {
                            LadaanBijakSaleDeal value = dbc.getLadBijSaleDeal(log.getEventObjectId());
                            if (value != null) {
                                entryObject = value;
                                name = value.getSaleNo();
                                date = value.getDate();
                                amount = value.getAggregatedAmount();
                            }
                            break;
                        }
                        case "supplierDeals": 
                        {
                            DSupplierTableLine value = dbc.getSupplierDealEntry(log.getEventObjectId());
                            if (value!= null) 
                            {
                                entryObject = value;
                                name = value.getSupplierTitle();
                                date = value.getDate();
                                amount = value.getNet();
                            }
                        break;
                        }
                        case "accountEntries": {
                            AccountEntryLine value = dbc.getAccountEntryLine(log.getEventObjectId());
                            if (value != null) {
                                entryObject = value;
                                name = value.getPayee();
                                date = value.getDateCol();
                                amount = String.valueOf(value.getDepositCol() + value.getWithdrawalCol());
                            }
                            break;
                        }
                        case "accounts": 
                        {
                            Account value = dbc.getAccountById(log.getEventObjectId());
                            if (value != null) 
                            {
                                entryObject = value;
                                name = value.getAccountName();
                                amount = String.valueOf(value.getBalance());
                            }
                        break;
                        }
                        case "expenditures": 
                        {
                            Expenditure value = dbc.getExpenditureById(log.getEventObjectId());
                            if (value != null) 
                            {
                                entryObject = value;
                                name = value.getPayee();
                                date = value.getDate();
                                amount = String.valueOf(value.getAmount());
                            }
                        break;
                        }
                    }
                }
                catch (NoSuchElementException ex) {
                    Logger.getLogger(AuditLogController.class.getName()).log(Level.WARNING, ex.getMessage());
                }
                catch (SQLException ex) {
                    Logger.getLogger(AuditLogController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (entryObject != null) {
                log.setName(name);
                try {
                    String format = DateUtil.determineDateFormat(date);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                    log.setDate(Date.from(LocalDate.parse(date, formatter)
                            .atStartOfDay(ZoneId.systemDefault()).toInstant()));
                }
                catch (Exception x) {
                    log.setDate(null);
                    x.printStackTrace();
                }
                try {
                    log.setAmount(Double.parseDouble(amount));
                }
                catch (NumberFormatException x) {
                    log.setAmount(null);
                    x.printStackTrace();
                }
            }
            log.setEntryObject(entryObject);
        }
        auditLogTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("eventTime"));
        auditLogTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("userId"));
        auditLogTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("eventDetail"));
        ((TableColumn<AuditLog, String>) auditLogTable.getColumns().get(2)).setCellFactory(new CellCallback());
        auditLogTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("name"));
        auditLogTable.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("date"));
        ((TableColumn<AuditLog, String>) auditLogTable.getColumns().get(4)).setCellFactory(new Callback() {

            @Override
            public TableCell<AuditLog, Date> call(Object param) {
                final TableCell<AuditLog, Date> cell = new TableCell<AuditLog, Date>() {
                    
                    @Override
                    public void updateItem(Date item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty || item == null) {
                            setGraphic(null);
                            setText(null);
                        }
                        else {
                            setText(item.toInstant().atZone(ZoneId.systemDefault())
                                    .toLocalDateTime().format(localFormatter));
                        }
                    }
                };
                return cell;
            }
        });
        auditLogTable.getColumns().get(5).setCellValueFactory(new PropertyValueFactory<>("amount"));
        ((TableColumn<AuditLog, String>) auditLogTable.getColumns().get(6)).setCellFactory(new ViewCallback());
        auditLogTable.setItems(logLines);
    }
    
    private class ViewCallback implements Callback<TableColumn<AuditLog, String>, TableCell<AuditLog, String>> {

        @Override
        public TableCell<AuditLog, String> call(final TableColumn<AuditLog, String> param) {
            final TableCell<AuditLog, String> cell = new TableCell<AuditLog, String>() {

                final Button btn = new Button("View");

                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty) {
                        setGraphic(null);
                        setText(null);
                    }
                    else {
                        btn.setOnAction((ActionEvent event) -> {
                            AuditLog log = getTableView().getItems().get(getIndex());
                            if (log.getEntryObject() == null) {
                                return;
                            }
                            //Show the entity of transaction
                            switch (log.getEventObject()) {
                                case "partyMoney": {
                                    String pdf = prepareMoneyCashPdf((MoneyPaidRecd) log.getEntryObject());
                                    DashboardController.showPopup("/auditlogentrypopup.fxml", "Log Entry Preview",
                                            new AuditLogEntryPopupController(pdf, null){{
                                                setEventData(log.getDate().toInstant().atZone(
                                                        ZoneId.systemDefault()).toLocalDateTime().format(localFormatter));
                                                setDescription(log.getEventDetail());
                                            }});
                                    break;
                                }
                                case "arrival": {
                                    String pdf = prepareSalesDealPdf((DSalesTableLine) log.getEntryObject());
                                    DSalesTableLine oldLine = new DSalesTableLine();
                                    String pdfOldValues = null;
                                    if (oldLine.deserialize(log.getOldValues())) {
                                        pdfOldValues = prepareSalesDealPdf(oldLine);
                                    }
                                    DashboardController.showPopup("/auditlogentrypopup.fxml", "Log Entry Preview",
                                            new AuditLogEntryPopupController(pdf, pdfOldValues) {
                                                {
                                                    setEventData(log.getDate().toInstant().atZone(
                                                            ZoneId.systemDefault()).toLocalDateTime().format(localFormatter));
                                                    setDescription(log.getEventDetail());
                                                }
                                            });
                                    break;
                                }
                                case "storagebuyerdeals":
                                case "buyerDeals": {
                                    String pdf = prepareBuyerDealPdf((DBuyerTableLine) log.getEntryObject());
                                    DBuyerTableLine oldLine = new DBuyerTableLine(null);
                                    String pdfOldValues = null;
                                    if (oldLine.deserialize(log.getOldValues())) {
                                        pdfOldValues = prepareBuyerDealPdf(oldLine);
                                    }
                                    DashboardController.showPopup("/auditlogentrypopup.fxml", "Log Entry Preview",
                                            new AuditLogEntryPopupController(pdf, pdfOldValues){{
                                                setEventData(log.getDate().toInstant().atZone(
                                                        ZoneId.systemDefault()).toLocalDateTime().format(localFormatter));
                                                setDescription(log.getEventDetail());
                                            }});
                                    break;
                                }
                                case "ladaanBijakSaleDeals": {
                                    break;
                                }
                                case "supplierDeals": {
                                    String pdf = prepareSupplierDealPdf((DSupplierTableLine) log.getEntryObject());
                                    DSupplierTableLine oldLine = new DSupplierTableLine(null);
                                    String pdfOldValues = null;
                                    if (oldLine.deserialize(log.getOldValues())) {
                                        pdfOldValues = prepareSupplierDealPdf(oldLine);
                                    }
                                    DashboardController.showPopup("/auditlogentrypopup.fxml", "Log Entry Preview",
                                            new AuditLogEntryPopupController(pdf, pdfOldValues) {
                                                {
                                                    setEventData(log.getDate().toString());
                                                    setDescription(log.getEventDetail());
                                                }
                                            });
                                    break;
                                }
                                case "expenditures": {
                                    String pdf = prepareExpenditurePdf((Expenditure) log.getEntryObject());
                                    DashboardController.showPopup("/auditlogentrypopup.fxml", "Log Entry Preview",
                                            new AuditLogEntryPopupController(pdf, null){{
                                                setEventData(log.getDate().toString());
                                                setDescription(log.getEventDetail());
                                            }});
                                    break;
                                }
                                case "accountEntries": {
                                    break;
                                }
                                case "accounts": {
                                    break;
                                }
                            }
                        });
                        setGraphic(btn);
                        setText(null);
                    }
                }
            };
            return cell;
        }
    }
    
    private class CellCallback implements Callback<TableColumn<AuditLog, String>, TableCell<AuditLog, String>> {

        @Override
        public TableCell<AuditLog, String> call(TableColumn<AuditLog, String> p) {
            TableCell<AuditLog, String> cell = new TableCell<AuditLog, String>() {
                @Override 
                protected void updateItem(String item, boolean empty) {
                    // calling super here is very important - don't skip this!
                    super.updateItem(item, empty);
                    if(item != null) {
                        setText(item);
                        if (item.startsWith("ADDED")) {
                            setTextFill(Color.DARKGREEN);
                        }
                        else if (item.startsWith("DELETED")) {
                            setTextFill(Color.DARKRED);
                        }
                        else if (item.startsWith("UPDATED")) {
                            setTextFill(Color.CORAL);
                        }
                    }
                }
            };
            return cell;
        }
    }
    
    private String prepareBuyerDealPdf(DBuyerTableLine currDeal) {
        try {
            List<DBuyerTableLine> buyerDeals = dbclient.getBuyerDealEntries(currDeal.getBuyerTitle(), null);
            Integer cases = 0;
            DBuyerTableLine replace = null;
            for (DBuyerTableLine line : buyerDeals) {
                line.setAmountedTotal(line.getAggregatedAmount());
                cases += line.getCases();
                if (Objects.equals(currDeal.getSaleNo(), line.getSaleNo())) {
                    replace = line;
                }
            }
            Iterator<DBuyerTableLine> lines = buyerDeals.iterator();
            while (lines.hasNext()) {
                DBuyerTableLine deal = lines.next();
                if (!deal.getDealID().equals(currDeal.getDealID())) {
                    lines.remove();
                }
                else if (deal.getSaleNo().equals(currDeal.getSaleNo())) {
                    deal.deserialize(currDeal.serialize());
                }
            }
            if (replace != null) {
                buyerDeals.remove(replace);
                buyerDeals.add(currDeal.clone());
            }
            List<ExpenseInfo> buyerExpenses = dbclient.getBuyerExpenseInfoList();
            String[][] dataArr = buildBuyerInvoicePdf(currDeal, buyerDeals, buyerExpenses);
            Buyer buyer = dbclient.getBuyerByName(currDeal.getBuyerTitle());
            String buyerName = currDeal.getBuyerTitle();
            if (buyer != null) {
                buyerName = buyer.getFirstName() + " " + buyer.getLastName();
            }
            String pdfFile = PDFUtil.buildBuyerInvoicePdf(currDeal.getBuyerTitle(),
                    buyerName, currDeal.getDealID(), currDeal.getDate(), cases, dataArr);
            return pdfFile;
        }
        catch (SQLException | NoSuchElementException | CloneNotSupportedException ex) {
            Logger.getLogger(AuditLogController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private String prepareSupplierDealPdf(DSupplierTableLine currDeal) {
        try {
            String[][] dataArr = DSupplierController.buildInvTabForEmail(currDeal);
            Supplier supplier = dbclient.getSupplierByName(currDeal.getSupplierTitle());
            String supplierName = currDeal.getSupplierTitle();
            if (supplier != null) {
                supplierName = supplier.getFirstName() + " " + supplier.getLastName();
            }
            String pdfFile = PDFUtil.buildSupplierInvoicePdf(currDeal.getSupplierTitle(), supplierName, currDeal.getDealID(),
                    currDeal.getDate(), dataArr);
            return pdfFile;
        }
        catch (SQLException | NoSuchElementException ex) {
            Logger.getLogger(AuditLogController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private String prepareSalesDealPdf(DSalesTableLine currDeal) {
        try {
            String[][] dataArr = DSalesTransController.buildInvTabForEmail(currDeal);
            Supplier supplier = dbclient.getSupplierByName(currDeal.getSupplier());
            String supplierName = currDeal.getSupplier();
            if (supplier != null) {
                supplierName = supplier.getFirstName() + " " + supplier.getLastName();
            }
            String pdfFile = PDFUtil.buildSupplierInvoicePdf(currDeal.getSupplier(), supplierName, currDeal.getDealID(),
                    currDeal.getDate(), dataArr);
            return pdfFile;
        }
        catch (SQLException | NoSuchElementException ex) {
            Logger.getLogger(AuditLogController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private String prepareMoneyCashPdf(MoneyPaidRecd currDeal) {
        String[][] dataArr = MoneyPaidRecdController.buildTableView(currDeal);
        String pdfFile = PDFUtil.buildMailCashInvoicePdf(currDeal, dataArr);
        return pdfFile;
    }
    
    private String prepareExpenditurePdf(Expenditure currDeal) {
        String pdfFile = PDFUtil.buildExpenseInvoicePdf(currDeal);
        return pdfFile;
    }
}
