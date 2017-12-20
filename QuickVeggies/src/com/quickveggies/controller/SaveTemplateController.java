package com.quickveggies.controller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

import com.quickveggies.GeneralMethods;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Template;
import com.quickveggies.misc.CustomComboboxListCell;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import com.sun.javafx.scene.control.skin.VirtualScrollBar;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;

public class SaveTemplateController implements Initializable {

    @FXML
    private VBox tablePane;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Pane paneParent;

    private Button removeTemplate, addTemplate, preview;
    private TextField templateName;

    private String accountName = null;

    @SuppressWarnings({"rawtypes"})
    private TableView table;
    @SuppressWarnings("rawtypes")

    private ListView assignBoxesView;
    private final ObservableList<String> defaulColumnNameList = FXCollections.observableArrayList("<excluded>",
            "Transaction Id", "Date", "Cheque No.", "Comment", "Withdrawal", "Deposit", "Balance"
    );

    private ArrayList<CustomComboboxListCell> assignBoxesCellsList = new ArrayList<CustomComboboxListCell>();

    private final double constWidth = 120.0, constHeight = 50.0;
    // private ChoiceBox<String> templatesChoice=null;

    // private ArrayList<ComboBox<String>> assignBoxesList=new
    // ArrayList<ComboBox<String>>();
    @SuppressWarnings({"unchecked", "rawtypes"})
    public SaveTemplateController(String accountName, TableView table, Button removeTemplate, Button addTemplate,
            TextField templateName, Button preview) {// ChoiceBox<String>
        // templatesChoice){
        this.accountName = accountName;
        this.table = table;
        table.setEditable(false);
        // this.templatesChoice=templatesChoice;
        this.removeTemplate = removeTemplate;
        this.addTemplate = addTemplate;
        this.templateName = templateName;
        this.preview = preview;

        // create the comboboxes 1-line table where the user assigns the column
        // types
        assignBoxesView = new ListView();
        assignBoxesView.setOrientation(javafx.geometry.Orientation.HORIZONTAL);

        // GeneralMethods.fixTableColumnWidth(table, constWidth);
        GeneralMethods.fixListViewHeight(assignBoxesView, constHeight);

    }// end of constructor

    private String[] columnNames = new String[]{"accountName", "transactionId", "dateCol", "chqnoCol", "descriptionCol",
        "withdrawalCol", "depositCol", "balance"};
    private Integer[] columnsAssoc = new Integer[]{-1, -1, -1, -1, -1, -1, -1, -1};

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Rectangle2D screenRect = Screen.getPrimary().getVisualBounds();
            double scrWidth = screenRect.getWidth();
            Object[] nodes = new Object[]{paneParent, tablePane, table, assignBoxesView};
            double componentWidth = scrWidth - 10;
            for (Object node : nodes) {
                if (node instanceof Control) {
                    ((Control) node).setPrefWidth(componentWidth);
                    ((Control) node).setMaxWidth(componentWidth);
                } else if (node instanceof Pane) {
                    ((Pane) node).setMaxWidth(componentWidth);
                    ((Pane) node).setPrefWidth(componentWidth);
                }
            }
            int colCount = table.getColumns().size();
            final double colWidth = ((int) componentWidth / colCount);

            GeneralMethods.fixTableColumnWidth(table, colWidth);

            ObservableList buffer = FXCollections.observableArrayList();
            int tableLength = table.getColumns().size();
            for (int i = 0; i < tableLength; i++) {
                buffer.add(defaulColumnNameList.get(0));
            }
            assignBoxesView.setCellFactory(new javafx.util.Callback<ListView, ListCell>() {
                @Override
                public ListCell call(ListView param) {
                    final CustomComboboxListCell cell = new CustomComboboxListCell(defaulColumnNameList);
                    cell.getComboBox().setPrefWidth(colWidth);
                    cell.setPrefHeight(30.0);
                    cell.setPrefWidth(colWidth);

                    cell.setComboBoxHandler(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> obs, String oldValue, String newValue) {
                            // no duplication allowed in column names
                            int currCellInd = assignBoxesCellsList.indexOf(cell);
                            for (CustomComboboxListCell otherCell : assignBoxesCellsList) {
                                if (assignBoxesCellsList.indexOf(otherCell) == currCellInd) {
                                    continue;// dont check the original cell
                                }
                                Object otherVal = otherCell.getComboBox().getValue();
                                if (otherVal == null) {
                                    continue;
                                }
                                String otherString = otherVal.toString();
                                if (otherString.equals(newValue)) {
                                    if (otherString.equals(defaulColumnNameList.get(0))) {
                                        continue;// skip check if value is
                                    }													// <empty>
                                    GeneralMethods.errorMsg("Column name already assigned!");
                                    cell.getComboBox().setValue(oldValue);
                                }
                            }
                        }
                    });

                    cell.setAutosync(false);// the cell values are irrelevant,
                    // the
                    // boxes are important
                    cell.getComboBox().setValue(defaulColumnNameList.get(0));
                    assignBoxesCellsList.add(cell);
                    return cell;
                }
            });
            assignBoxesView.setItems(buffer);

            GeneralMethods.fixListViewHeight(assignBoxesView, constHeight);

            tablePane.getChildren().addAll(assignBoxesView, table);
        } catch (NullPointerException e) {
            System.out.println("storedTableData=null in SaveTemplateController");
            e.printStackTrace();
        }

        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                int rawDataColumnInd = 0;
                // check that all column types have been assigned
                int excludedNum = 0;
                for (CustomComboboxListCell cell : assignBoxesCellsList) {
                    if (cell.isEmpty()) {
                        continue;
                    }
                    String val = cell.getComboBox().getValue().toString();
                    if (val.equals(defaulColumnNameList.get(0))) {
                        excludedNum++;
                    } else {
                        int colNameInd = defaulColumnNameList.indexOf(val);
                        columnsAssoc[colNameInd] = rawDataColumnInd;
                    }
                    rawDataColumnInd++;
                }
                System.out.println(Arrays.toString(columnNames));
                System.out.println(Arrays.toString(columnsAssoc));
                int tableSize = table.getColumns().size();
                int defColListCount = defaulColumnNameList.size();
                int assignedColCount = (tableSize + 1) < defColListCount ? tableSize + 1 : defColListCount;

                if (excludedNum > (tableSize - ((assignedColCount) - 1))) {
                    GeneralMethods.errorMsg("Not all column types have been assigned!");
                    return;
                }
                Template t = new Template(accountName, columnsAssoc[1], columnsAssoc[2], columnsAssoc[3],
                        columnsAssoc[4], columnsAssoc[5], columnsAssoc[6], columnsAssoc[7]);
                DatabaseClient.getInstance().saveTemplate(t);

                // templatesChoice.getItems().add(accountName);
                templateName.setText(accountName);
                addTemplate.setDisable(true);
                addTemplate.setVisible(false);
                removeTemplate.setDisable(false);
                removeTemplate.setVisible(true);
                preview.setDisable(false);
                saveButton.getScene().getWindow().hide();
            }
        });
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                cancelButton.getScene().getWindow().hide();
            }
        });
    }

    public static VirtualScrollBar getHbar(VirtualFlow vf) {
        try {
            final Method method = VirtualFlow.class.getDeclaredMethod("getHbar");
            method.setAccessible(true);
            return (VirtualScrollBar) method.invoke(vf);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }
}
