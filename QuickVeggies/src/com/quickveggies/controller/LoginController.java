package com.quickveggies.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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

    public void initialize(URL location, ResourceBundle resources) {
        CosmeticStyles.addHoverEffect(register,loginButton);
        
        
        
        
       /* 
        sqlserver.setText("localhost");  
        final DatabaseClient dbclient=DatabaseClient.getInstance();
        //check if there is already a user logged into the database
        //if so, let the user to unregister and let another user register the program to his name
        	if(dbclient.getRowsNum("users")>0){
        		register.setText("Unregister");
        		register.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {
                    	
               	     //prepare the buttons for the unregister dialog
                    //------------------
               		Stage dialogStage=new Stage();
               		final PasswordField pwdField=new PasswordField();
               		
               	     final Button ok=new Button("OK");
               	     ok.setOnMouseClicked(new EventHandler<MouseEvent>() {
               	         @Override
               	         public void handle(MouseEvent event) {
                         	try{
                                String input=pwdField.getText();
                                String realPwd=CryptDataHandler.getInstance().decrypt(
                                		(dbclient.getUserById(1)).getPassword());
                                if(input.equals(realPwd)){
                                        dbclient.deleteTableEntries("users", "ALL", "ALL", true);
                                        register.setText("Register");
                                        register.setOnAction(new EventHandler<ActionEvent>() {
                                            public void handle(ActionEvent event) {
                                                new Main().replaceSceneContent("/fxml/register.fxml");
                                            }
                                        });
                                }else GeneralMethods.errorMsg("incorrect password!");
                            	ok.getScene().getWindow().hide();
                         	}catch(Exception e){e.printStackTrace();}
               	         }
               	     });
               	     
               	     final Button cancel=new Button("Cancel");
               	     cancel.setOnMouseClicked(new EventHandler<MouseEvent>() {
               	         @Override
               	         public void handle(MouseEvent event) {
                               cancel.getScene().getWindow().hide();
               	         }
               	     });    
               	     ok.setLayoutX(25.0);
               	     ok.setLayoutY(150.0);
               	     cancel.setLayoutX(150.0);
               	     cancel.setLayoutY(150.0);
               	     pwdField.setLayoutX(50.0);
               	     pwdField.setLayoutY(100.0);
               	     
                    	CustomDialogController controller=new CustomDialogController(new Button[]{ok,cancel},pwdField,"Retype login password:");
                    	
                    	GeneralMethods.openNewWindow(LoginController.this, "/fxml/customdialog.fxml", controller, null);
                    	
                    }
                });
        		
        	}else */register.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent event) {
                    new Main().replaceSceneContent("/fxml/register.fxml");
                }
            });
        
        	
        	
        	
        	
        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (!username.getText().equals("") && !pass.getText().equals("")) {
                	
                	//UserGlobalParameters.SQLURL=sqlserver.getText();
                	//UserGlobalParameters.SQLUSER=sqluser.getText();
                	//UserGlobalParameters.SQLPASS=sqlpw.getText();
                	
                	//if(UserGlobalParameters.SQLURL.equals(""))UserGlobalParameters.SQLURL="localhost";
                	//if(UserGlobalParameters.SQLUSER.equals(""))UserGlobalParameters.SQLUSER="qvuser";
                	//if(UserGlobalParameters.SQLPASS.equals(""))UserGlobalParameters.SQLPASS="qvdbusr123";
                	
                	DatabaseClient dbclient=DatabaseClient.getInstance();
                	//if(dbclient==null){GeneralMethods.errorMsg("Wrong SQL server address/login parameters"); return;}
                	
                	 try {
                         User user = dbclient.getUserByName(username.getText());
                         String password = pass.getText();
                         if (password.equals(CryptDataHandler.getInstance().decrypt(user.getPassword()))) {
                             SessionDataController.getInstance().setCurrentUser(user);
                             //add a check to prevent closing the dash with unsaved windows still open
//                             ((Stage)loginButton.getScene().getWindow()).setOnCloseRequest(new EventHandler<WindowEvent>() {
//                            	    @Override
//                            	    public void handle(WindowEvent event) {
//                            	       if(SessionDataController.getInstance().getUnsavedWindows()>0)
//                            	    	   //TODO add here a ok-cancel confirmation dialog
//                            	    	   event.consume();
//                            	    }
//                            	});
                             new Main().replaceSceneContent("/fxml/dashboardz.fxml");
                         } else {
                             throw new NoSuchElementException();
                         }

                 } catch (SQLException e) {
                     e.printStackTrace();
                 } catch (NoSuchElementException e){
                     Alert alert = new Alert(Alert.AlertType.WARNING);
                     alert.setTitle("Error!");
                     alert.setHeaderText(null);
                     alert.setContentText("Wrong login \\ password!");
                     alert.showAndWait();
                 }
                }else GeneralMethods.errorMsg("Not all fields were properly filled!");

            }
        });
    }
}
