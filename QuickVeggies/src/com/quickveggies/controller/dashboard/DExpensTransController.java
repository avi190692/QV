package com.quickveggies.controller.dashboard;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.ai_int.utils.ExcelExportUtil;
import com.ai_int.utils.FileUtil;
import com.ai_int.utils.javafx.ListViewUtil;
import com.ai_int.utils.javafx.TableUtil;
import com.quickveggies.Main;
import com.quickveggies.controller.ExpenseAddController;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.DExpensesTableLine;
import com.quickveggies.entities.DExpensesTableList;
import com.quickveggies.entities.Expenditure;
import com.quickveggies.misc.DeleteTableButtonCell;
import javafx.beans.value.ObservableValue;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class DExpensTransController implements Initializable {

    @FXML
    private Label Title;

    @FXML
    private Label rsInvoice;

    @FXML
    private Label openInvoice;

    @FXML
    private Label rsOverdue;

    @FXML
    private Label overdue;

    @FXML
    private Label rsPaid;

    @FXML
    private ComboBox<?> batchActions;

    @FXML
    private TableView<DExpensesTableLine> table;
    
    @FXML
    private TableView<DExpensesTableLine> tableTotal;

    @FXML
    private Button newExpense;

    @FXML
    private Label rsEstimate1;

    @FXML
    private Label openInvoice1;

    @FXML
    private Label rsEstimate2;

    @FXML
    private Label overdue1;

    @FXML
    private Button btnColSettings;

    @FXML
    private Button btnPrint;

    @FXML
    private Button btnExport;

    private DatabaseClient dbclient = DatabaseClient.getInstance();

    private ObservableList<DExpensesTableLine> lines = FXCollections.observableArrayList(new DExpensesTableLine(0, "", "", "", "", ""));

    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources) {
        //populate expenses table
        lines.clear();
        for (Expenditure x : dbclient.getExpenditureList()) {
            lines.add(new DExpensesTableLine(new String[]{x.getAmount(), x.getDate(), x.getComment(), x.getPayee(),
                x.getType(), String.valueOf(x.getId())}));

        }

        final Pane pane = (Pane) btnColSettings.getParent().getParent();
        ListViewUtil.addColumnSettingsButtonHandler(table, pane, btnColSettings);

        TableColumn<DExpensesTableLine, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setCellFactory(TextFieldTableCell.<DExpensesTableLine>forTableColumn());

        TableColumn<DExpensesTableLine, String> amountCol = new TableColumn<>("Sum");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setCellFactory(TextFieldTableCell.<DExpensesTableLine>forTableColumn());

        TableColumn<DExpensesTableLine, String> commentCol = new TableColumn<>("Comment");
        commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
        commentCol.setCellFactory(TextFieldTableCell.<DExpensesTableLine>forTableColumn());

        TableColumn<DExpensesTableLine, String> billToCol = new TableColumn<>("Billed to");
        billToCol.setCellValueFactory(new PropertyValueFactory<>("billto"));
        billToCol.setCellFactory(TextFieldTableCell.<DExpensesTableLine>forTableColumn());

        TableColumn<DExpensesTableLine, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setCellFactory(TextFieldTableCell.<DExpensesTableLine>forTableColumn());

        TableColumn<DExpensesTableLine, String> deleteCol = new TableColumn<>();
        deleteCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        deleteCol.setCellFactory(new javafx.util.Callback<TableColumn<DExpensesTableLine, String>, TableCell<DExpensesTableLine, String>>() {
            @SuppressWarnings("rawtypes")
            @Override
            public TableCell<DExpensesTableLine, String> call(TableColumn<DExpensesTableLine, String> param) {
                return new DeleteTableButtonCell("expenditures", "id");
            }
        });
        table.setEditable(false);
        table.setItems(lines);
        table.getColumns().addAll(dateCol, amountCol, commentCol, billToCol, typeCol, deleteCol);

        btnExport.setOnAction((event) -> {
            String[][] tableData = TableUtil.toArray(table);
            String fileName = FileUtil.getSaveToFileName(btnExport.getScene(), "Select Excel file", FileUtil.getExcelExtMap());
            if (fileName != null) {
                try {
                    ExcelExportUtil.exportTableData(tableData, "Expense transaction List", fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnPrint.setOnAction((event) -> TableUtil.printTable(table, "Expenditures", deleteCol));

        //changed by ss on 05-Jan-2018
        //newExpense.setOnAction((ActionEvent event) -> 
        newExpense.setOnAction(new EventHandler<ActionEvent>(){
        	@Override
    		public void handle(ActionEvent event)
        	{
    
            final Stage addTransaction = new Stage();
            addTransaction.centerOnScreen();
            addTransaction.setTitle("Add Expense");
            addTransaction.initModality(Modality.APPLICATION_MODAL);
            
            addTransaction.setOnCloseRequest((WindowEvent event1) -> {
                Main.getStage().getScene().getRoot().setEffect(null);
            });
	            try 
	            {
	                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/expenditureadd.fxml"));
	                ExpenseAddController controller = new ExpenseAddController();
	                loader.setController(controller);
	                Parent parent = loader.load();
	                Scene scene = new Scene(parent);
	                scene.setOnKeyPressed((KeyEvent event1) -> 
	                {
	                    if (event1.getCode() == KeyCode.ESCAPE) 
	                    {
	                        Main.getStage().getScene().getRoot().setEffect(null);
	                        addTransaction.close();
	                    }
	             });
	                addTransaction.setScene(scene);
	                addTransaction.show();
	            } 
	            catch (IOException e) 
	            {
	                e.printStackTrace();
	            }
           }
        });
        setupTotalAmountsTable(lines);
    }
    
    private void setupTotalAmountsTable(final ObservableList<DExpensesTableLine> list) {
        //Setup total amounts table
        tableTotal.getColumns().clear();
        for (TableColumn column : table.getColumns()) {
            TableColumn newColumn = new TableColumn("");
            if (!column.getText().isEmpty()) {
                newColumn.setCellFactory(column.getCellFactory());
                newColumn.setCellValueFactory(column.getCellValueFactory());
            }
            newColumn.prefWidthProperty().bind(column.widthProperty());
            tableTotal.getColumns().add(newColumn);
        }
        tableTotal.setEditable(false);
        tableTotal.getItems().addAll(new DExpensesTableList(list));
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
        table.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            //Run after initialization to get controls
            for (Node bar1 : table.lookupAll(".scroll-bar")) {
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
}
