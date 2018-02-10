package com.quickveggies.controller.popup;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import com.quickveggies.controller.dashboard.DashboardController;

/**
 *
 * @author serg.merlin
 */
public class SendEmailsPopupController {
    
    private final List<EnteremailpaneController> panels;
    private final VBox pane;
    private final VBox container;
    
    private SendEmailsPopupController(List<SendEmailData> templates) {
        panels = new ArrayList<>();
        pane = new VBox();
        container = new VBox();
        //Add panels for every email template
        for (SendEmailData email : templates) {
            FXMLLoader loader = new FXMLLoader(SendEmailsPopupController.class.
                    getResource("/fxml/enteremailpane.fxml"));
            EnteremailpaneController controller = new EnteremailpaneController(
                    email.to, email.from, email.attachment);
            loader.setController(controller);
            panels.add(controller);
            try {
                 container.getChildren().add(loader.load());
            } catch (IOException ex) {
                Logger.getLogger(SendEmailsPopupController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        pane.setPrefWidth(((Pane) container.getChildren().get(0)).getPrefWidth() + 60.0);
        pane.setMaxHeight(((Pane) container.getChildren().get(0)).getPrefHeight() * panels.size() + 60.0);
        ScrollPane scroll = new ScrollPane();
        pane.getChildren().add(scroll);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(15, 15, 15, 15));
        scroll.setContent(container);
        Button sendButton = new Button();
        sendButton.setText("SEND all emails");
        sendButton.setPadding(new Insets(5, 50, 5, 50));
        container.getChildren().add(sendButton);
        sendButton.setOnAction((ActionEvent event) -> {
            List<SendEmailData> dealsToSend = new ArrayList<>();
            for (EnteremailpaneController controller : panels) {
                dealsToSend.add(controller.getEmailData());
            }
            DashboardController.showPopup("/fxml/sendemailpopup.fxml", "Email",
                    new SendemailpopupController(dealsToSend));
            sendButton.getScene().getWindow().hide();
        });
    }
    
    public static void showSendEmailsPopup(List<SendEmailData> templates) {
        SendEmailsPopupController popup = new SendEmailsPopupController(templates);
        double height = popup.pane.getMaxHeight();
        Scene scene = new Scene(popup.pane, popup.pane.getPrefWidth(),
                height < 640.0 ? height : 640.0);
        Stage primaryStage = new Stage();
        primaryStage.initStyle(StageStyle.UTILITY);
        primaryStage.centerOnScreen();
        primaryStage.initModality(Modality.APPLICATION_MODAL);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Send Email(s)");
        primaryStage.setResizable(true);
        primaryStage.sizeToScene();
        primaryStage.show();
    }
}
