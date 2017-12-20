package com.quickveggies.controller;

import com.quickveggies.PaymentMethodSource;
import com.quickveggies.UserGlobalParameters;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Buyer;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

public class EditBuyerController implements Initializable {

    @FXML
    private TextField title;

    @FXML
    private TextField firstName;

    @FXML
    private TextField lastName;

    @FXML
    private TextField company;

    @FXML
    private TextField proprietor;

    @FXML
    private TextField mobile;

    @FXML
    private TextField mobile2;

    @FXML
    private TextField email;

    @FXML
    private TextField shop;

    @FXML
    private TextField city;

    @FXML
    private TextField email2;

    @FXML
    private ComboBox<String> paymentMethod;

    @FXML
    private ChoiceBox<String> creditPeriod;

    @FXML
    private CheckBox hasGuarantor;

    @FXML
    private TextField guarantorName;

    @FXML
    private Button save;

    @FXML
    private Button delete;

    private Buyer buyerToEdit = null;

    private DatabaseClient dbclient = DatabaseClient.getInstance();

    private static final String[] creditPeriodSource = UserGlobalParameters.creditPeriodSource;


    /* 
     * The values from buyer object returns integer number for payment methods, so this
     * map is used to keep the number and name of payment method 
     */
    private static Map<Integer, PaymentMethodSource> buyerPayMethodMap = UserGlobalParameters.getPaymentMethodMap();

    public EditBuyerController(String buyerTitle) {
        //find the buyer in the database
        try {
            buyerToEdit = dbclient.getBuyerByName(buyerTitle);
        }
        catch (SQLException e) {
            System.out.print("sqlexception in EditBuyerController");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        paymentMethod.setItems(FXCollections.observableArrayList(PaymentMethodSource.getValueList()));
        creditPeriod.setItems(FXCollections.observableArrayList(creditPeriodSource));
        title.setText(buyerToEdit.getTitle());
        firstName.setText(buyerToEdit.getFirstName());
        lastName.setText(buyerToEdit.getLastName());
        mobile2.setText(buyerToEdit.getMobile2());
        company.setText(buyerToEdit.getCompany());
        proprietor.setText(buyerToEdit.getProprietor());
        mobile.setText(buyerToEdit.getMobile());
        email.setText(buyerToEdit.getEmail());
        email2.setText(buyerToEdit.getEmail2());
        shop.setText(buyerToEdit.getShopno());
        city.setText(buyerToEdit.getCity());
        paymentMethod.setValue(buyerPayMethodMap.get(buyerToEdit.getPaymentMethod()).toString());
        creditPeriod.setValue(buyerToEdit.getCreditPeriod());
        prepareGuarantorCheckBox();

        save.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DatabaseClient.getInstance().updateTableEntry("buyers1", buyerToEdit.getId(),
                        new String[]{"title", "firstName", "lastName", "company", "proprietor", "mobile", "mobile2", "email", "email2", "shopno", "city"},
                        new String[]{title.getText(), firstName.getText(), lastName.getText(), company.getText(), proprietor.getText(), mobile.getText(),
                            mobile2.getText(), email.getText(), email2.getText(), shop.getText(), city.getText()},
                         false);
                save.getScene().getWindow().hide();
            }
        });

//        delete.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//               DatabaseClient.getInstance().deleteTableEntry("buyers1", "title", buyerToEdit.getTitle());
//               delete.getScene().getWindow().hide();
//            }
//        });
    }

    /* Helper method to set properties for guaranteer check box */
    private void prepareGuarantorCheckBox() {
        hasGuarantor.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (hasGuarantor.isSelected()) {
                    guarantorName.setDisable(false);
                }

            }
        });
    }

}
