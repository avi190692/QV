package com.quickveggies.misc;

import java.io.IOException;
import java.util.ArrayList;

import com.quickveggies.Main;
import com.quickveggies.controller.UpdateEntryController;
import com.quickveggies.controller.UpdateLadaanEntryController;
import com.quickveggies.controller.UpdateRegularBuyerController;
import com.quickveggies.controller.UpdateSupplierEntryController;
import com.quickveggies.entities.DBuyerTableLine;
import com.quickveggies.entities.DSupplierTableLine;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EditTableButtonCell<S, T> extends TableCell<S, T> {

    private final Button editButton = new Button();
    private final Runnable onEditFinished;
    private String lineId;
    private String[] cellValuesFactoryList = null;
    private ArrayList<String> colNamesList = new ArrayList<String>();
    private String[] valuesList = null;

    public EditTableButtonCell(final String tableLineType) {
        this(tableLineType, null);
    }
    
    public EditTableButtonCell(final String tableLineType, Runnable onEditFinished) {
        this.onEditFinished = onEditFinished;
        //set the image for the button
        editButton.setPrefSize(30, 30);
        BackgroundImage backgroundImage = new BackgroundImage(new Image(getClass().getResource("/icons/edit.png").toExternalForm(), 30, 30, true, true), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        Background background = new Background(backgroundImage);
        editButton.setBackground(background);
        editButton.setOnMouseClicked((MouseEvent event) -> {
            //get arrays of column names, values and cellValuFactory
            //--------------------------------------------------
            ObservableList<TableColumn<S, ?>> columnsList = EditTableButtonCell.this.getTableView().getColumns();
            int rowIdx = EditTableButtonCell.this.getTableRow().getIndex();
            Object row = getTableView().getItems().get(rowIdx);
            colNamesList.clear();
            for (int i = 1; i < columnsList.size() - 4; i++) {
                colNamesList.add(((TableColumn) (columnsList.get(i))).getText());
            }
            switch (tableLineType) {
                case "DBuyerTableLine":
                    valuesList = ((DBuyerTableLine) row).getAll();
//                    cellValuesFactoryList=new String[]{"buyerTitle","date","buyerRate","amountedTotal","cases"};
                    lineId = ((DBuyerTableLine) row).getSaleNo();
                    cellValuesFactoryList = new String[]{"date", "dealID", "buyerRate", "amountedTotal", "cases"};
                    break;
                    
                case "DSupplierTableLine":
                    valuesList = ((DSupplierTableLine) row).getAll();
                    colNamesList.set(4, "Rate");
                    //cellValuesFactoryList=new String[]{"dealId", "supplierTitle","date","supplierRate","net","cases","agent"};
                    cellValuesFactoryList = new String[]{"date", "supplierTitle", "proprietor", "cases", "supplierRate", "net", "agent"};
                    break;

                default:
                    System.out.print("tableLineType specified for switch wasn't found\n");
            }
            //------------------------------------------------------------
            //OPEN EDIT ENTRY WINDOW
            final Stage addTransaction = new Stage();
            addTransaction.centerOnScreen();
            addTransaction.setTitle("Edit entry");
            addTransaction.initModality(Modality.APPLICATION_MODAL);
            String guiToLoad = null;
            try {
                Initializable controller = null;
                int width1 = 0;
                int height1 = 0;
                width1 = 707;
                height1 = 300;
                if (tableLineType.equalsIgnoreCase("DBuyerTableLine")) {
                    String bType = ((DBuyerTableLine) row).getBuyerType().trim().toUpperCase();
                    System.out.println(bType);
                    switch (bType) {
                        case "BIJAK":
                        case "LADAAN":
                            guiToLoad = "/fxml/updateentryladaanbuyer.fxml";
                            controller = new UpdateLadaanEntryController(tableLineType, lineId, valuesList);
                            width1 = 707;
                            height1 = 500;
                            break;
                        case "REGULAR":
                            guiToLoad = "/fxml/updateentry.fxml";
                            controller = new UpdateRegularBuyerController(tableLineType, lineId, valuesList);
                            break;
                    }
                } else if (tableLineType.equalsIgnoreCase("DSupplierTableLine")) {
                    guiToLoad = "/fxml/updateentry.fxml";
                    controller = new UpdateSupplierEntryController(tableLineType, lineId, valuesList);

                } else {
                    guiToLoad = "/fxml/updateentry.fxml";
                    controller = new UpdateEntryController(tableLineType, colNamesList.toArray(new String[0]),
                            lineId, valuesList, cellValuesFactoryList);
                }
                FXMLLoader loader = new FXMLLoader(getClass().getResource(guiToLoad));
                loader.setController(controller);
                Parent parent1 = loader.load();
                Scene scene1 = new Scene(parent1, width1, height1);
                scene1.setOnKeyPressed(new EventHandler<KeyEvent>() {
                    public void handle(KeyEvent event) {
                        if (event.getCode() == KeyCode.ESCAPE) {
                            Main.getStage().getScene().getRoot().setEffect(null);
                            addTransaction.close();
                        }
                    }
                });
                addTransaction.setScene(scene1);
                addTransaction.showAndWait();
                if (onEditFinished != null) {
                    onEditFinished.run();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void setCellProperty(String value) {
        lineId = value;
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            try {
                setCellProperty(item.toString());
            } catch (NullPointerException e) {
            }
            setGraphic(editButton);
        }
    }

}
