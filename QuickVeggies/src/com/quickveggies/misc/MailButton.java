package com.quickveggies.misc;

import java.io.IOException;
import java.sql.SQLException;

import com.quickveggies.Main;
import com.quickveggies.controller.popup.EnteremailpaneController;
import com.quickveggies.dao.DatabaseClient;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

public class MailButton extends Button{
	public MailButton(final String mailReceiver,final String mailSender){
	    setPrefSize(30, 30);
		BackgroundImage backgroundImage = new BackgroundImage( new Image( getClass().getResource("/icons/env2.png").toExternalForm(),30,30,true,true), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
	    Background background = new Background(backgroundImage);
	    setBackground(background);
		
		setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {            	
            	//OPEN MAIL WINDOW
                final Stage addTransaction = new Stage();
                addTransaction.centerOnScreen();
                addTransaction.setTitle("New email");
                addTransaction.initModality(Modality.APPLICATION_MODAL);
                try {
                	FXMLLoader loader=new FXMLLoader(getClass().getResource("/mailwindow.fxml"));
                	EnteremailpaneController controller=new EnteremailpaneController(mailReceiver,mailSender, null);
                	loader.setController(controller);
                    Parent parent = loader.load();
                    Scene scene = new Scene(parent);
                    scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
                        public void handle(KeyEvent event) {
                         if (event.getCode() == KeyCode.ESCAPE) {
                                Main.getStage().getScene().getRoot().setEffect(null);
                                addTransaction.close();
                            }
                        }
                    });
                    addTransaction.setScene(scene);
                    addTransaction.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
           }
        });
		
	}
	
	
}
