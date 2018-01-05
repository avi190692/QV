package com.quickveggies.misc;

import java.io.IOException;

import com.quickveggies.GeneralMethods;
import com.quickveggies.Main;
import com.quickveggies.controller.FreshEntryTableData.BuyerEntryTableLine;
import com.quickveggies.controller.SearchPartyController;
import com.quickveggies.entities.AccountEntryLine;
import com.quickveggies.entities.PartyType;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
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
import javafx.stage.WindowEvent;

public class SearchPartyButton extends Button {

    private PartyType partyType = PartyType.BUYER_SUPPLIERS;
    private Object linkedObject = null;
    private String selectedValue;

    public SearchPartyButton() {
        BackgroundImage backgroundImage = new BackgroundImage(
                new Image(getClass().getResource("/icons/search_icon.png").toExternalForm(), 30, 30, true, true),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        Background background = new Background(backgroundImage);
        setBackground(background);

        setOnMouseClicked((MouseEvent event) -> {
            // OPEN SEARCH PARTY WINDOW
            final Stage stage = new Stage();
            stage.centerOnScreen();
            stage.setTitle("Edit entry");
            stage.initModality(Modality.APPLICATION_MODAL);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/searchparty.fxml"));
                final SearchPartyController controller = new SearchPartyController(partyType,linkedObject);
                loader.setController(controller);
                Parent parent1 = loader.load();
                Scene scene1 = new Scene(parent1);
                scene1.setOnKeyPressed((KeyEvent event1) -> {
                    if (event1.getCode() == KeyCode.ESCAPE) {
                        Main.getStage().getScene().getRoot().setEffect(null);
                        stage.close();
                    }
                });
                EventHandler<WindowEvent> we = (WindowEvent event1) -> {
                    if (linkedObject == null) {
                        return;
                    }
                    try {
                        if (linkedObject instanceof AutoCompleteTextField) {
                            AutoCompleteTextField actf = (AutoCompleteTextField) linkedObject;
                            actf.setText(controller.getSelectedValue());
                            actf.hidePopup();
                        } else if (linkedObject instanceof PartySearchTableCell) {
                            PartySearchTableCell linkedCell = (PartySearchTableCell) linkedObject;
                            BuyerEntryTableLine beLine = null;
                            Object obj = linkedCell.getTableRow().getItem();
                            if (obj instanceof BuyerEntryTableLine) {
                                beLine = (BuyerEntryTableLine) obj;
                                beLine.setBuyerSelect(controller.getSelectedValue());
                            } else if (obj instanceof AccountEntryLine) {
                                AccountEntryLine acLine = (AccountEntryLine) obj;
                                acLine.setPayee(controller.getSelectedValue());
                                acLine.setPayeeType(controller.getPartyType().toString());
                            }
                            GeneralMethods.refreshTableView(linkedCell.getTableView(), null);
                        }
                        ((Control) linkedObject).setUserData(controller.getPartyType());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };
                stage.setOnCloseRequest(we);
                stage.setOnHiding(we);
                stage.setScene(scene1);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setPartyType(PartyType newVal) {
        partyType = newVal;
    }

    public Object getLinkedObject() {
        return linkedObject;
    }

    public void setLinkedObject(Object linkedObject) {
        this.linkedObject = linkedObject;
    }

    public String getSelectedValue() {
        return this.selectedValue;
    }

    public void clearChoice() {
        selectedValue = null;
    }
}
