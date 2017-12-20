package com.quickveggies.misc;

import com.quickveggies.GeneralMethods;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

public class PrintTableButtonCell<S, T> extends TableCell<S, T> {

    private final Button printButton = new Button();

    private String lineId;

    public PrintTableButtonCell() {
        //set the image for the button
        printButton.setPrefSize(30, 30);
        BackgroundImage backgroundImage = new BackgroundImage(new Image(getClass().getResource("/icons/print.png").toExternalForm(), 30, 30, true, true), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        Background background = new Background(backgroundImage);
        printButton.setBackground(background);
        if (printButton.getOnMouseClicked() == null) {
            printButton.setOnMouseClicked((MouseEvent event) -> {
                GeneralMethods.msg("This component will be completed soon");
            });
        }
    }

    private void setCellProperty(String value) {
        lineId = value;
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        }
        else {
            try {
                setCellProperty(item.toString());
            }
            catch (NullPointerException e) {
            }
            setGraphic(printButton);
        }
    }

    public Button getPrintButton() {
        return printButton;
    }

}
