package com.quickveggies.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.imageio.ImageIO;

import com.ai.util.dates.DateUtil;
import com.quickveggies.GeneralMethods;
import com.quickveggies.Main;
import com.quickveggies.UserGlobalParameters;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Buyer;
import com.quickveggies.entities.DBuyerTableLine;
import com.quickveggies.entities.DSupplierTableLine;
import com.quickveggies.entities.Expenditure;
import com.quickveggies.entities.LadaanBijakSaleDeal;
import com.quickveggies.entities.MoneyPaidRecd;
import com.quickveggies.entities.PartyProfile;
import com.quickveggies.entities.PartyProfileList;
import com.quickveggies.entities.Supplier;
import com.quickveggies.misc.MailButton;
import com.quickveggies.misc.Utils;
import com.quickveggies.model.EntityType;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

public class ProfileViewController implements Initializable {

    private enum ObjectType {
        BUYER, LADAAN, MPR, SUPPLIER, EXPENDITURE;
    }

    @FXML
    private TableView<PartyProfile> dealAndPaysTable;
    @FXML
    private TableView<PartyProfile> tableTotal;
    @FXML
    private Label nameLabel;
    @FXML
    private Label companyLabel;
    @FXML
    private Label cityLabel;
    @FXML
    private Label mobileLabel;
    @FXML
    private Pane mainpane;
    @FXML
    private Text balanceAmt;
    @FXML
    private Label titleLabel;
    @FXML
    private Button cancel;
    @FXML
    private Button edit;
    @FXML
    private Button delete;
    @FXML
    private Label lblPaymentType;
    @FXML
    private Label lblCreditLimit;
    @FXML
    private Label lblAccountType;
    @FXML
    private Label paymentType;
    @FXML
    private Label creditLimit;
    @FXML
    private Pane paneParty;
    @FXML
    private Pane paneExpenditure;
    @FXML
    private Label lblExpAccountName;
    @FXML
    private ImageView imvParty;

    private Buyer buyer = null;

    private String expenditure;

    private Supplier supplier = null;

    private List<LadaanBijakSaleDeal> ladaanDealsList;

    private StringProperty allDealsAmt = new SimpleStringProperty("0");

    private DatabaseClient dbclient = DatabaseClient.getInstance();

    private ObservableList<DBuyerTableLine> buyerlines = FXCollections
            .observableArrayList(new DBuyerTableLine("", "", "", "", "", "", "", "", "", "", "", ""));

    private ObservableList<DSupplierTableLine> supplierlines = FXCollections
            .observableArrayList(new DSupplierTableLine("", "", "", "", "", "", "", "", "", "", "", "", "", ""));

    private ObservableList<Expenditure> expenditureLines = FXCollections.observableArrayList();

    private ObservableList<MoneyPaidRecd> mprList = FXCollections.observableArrayList();

    private EntityType partyType;

    private MailButton mailButton = null;

    private DateFormat dateFormat;

    private Set<String> saleDealIds = new LinkedHashSet<>();

    public ProfileViewController(String entityTitle, EntityType entityType) {
        // System.out.println(personType);
        switch (entityType) {
            case SUPPLIER:
                try {
                    supplier = dbclient.getSupplierByName(entityTitle);
                } catch (NoSuchElementException | SQLException e) {
                    GeneralMethods.errorMsg("error getting supplier info");
                    e.printStackTrace();
                    return;
                }
                mailButton = new MailButton(supplier.getEmail(), UserGlobalParameters.userEmail);
                partyType = EntityType.SUPPLIER;
                break;
            case BUYER:
                try {
                    buyer = dbclient.getBuyerByName(entityTitle);
                } catch (NoSuchElementException | SQLException e) {
                    GeneralMethods.errorMsg("error getting buyer info");
                    e.printStackTrace();
                    return;
                }
                mailButton = new MailButton(buyer.getEmail(), UserGlobalParameters.userEmail);
                switch (buyer.getBuyerType().toLowerCase()) {
                    case "regular":
                        partyType = EntityType.BUYER;
                        break;
                    case "ladaan":
                        partyType = EntityType.LADAAN;
                        break;
                    case "bijak":
                        partyType = EntityType.BIJAK;
                }
                break;

            case EXPENDITURE:
                expenditure = entityTitle;
                partyType = EntityType.EXPENDITURE;
                break;

            case UNIVERSAL_BANK:
            case UNIVERSAL_CASH:
                expenditure = entityTitle;
                partyType = entityType;
                break;
            
            default:
                throw new IllegalArgumentException("Invalid entity type encountered");
        }
    }

    public void initialize(URL location, ResourceBundle resources) {
        dealAndPaysTable.getColumns().clear();
        balanceAmt.textProperty().bindBidirectional(allDealsAmt);

        try{
        // initialize buttons
        if (partyType == EntityType.EXPENDITURE) {
            paneParty.setVisible(false);
            paneExpenditure.setVisible(true);
            String text = lblExpAccountName.getText();
            text = text.concat(" ".concat(expenditure));
            lblExpAccountName.setText(text);

        }
        else if (partyType == EntityType.UNIVERSAL_CASH 
                || partyType == EntityType.UNIVERSAL_BANK) {
            paneParty.setVisible(false);
            paneExpenditure.setVisible(true);
            String text = lblExpAccountName.getText();
            text = text.concat(" ".concat(expenditure));
            lblExpAccountName.setText(text);
            lblAccountType.setText("Account Type: Universal");
        } else {
            paneParty.setVisible(true);
            paneExpenditure.setVisible(false);
            mailButton.setLayoutX(500.0);
            mailButton.setLayoutY(235.0);
            mailButton.setVisible(true);
            mainpane.getChildren().add(mailButton);
            Image image = null;
            if (buyer != null) {
                nameLabel.setText(buyer.getFirstName() + " " + buyer.getLastName());
                companyLabel.setText(buyer.getCompany());
                cityLabel.setText(buyer.getCity());
                mobileLabel.setText(buyer.getMobile());
                titleLabel.setText(buyer.getTitle());
                paymentType.setText(UserGlobalParameters.getPaymentMethodMap().get(buyer.getPaymentMethod()).toString());
                creditLimit.setText(buyer.getCreditPeriod());
                if (buyer.getImageStream() != null) {
                    image = new Image(buyer.getImageStream());
                }

            } else if (supplier != null) {
                nameLabel.setText(supplier.getFirstName() + " " + supplier.getLastName());
                companyLabel.setText(supplier.getCompany());
                cityLabel.setText(supplier.getVillage());
                mobileLabel.setText(supplier.getMobile());
                titleLabel.setText(supplier.getTitle());
                lblPaymentType.setVisible(false);
                lblCreditLimit.setVisible(false);
                if (supplier.getImagePath() != null) {
                	BufferedImage imageBuffer = ImageIO.read(new File(supplier.getImagePath()));
                    image = SwingFXUtils.toFXImage(imageBuffer, null);
                }
            }
            if (image != null) {
                imvParty.setImage(image);
            }

        }} catch(Exception e){
        	
        }
        cancel.setOnAction((ActionEvent event) -> {
            cancel.getScene().getWindow().hide();
        });
        mprList.clear();
        
        switch (partyType) {
            case BIJAK:
            case LADAAN:
                prepareNonRegBuyerList();
                break;
                
            case BUYER:
                prepareBuyerList(buyer.getTitle());
                break;
                
            case SUPPLIER:
                prepareSupplierList(supplier.getTitle());
                break;
                
            case EXPENDITURE:
                prepareExpenditureList();
                break;
                
            case UNIVERSAL_BANK:
            case UNIVERSAL_CASH:
                prepareUniversalList();
                mprList.addAll(dbclient.getMoneyPaidRecdList(null, EntityType.BUYER));
                mprList.addAll(dbclient.getMoneyPaidRecdList(null, EntityType.SUPPLIER));
                break;
                
            default:
                break;
        }
        mprList.addAll(dbclient.getMoneyPaidRecdList(titleLabel.getText(), partyType));

        List<Date> sortedDates = new ArrayList<>();
        ObservableList<PartyProfile> profList = FXCollections.observableArrayList();
        switch (partyType) {
            case LADAAN:
            case BIJAK:
                sortedDates.addAll(convertToUtilDates(ladaanDealsList, ObjectType.LADAAN));
                sortedDates.addAll(convertToUtilDates(buyerlines, ObjectType.BUYER));
                break;

            case BUYER:
                sortedDates.addAll(convertToUtilDates(buyerlines, ObjectType.BUYER));
                break;

            case SUPPLIER:
                sortedDates.addAll(convertToUtilDates(supplierlines, ObjectType.SUPPLIER));
                break;

            case EXPENDITURE:
                sortedDates.addAll(convertToUtilDates(expenditureLines, ObjectType.EXPENDITURE));
                break;
            
            case UNIVERSAL_BANK:
            case UNIVERSAL_CASH:
                sortedDates.addAll(convertToUtilDates(buyerlines, ObjectType.BUYER));
                sortedDates.addAll(convertToUtilDates(supplierlines, ObjectType.SUPPLIER));
                break;

            default:
                break;
        }
        sortedDates.addAll(convertToUtilDates(mprList, ObjectType.MPR));
        // System.out.println("dates after:" + sortedDates);
        List<String> strDates = convertToSortedStringDates(sortedDates);
        profList.addAll(prepareProfileList(mprList, strDates));
        populateProfileTable(profList);
        calculateBal(profList);

        edit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                if (buyer != null) {
                    /* open EditBuyerController window */
                    final Stage addTransaction = new Stage();
                    addTransaction.centerOnScreen();
                    addTransaction.setTitle("Edit Buyer Profile");
                    addTransaction.initModality(Modality.APPLICATION_MODAL);
                    addTransaction.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        public void handle(WindowEvent event) {
                            Main.getStage().getScene().getRoot().setEffect(null);
                        }
                    });
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/buyeredit.fxml"));
                        EditBuyerController controller = new EditBuyerController(buyer.getTitle());
                        loader.setController(controller);
                        Parent parent = loader.load();
                        Scene scene = new Scene(parent, 707, 500);
                        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
                            public void handle(KeyEvent event) {
                                if (event.getCode() == KeyCode.ESCAPE) {
                                    Main.getStage().getScene().getRoot().setEffect(null);
                                    addTransaction.close();
                                }
                            }
                        });
                        addTransaction.setScene(scene);
                        addTransaction.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else if (supplier != null) {
                    /*
					 * open EditSupplierController window
                     */
                    final Stage addTransaction = new Stage();
                    addTransaction.centerOnScreen();
                    addTransaction.setTitle("Edit Supplier Profile");
                    addTransaction.initModality(Modality.APPLICATION_MODAL);
                    addTransaction.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        public void handle(WindowEvent event) {
                            Main.getStage().getScene().getRoot().setEffect(null);
                        }
                    });
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/supplieredit.fxml"));
                        EditSupplierController controller = new EditSupplierController(supplier.getTitle());
                        loader.setController(controller);
                        Parent parent = loader.load();
                        Scene scene = new Scene(parent, 707, 500);
                        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
                            public void handle(KeyEvent event) {
                                if (event.getCode() == KeyCode.ESCAPE) {
                                    Main.getStage().getScene().getRoot().setEffect(null);
                                    addTransaction.close();
                                }
                            }
                        });
                        addTransaction.setScene(scene);
                        addTransaction.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                edit.getScene().getWindow().hide();
            }
        });

        delete.setOnAction((ActionEvent event) -> {
            final Stage dialogStage = new Stage();
            Button ok = new Button("OK");
            ok.setOnMouseClicked((MouseEvent event1) -> {
                if (buyer != null) {
                    DatabaseClient.getInstance().deleteTableEntries("buyers1", "title", buyer.getTitle(),
                            true);
                } else if (supplier != null) {
                    DatabaseClient.getInstance().deleteTableEntries("suppliers1", "title",
                            supplier.getTitle(), true);
                }
                delete.getScene().getWindow().hide();
                dialogStage.close();
            });
            Button cancel1 = new Button("Cancel");
            cancel1.setOnMouseClicked((MouseEvent event1) -> {
                dialogStage.close();
            });
            cancel1.setDefaultButton(true);
            ok.setDefaultButton(false);
            ok.setLayoutX(25.0);
            ok.setLayoutY(150.0);
            cancel1.setLayoutX(150.0);
            cancel1.setLayoutY(150.0);
            String msg = String.format("You are going to delete this %s.\n Are you sure?",
                    partyType.getValue().toLowerCase());
            // --------------------
            GeneralMethods.confirm(new Button[]{ok, cancel1}, dialogStage, ProfileViewController.this, msg);
        });
        setupTotalAmountsTable(profList);
    }
    
    private void setupTotalAmountsTable(final ObservableList<PartyProfile> list) {
        //Setup total amounts table
        tableTotal.getColumns().clear();
        for (TableColumn column : dealAndPaysTable.getColumns()) {
            TableColumn newColumn = new TableColumn("");
            if (!column.getText().isEmpty()) {
                newColumn.setCellFactory(column.getCellFactory());
                newColumn.setCellValueFactory(column.getCellValueFactory());
            }
            newColumn.prefWidthProperty().bind(column.widthProperty());
            tableTotal.getColumns().add(newColumn);
        }
        tableTotal.setEditable(false);
        tableTotal.getItems().addAll(new PartyProfileList(list));
        //Hide Header
        tableTotal.widthProperty().addListener(
                (ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            Pane header = (Pane) tableTotal.lookup("TableHeaderRow");
            if (header.isVisible()){
                header.setMaxHeight(0);
                header.setMinHeight(0);
                header.setPrefHeight(0);
                header.setVisible(false);
            }
        });
        dealAndPaysTable.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            //Run after initialization to get controls
            for (Node bar1 : dealAndPaysTable.lookupAll(".scroll-bar")) {
                if (bar1 instanceof ScrollBar
                        && ((ScrollBar) bar1).getOrientation().equals(Orientation.HORIZONTAL)) {
                    ((ScrollBar) bar1).valueProperty().addListener((ObservableValue<? extends Number> observ, Number old, Number newVal) -> {
                        for (Node bar : tableTotal.lookupAll(".scroll-bar")) {
                            if (bar instanceof ScrollBar
                                    && ((ScrollBar) bar).getOrientation().equals(Orientation.HORIZONTAL)) {
                                ((ScrollBar) bar).setMax(((ScrollBar) bar1).getMax());
                                ((ScrollBar) bar).setMin(((ScrollBar) bar1).getMin());
                                ((ScrollBar) bar).setValue(((ScrollBar) bar1).getValue());
                            }
                        }
                    });
                }
            }
        });
    }

    private void calculateBal(List<PartyProfile> profileList) {
        double totRecdAmt = 0;
        double totPaidAmt = 0;
        for (PartyProfile pp : profileList) {
            if (!isIgnorable(pp.getAmountReceived())) {
                totRecdAmt += Double.valueOf(pp.getAmountReceived());
            }
            if (!isIgnorable(pp.getAmountPaid())) {
                totPaidAmt += Double.valueOf(pp.getAmountPaid());
            }
        }
        Double dblBalance = 0d;

        dblBalance = totPaidAmt - totRecdAmt;
        if (dblBalance < 0) {
            balanceAmt.setFill(Color.DARKGREEN);

        }
        else {
            balanceAmt.setFill(Color.DARKRED);

        }
        dblBalance = -(dblBalance);
        String strBalance = toStr(dblBalance.intValue());

        balanceAmt.setText(strBalance);
    }
    
    private void prepareUniversalList() {
        prepareSupplierList("Cash Sale");
        prepareBuyerList("Cash Sale");
    }

    private void prepareExpenditureList() {
        expenditureLines.clear();
        List<Expenditure> xprList = dbclient.getExpenditureList();
        for (Expenditure line : xprList) {
            if (line.getType().equalsIgnoreCase(expenditure)) {
                expenditureLines.add(line);
            }
        }
    }

    private void prepareSupplierList(String supplierTitle) {
        try {
            Map<String, DSupplierTableLine> grSuppMap = prepareSuppDealMap(supplierTitle);
            supplierlines.clear();
            supplierlines.addAll(grSuppMap.values());

        } catch (Exception e) {
            System.out.print("sqlexception while fetching supplier deal entries from sql");
        }
    }

    private void prepareNonRegBuyerList() {
        ladaanDealsList = dbclient.getLadBijSaleDealsForBuyer(buyer.getTitle());
        prepareBuyerList(buyer.getTitle());
        for (LadaanBijakSaleDeal lbLine : ladaanDealsList) {
            for (DBuyerTableLine buyLine : buyerlines) {
                if (lbLine.getDealId().equals(buyLine.getDealID())) {
                    /* we are using amounted total field of LadanBijak sale deal
                    * object to store the original buyer price. This will
                    * eventually be used to calculate the profit/loss */
                    lbLine.setAmountedTotal(buyLine.getAggregatedAmount());
                    lbLine.setInitialSaleDate(buyLine.getDate());
                }
            }
        }
    }

    private void prepareBuyerList(String buyerTitle) {
        try {
            buyerlines.clear();
            Map<String, List<DBuyerTableLine>> grBuyerMap = prepareBuyerDealMap(buyerTitle);
            for (String grNo : grBuyerMap.keySet()) {
                List<DBuyerTableLine> list = grBuyerMap.get(grNo);
                if (!list.isEmpty()) {
                    DBuyerTableLine line = list.get(0);
                    int totalCases = line.getCases();
                    int totalDueAmount = line.getAmountedTotal();
                    for (int idx = 1; idx < list.size(); idx++) {
                        DBuyerTableLine tmpLine = list.get(idx);
                        totalCases += tmpLine.getCases();
                        if (tmpLine.getAmountedTotal() != null) {
                            totalDueAmount += tmpLine.getAmountedTotal();
                        }
                    }
                    line.setCases(String.valueOf(totalCases));
                    line.setAmountedTotal(String.valueOf(totalDueAmount));
                }
                buyerlines.add(list.get(0));
            }
        } catch (Exception e) {
            System.out.print("Exception while fetching buyer deal entries from sql");
            e.printStackTrace();
        }
    }

    private Map<String, List<DBuyerTableLine>> prepareBuyerDealMap(String title) throws Exception {
        Map<String, List<DBuyerTableLine>> grBuyerMap = new LinkedHashMap<>();
        List<DBuyerTableLine> lines = dbclient.getBuyerDealEntries(title, null);
        for (DBuyerTableLine line : lines) {
            line.setAmountedTotal(line.getAggregatedAmount());
            String grNo = line.getDealID();
            List<DBuyerTableLine> list = grBuyerMap.get(grNo);
            if (list == null) {
                grBuyerMap.put(grNo, list = new ArrayList<>());
            }
            list.add(line);
            saleDealIds.add(line.getDealID());
        }
        return grBuyerMap;
    }

    private Map<String, DSupplierTableLine> prepareSuppDealMap(String supplierTitle) throws Exception {
        Map<String, DSupplierTableLine> grSuppMap = new LinkedHashMap<>();
        List<String[]> lines = dbclient.getSupplierDealEntryLines(supplierTitle);
        for (String[] values : lines) {
            DSupplierTableLine line = new DSupplierTableLine(values);
            if (line.getSupplierTitle().equals(supplier.getTitle())) {
                if (grSuppMap.containsKey(line.getDealID())) {
                    DSupplierTableLine tmpLine = grSuppMap.get(line.getDealID());
                    int tmpCases = Utils.toInt(tmpLine.getCases()) + Utils.toInt(line.getCases());
                    tmpLine.setCases(String.valueOf(tmpCases));
                } else {
                    grSuppMap.put(line.getDealID(), line);
                }
            }
        }
        return grSuppMap;
    }

    private List<PartyProfile> prepareProfileList(List<MoneyPaidRecd> mprList, List<String> sortedDates) {
        List<PartyProfile> profileList = new ArrayList<>();
        for (String sortedDate : new LinkedHashSet<>(sortedDates)) {
            switch (partyType) {
                case LADAAN:
                case BIJAK: {
                    List<DBuyerTableLine> buyerList = (List<DBuyerTableLine>) buyerlines;
                    for (DBuyerTableLine line : buyerList) {
                        if (line.getDate().equals(sortedDate)) {
                            PartyProfile pp = new PartyProfile(line.getDate(),
                                    String.valueOf(line.getCases()),
                                    String.valueOf(line.getAmountedTotal()), "", "Sales");
                            profileList.add(pp);
                        }
                    }
                    List<LadaanBijakSaleDeal> ladaanList = ladaanDealsList;
                    for (LadaanBijakSaleDeal line : ladaanList) {
                        if (line.getDate().equals(sortedDate)) {
                            Integer aggAmt = toInt(line.getAggregatedAmount());
                            Integer amt = aggAmt - (Integer.valueOf(line.getFreight())
                                    * Integer.valueOf(line.getCases())
                                    + Integer.valueOf(line.getComission()) * aggAmt / 100);
                            String desc = "%s (As of %s)";
                            Integer netAmount = amt - Integer.valueOf(line.getAmountedTotal());
                            String amtRecd = "";
                            String amtPaid = "";
                            
                            if (netAmount < 0) {
                                desc = String.format(desc, "Loss", line.getInitialSaleDate());
                                amtPaid = String.valueOf(-netAmount);
                            }
                            else {
                                desc = String.format(desc, "Profit", line.getInitialSaleDate());
                                amtRecd = String.valueOf(netAmount);
                            }
                            PartyProfile pp = new PartyProfile(line.getDate(), line.getCases(), amtRecd, amtPaid, desc);
                            profileList.add(pp);
                        }
                    }
                    break;
                }
                case BUYER: {
                    List<DBuyerTableLine> buyerList = (List<DBuyerTableLine>) buyerlines;
                    for (DBuyerTableLine line : buyerList) {
                        if (line.getDate().equals(sortedDate)) {
                            PartyProfile pp = new PartyProfile(line.getDate(),
                                    String.valueOf(line.getCases()), String.valueOf(line.getAmountedTotal()),
                                    "", "Sales");
                            profileList.add(pp);
                        }
                    }
                    break;
                }
                case SUPPLIER: {
                    List<DSupplierTableLine> suppList = (List<DSupplierTableLine>) supplierlines;
                    for (DSupplierTableLine line : suppList) {
                        if (line.getDate().equals(sortedDate)) {
                            PartyProfile pp = new PartyProfile(line.getDate(), line.getCases(), line.getNet(), "", "Sales");
                            profileList.add(pp);
                        }
                    }
                    break;
                }
                case EXPENDITURE: {
                    List<Expenditure> xprList = (List<Expenditure>) expenditureLines;
                    for (Expenditure line : xprList) {
                        if (line.getDate().equals(sortedDate)) {
                            PartyProfile pp = new PartyProfile(line.getDate(), "---", line.getAmount(), "", line.getPayee() + "@" + line.getComment());
                            pp.setReceipt(line.getReceipt());
                            profileList.add(pp);
                        }
                    }
                    break;
                }
                case UNIVERSAL_BANK: {
                    List<DBuyerTableLine> buyerList = (List<DBuyerTableLine>) buyerlines;
                    for (DBuyerTableLine line : buyerList) {
                        if (line.getDate().equals(sortedDate)
                                && line.getBuyerTitle().equals("Bank Sale")) {
                            PartyProfile pp = new PartyProfile(line.getDate(),
                                    String.valueOf(line.getCases()), String.valueOf(line.getAmountedTotal()),
                                    "", "Sales");
                            profileList.add(pp);
                        }
                    }
                    List<DSupplierTableLine> suppList = (List<DSupplierTableLine>) supplierlines;
                    for (DSupplierTableLine line : suppList) {
                        if (line.getDate().equals(sortedDate)
                                && line.getSupplierTitle().equals("Bank Sale")) {
                            PartyProfile pp = new PartyProfile(line.getDate(),
                                    line.getCases(), line.getNet(), "", "Sales");
                            profileList.add(pp);
                        }
                    }
                    break;
                }
                case UNIVERSAL_CASH: {
                    List<DBuyerTableLine> buyerList = (List<DBuyerTableLine>) buyerlines;
                    for (DBuyerTableLine line : buyerList) {
                        if (line.getDate().equals(sortedDate)
                                && line.getBuyerTitle().equals("Cash Sale")) {
                            PartyProfile pp = new PartyProfile(line.getDate(),
                                    String.valueOf(line.getCases()), String.valueOf(line.getAmountedTotal()),
                                    "", "Sales");
                            profileList.add(pp);
                        }
                    }
                    List<DSupplierTableLine> suppList = (List<DSupplierTableLine>) supplierlines;
                    for (DSupplierTableLine line : suppList) {
                        if (line.getDate().equals(sortedDate)
                                && line.getSupplierTitle().equals("Cash Sale")) {
                            PartyProfile pp = new PartyProfile(line.getDate(),
                                    line.getCases(), line.getNet(), "", "Sales");
                            profileList.add(pp);
                        }
                    }
                    break;
                }
            }
            for (MoneyPaidRecd line : mprList) {
                if (line.getDate().equals(sortedDate)) {
                    String amtPaid = (isIgnorable(line.getPaid())) ? "" : line.getPaid().trim();
                    String amtRecd = (isIgnorable(line.getReceived())) ? "" : line.getReceived().trim();
                    String desc = line.getDescription();
                    if (EntityType.UNIVERSAL_CASH.equals(partyType)
                            && !"cash".equalsIgnoreCase(line.getPaymentMode())) {
                        continue;
                    }
                    if (EntityType.UNIVERSAL_BANK.equals(partyType)
                            && !"bank".equalsIgnoreCase(line.getPaymentMode())) {
                        continue;
                    }
                    desc = desc == null ? "" : desc;
                    if (desc.isEmpty()) {
                        if (!amtPaid.isEmpty()) {
                            desc = MoneyPaidRecd.MONEY_PAID;
                        }
                        if (!amtRecd.isEmpty()) {
                            desc = MoneyPaidRecd.MONEY_RECEIVED;
                        }
                    }
                    if ("Bank".equalsIgnoreCase(line.getPaymentMode())) {
                        desc += " (Bank)";
                    }
                    PartyProfile pp = new PartyProfile(line.getDate(), "---",
                            amtPaid, amtRecd, desc + " (" + line.getTitle() + ")");
                    pp.setReceipt(line.getReceipt());
                    profileList.add(pp);
                }
            }
        }
        return profileList;
    }

    private boolean isIgnorable(String str) {
        if (str == null || str.trim().isEmpty() || str.trim().equals("0")) {
            return true;
        }
        return false;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void populateProfileTable(ObservableList<PartyProfile> itemsList) {

        TableColumn casesCol = new TableColumn("Cases");
        casesCol.setCellValueFactory(new PropertyValueFactory<>("cases"));
        casesCol.setPrefWidth(getPercent(dealAndPaysTable.getPrefWidth(), 15));
        BackgroundImage backgroundImage = new BackgroundImage(
                new Image(getClass().getResource("/icons/attachment_icon.png").toExternalForm(), 23, 23, true, true),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        final Background background = new Background(backgroundImage);

        TableColumn<PartyProfile, Map<String, InputStream>> descriptCol = new TableColumn<>("Description");
        descriptCol.setCellValueFactory(
                new PropertyValueFactory<PartyProfile, Map<String, InputStream>>("descReceiptMap"));
        descriptCol.setPrefWidth(getPercent(dealAndPaysTable.getPrefWidth(), 29));
        descriptCol.setCellFactory(
                new Callback<TableColumn<PartyProfile, Map<String, InputStream>>, TableCell<PartyProfile, Map<String, InputStream>>>() {
            @Override
            public TableCell<PartyProfile, Map<String, InputStream>> call(
                    TableColumn<PartyProfile, Map<String, InputStream>> param) {
                TableCell<PartyProfile, Map<String, InputStream>> cell = new TableCell<PartyProfile, Map<String, InputStream>>() {
                    private HBox hb;
                    private Label label;
                    private Button btn;
                    private double width;
                    InputStream receipt;
                    Image image;

                    @Override
                    protected void updateItem(final Map<String, InputStream> item, boolean empty) {
                        if (item != null) {
                            if (item.isEmpty()) {
                                return;
                            }

                            String tmpStr = item.keySet().toArray()[0].toString();
                            hb = new HBox();
                            hb.setAlignment(Pos.CENTER);
                            label = new Label();
                            label.setAlignment(Pos.CENTER);

                            width = this.getTableColumn().getPrefWidth();
                            hb.setPrefWidth(width);
                            tmpStr = extractDescription(tmpStr);
                            final String text = tmpStr;
                            label.setText(text);
                            label.setPrefWidth(width);
                            hb.getChildren().add(label);
                            receipt = item.get(text);
                            if (((text.equalsIgnoreCase(MoneyPaidRecd.MONEY_PAID) || text.equals(MoneyPaidRecd.MONEY_RECEIVED))
                                    && receipt != null) || receipt != null) {
                                btn = new Button();
                                btn.setBackground(background);
                                btn.setPrefWidth((width * 15) / 100);
                                label.setPrefWidth((width * 80) / 100);
                                hb.getChildren().add(btn);
                                btn.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        if (receipt == null) {
                                            GeneralMethods.errorMsg("No receipt found for this entry");
                                            return;
                                        }
                                        final Stage dialog = new Stage();
                                        dialog.initModality(Modality.APPLICATION_MODAL);
                                        // dialog.initOwner(this);
                                        VBox dialogVbox = new VBox(20);
                                        if (image == null) {
                                            image = new Image(receipt);
                                        }
                                        final ImageView imv = new ImageView(image);
                                        dialogVbox.getChildren().add(imv);
                                        Scene dialogScene = new Scene(dialogVbox, image.getWidth() + 20,
                                                image.getHeight() + 20);
                                        dialog.setScene(dialogScene);
                                        dialog.show();
                                    }
                                });
                            }
                            setGraphic(hb);
                        }
                    }
                };

                return cell;
            }
        });

        descriptCol.setEditable(true);

        TableColumn<PartyProfile, String> payeeCol = new TableColumn<>("Payee");
        payeeCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<PartyProfile, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(CellDataFeatures<PartyProfile, String> param) {
                PartyProfile profile = param.getValue();
                String description = profile.getDescription();
                description = description.substring(0, description.indexOf("@"));
                return new SimpleStringProperty(description);
            }
        });

        TableColumn dateCol = new TableColumn("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        if (EntityType.EXPENDITURE.equals(partyType)) {
            dateCol.setPrefWidth(getPercent(dealAndPaysTable.getPrefWidth(), 13));
        } else {
            dateCol.setPrefWidth(getPercent(dealAndPaysTable.getPrefWidth(), 21));
        }

        TableColumn amtPaidCol = new TableColumn("Amount Dr.");
        amtPaidCol.setCellValueFactory(new PropertyValueFactory<>("amountPaid"));

        amtPaidCol.setPrefWidth(getPercent(dealAndPaysTable.getPrefWidth(), 17));

        TableColumn amtReceivedCol = new TableColumn("Amount Cr.");
        amtReceivedCol.setCellValueFactory(new PropertyValueFactory<>("amountReceived"));
        amtReceivedCol.setPrefWidth(getPercent(dealAndPaysTable.getPrefWidth(), 17));

        dealAndPaysTable.getColumns().addAll(dateCol, casesCol, descriptCol, amtPaidCol, amtReceivedCol);
        if (partyType.equals(EntityType.EXPENDITURE)) {
            dealAndPaysTable.getColumns().add(2, payeeCol);
        }
        dealAndPaysTable.setItems(itemsList);
    }

    private Integer toInt(String str) {
        str = str.trim();
        return Integer.valueOf(str);
    }

    private String toStr(Integer integer) {
        return String.valueOf(integer);
    }

    @SuppressWarnings("unchecked")
    private List<Date> convertToUtilDates(List<?> list, ObjectType objectType) {
        List<Date> dates = new ArrayList<>();
        switch (objectType) {
            case LADAAN: {
                List<LadaanBijakSaleDeal> newList = (List<LadaanBijakSaleDeal>) list;
                for (LadaanBijakSaleDeal line : newList) {
                    dates.add(convertDate(line.getDate()));
                }
                break;
            }
            case BUYER: {
                List<DBuyerTableLine> newList = (List<DBuyerTableLine>) list;
                for (DBuyerTableLine line : newList) {
                    dates.add(convertDate(line.getDate()));
                }
                break;
            }
            case SUPPLIER: {
                List<DSupplierTableLine> newList = (List<DSupplierTableLine>) list;
                for (DSupplierTableLine line : newList) {
                    dates.add(convertDate(line.getDate()));
                }
                break;
            }
            case MPR: {
                List<MoneyPaidRecd> newList = (List<MoneyPaidRecd>) list;
                for (MoneyPaidRecd line : newList) {
                    dates.add(convertDate(line.getDate()));
                }
                break;
            }
            case EXPENDITURE: {
                List<Expenditure> newList = (List<Expenditure>) list;
                for (Expenditure line : newList) {
                    dates.add(convertDate(line.getDate()));
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown object type detect");
        }
        return dates;
    }

    private List<String> convertToSortedStringDates(List<Date> dates) {
        dates = getSortedDates(dates);
        List<String> stringDates = new ArrayList<>();
        if (dates != null) {
            for (int idx = 0; idx < dates.size(); idx++) {
                Date d = dates.get(idx);
                String strDate = d == null ? "" : dateFormat.format(d);
                stringDates.add(strDate);
            }
        }
        return stringDates;
    }

    private List<Date> getSortedDates(List<Date> dates) {
        dates.sort(new Comparator<Date>() {
            @Override
            public int compare(Date o1, Date o2) {
                if (o1.before(o2)) {
                    return -1;
                } else if (o1.after(o2)) {
                    return 1;
                }
                return 0;
            }
        });
        return dates;
    }

    private Date convertDate(String srcDate) {
        if (srcDate == null || srcDate.trim().isEmpty()) {
            return new Date(System.currentTimeMillis());
        }
        String format = DateUtil.determineDateFormat(srcDate);
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(format);
        }
        Date d = null;
        try {
            d = DateUtil.parse(srcDate, format);
        } catch (ParseException e) {
            d = new Date(System.currentTimeMillis());
            System.out.println("Invalid date observed, using today's value");
            e.printStackTrace();
        }
        return d;
    }

    private double getPercent(double percentOf, int percentValue) {
        return ((percentValue * percentOf) / 100);
    }

    private String extractDescription(String tmpStr) {
        if (partyType == EntityType.EXPENDITURE) {
            if (tmpStr.contains("@")) {
                tmpStr = tmpStr.substring(tmpStr.indexOf("@") + 1, tmpStr.length());
            }
        }
        return tmpStr;
    }

}
