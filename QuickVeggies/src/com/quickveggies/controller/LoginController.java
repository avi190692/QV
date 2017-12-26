package com.quickveggies.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import com.quickveggies.misc.CryptDataHandler;
import com.quickveggies.controller.CustomDialogController;
import com.quickveggies.controller.LoginController;
import com.quickveggies.controller.SessionDataController;
import com.quickveggies.CosmeticStyles;
import com.quickveggies.GeneralMethods;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.Main;
import com.quickveggies.UserGlobalParameters;
import com.quickveggies.entities.User;

import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
	
	ObservableList<String> employeeTypeList = FXCollections.observableArrayList("Accountant","Admin","Analysts");

    @FXML
    private PasswordField pass;

    @FXML
    private TextField username;

    @FXML
    private Button loginButton;

    @FXML
    private Button register;
    
    @FXML
    private TextField sqlserver;
    
    @FXML
    private TextField sqluser;
    
    @FXML
    private TextField sqlpw;
    
  //## added by ss
    @FXML
    private ComboBox<String> usertype;
    
    

    public void initialize(URL location, ResourceBundle resources) {
        CosmeticStyles.addHoverEffect(register,loginButton);
        usertype.setValue("Admin");
        usertype.setItems(employeeTypeList);
    
        register.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent event) {
                    new Main().replaceSceneContent("/fxml/register.fxml");
                }
            });
        
        	
        	
        	
        	
        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (!username.getText().equals("") && !pass.getText().equals("")) {
                		
                	DatabaseClient dbclient=DatabaseClient.getInstance(); 
                	
                	 try {
                		 String userType = usertype.getValue();
                         User user = dbclient.getUserByName(username.getText());
                         String password = pass.getText();
                         if (password.equals(CryptDataHandler.getInstance().decrypt(user.getPassword())) && user.getUsertype().equals(userType) && user.isBool_status()==true)
                         {
                             SessionDataController.getInstance().setCurrentUser(user);
                             
                             new Main().replaceSceneContent("/fxml/dashboardz.fxml");
                         } 
                         else 
                         {
                             throw new NoSuchElementException();
                         }

                 } catch (SQLException e) {
                     e.printStackTrace();
                 } catch (NoSuchElementException e){
                     Alert alert = new Alert(Alert.AlertType.WARNING);
                     alert.setTitle("Error!");
                     alert.setHeaderText(null);
                     alert.setContentText("Error!Wrong username or password or the user is blocked!");
                     alert.showAndWait();
                 }
                }else GeneralMethods.errorMsg("Not all fields were properly filled!");

            }
        });
    }
}
