package com.quickveggies.controller.popup;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.WindowEvent;

import com.ai_int.utils.EmailUtil;

/**
 * FXML Controller class
 *
 * @author Sergey Orlov <serg.merlin@gmail.com>
 */
public class SendemailpopupController implements Initializable {
    
    @FXML
    AnchorPane sendConfirmationPane;
    
    @FXML
    AnchorPane sentPane;
    
    @FXML
    VBox bayersPane;
    
    @FXML
    Button sendButton;
    
    @FXML
    Button closeButton;
    
    private final List<SendEmailData> dealsForDate;
    
    public SendemailpopupController(List<SendEmailData> dealsForDate) {
        this.dealsForDate = dealsForDate;
    }
    
    //Initializes the controller class
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sentPane.setVisible(false);
        sendButton.setOnAction((ActionEvent event) -> {
            Stage stage = (Stage) sentPane.getScene().getWindow();
            stage.setOnCloseRequest((WindowEvent event1) -> {
                event1.consume();
            });
            sendButton.setDisable(true);
            sendButton.setText("Sending...");
            new Thread() {
                
                @Override
                public void run() {
                    try {
                        dealsForDate.forEach(supplier -> {
                            EmailUtil.send(supplier.to, supplier.title,
                                    supplier.text, supplier.attachment); 
                        });
                    }
                    finally {
                        Platform.runLater(() -> {
                            sendConfirmationPane.setVisible(false);
                            sentPane.setVisible(true);
                            stage.setOnCloseRequest(null);
                        });
                    }
                }
            }.start();
        });
        closeButton.setOnAction((ActionEvent event) -> {
            Stage stage = (Stage) sentPane.getScene().getWindow();
            stage.close();
        });
        for (SendEmailData buyer : dealsForDate) {
            bayersPane.getChildren().add(new Label(buyer.getTo()));
        }
    }
}
