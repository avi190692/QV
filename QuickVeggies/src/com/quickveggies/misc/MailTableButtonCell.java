package com.quickveggies.misc;

import java.util.function.Consumer;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.binding.*;

public class MailTableButtonCell<S, T> extends TableCell<S, T> {

    private final Button mailButton = new Button();
    private final ContextMenu contextMenu;
    private final Consumer<Integer> onSendSmsEvent;
    private final Consumer<Integer> onSendEmailEvent;
    
    private String sender;
    private String receiver;
    private String attachment;

    public MailTableButtonCell(Consumer<Integer> onSendSmsEvent, Consumer<Integer> onSendEmailEvent) {
        this.onSendSmsEvent = onSendSmsEvent;
        this.onSendEmailEvent = onSendEmailEvent;
        // set the image for the button
        mailButton.setPrefSize(30, 30);
        BackgroundImage backgroundImage = new BackgroundImage(
                new Image(getClass().getResource("/icons/env2.png").toExternalForm(), 30, 30, true, true),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        Background background = new Background(backgroundImage);
        mailButton.setBackground(background);
        //Add popup menu SMS/Email
        contextMenu = new ContextMenu();
        MenuItem sendSMS = new MenuItem("Send SMS");
        MenuItem sendEmail = new MenuItem("Send Email");
        contextMenu.getItems().addAll(sendSMS, sendEmail);
//        contextMenu.setStyle("-fx-background-color: #303030;");
//        sendSMS.setStyle("-fx-background-color: #303030; -fx-text-fill: white;");
//        sendEmail.setStyle("-fx-background-color: #303030; -fx-text-fill: white;");
        mailButton.setOnMouseClicked((MouseEvent event) -> {
            if (onSendSmsEvent == null) {
                MailTableButtonCell.this.onSendEmailEvent.accept(getIndex());
            }
            else if (onSendEmailEvent == null) {
                MailTableButtonCell.this.onSendSmsEvent.accept(getIndex());
            }
            else {
                contextMenu.show(mailButton, event.getScreenX(), event.getScreenY());
            }
        });
        sendSMS.setOnAction((ActionEvent event) -> {
            contextMenu.hide();
            if (MailTableButtonCell.this.onSendSmsEvent != null) {
                MailTableButtonCell.this.onSendSmsEvent.accept(getIndex());
            }
        });
        sendEmail.setOnAction((ActionEvent event) -> {
            contextMenu.hide();
            if (MailTableButtonCell.this.onSendEmailEvent != null) {
                MailTableButtonCell.this.onSendEmailEvent.accept(getIndex());
            }
        });
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        }
        else {
            setGraphic(mailButton);
        }
    }

    public Button getMailButton() {
        return mailButton;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Object getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

}
