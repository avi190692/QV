package com.quickveggies.controller.popup;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.LinkedHashMap;
import java.util.Map;

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

import org.apache.commons.lang.text.StrSubstitutor;

import com.ai_int.utils.SMSUtil;
import com.quickveggies.UserGlobalParameters;

/**
 * FXML Controller class
 *
 * @author Sergey Orlov <serg.merlin@gmail.com>
 */
public class SendsmspopupController implements Initializable {
    
    @FXML
    AnchorPane sendConfirmationPane;
    
    @FXML
    AnchorPane sentPane;
    
    @FXML
    VBox bayersPane;
    
    @FXML
    Button sendSmsButton;
    
    @FXML
    Button closeButton;
    
    private final List<SendSmsBuyer> dealsForDate;
    
    public SendsmspopupController(List<SendSmsBuyer> dealsForDate) {
        this.dealsForDate = dealsForDate;
    }
    
    //Initializes the controller class
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sentPane.setVisible(false);
        sendSmsButton.setOnAction((ActionEvent event) -> {
            Stage stage = (Stage) sentPane.getScene().getWindow();
            stage.setOnCloseRequest((WindowEvent event1) -> {
                event1.consume();
            });
            sendSmsButton.setDisable(true);
            sendSmsButton.setText("Sending...");
            new Thread() {
                
                @Override
                public void run() {
                    try {
                        dealsForDate.forEach(buyer -> {
                            String sms = buildSMS(buyer.getFirstName() + " " + buyer.getLastName(),
                                    buyer.getDate(), buyer.getAggregatedAmount());
                            SMSUtil.sendMessage(sms, buyer.getMobileNumber());
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
        for (SendSmsBuyer buyer : dealsForDate) {
            bayersPane.getChildren().add(new Label(buyer.getDate()));
            bayersPane.getChildren().add(new Label(buyer.getFirstName() + " " + buyer.getLastName()));
        }
    }    
    
    public static String buildSMS(String partyName, String date, String amt) {
        Map<String, String> substituteMap = new LinkedHashMap<>();
        substituteMap.put("PartyName", partyName);
        substituteMap.put("Date", date);
        substituteMap.put("TotalAmt", amt);
        StrSubstitutor strSub = new StrSubstitutor(substituteMap);
        
        return strSub.replace(UserGlobalParameters.GET_SMS_TEMPLATE());
    }
    
    public static class SendSmsBuyer {
        
        private String firstName;
        private String lastName;
        private String date;
        private String aggregatedAmount;
        private String mobileNumber;
        
        private SendSmsBuyer() {
        }
        
        public static SendSmsBuyer buildSendSmsBuyer (String firstName, String lastName, String date,
                String aggregatedAmount, String mobileNumber) {
            SendSmsBuyer buyer = new SendSmsBuyer();
            buyer.firstName = firstName;
            buyer.lastName = lastName;
            buyer.date = date;
            buyer.aggregatedAmount = aggregatedAmount;
            buyer.mobileNumber = mobileNumber;
            return buyer;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getDate() {
            return date;
        }

        public String getAggregatedAmount() {
            return aggregatedAmount;
        }

        public String getMobileNumber() {
            return mobileNumber;
        }
    }
}
