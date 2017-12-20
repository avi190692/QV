package com.quickveggies.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import com.quickveggies.Main;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

abstract public class AbstractFreshEntryController {

    public void initialize(URL location, ResourceBundle resources, Button button) {
        button.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                final Stage addTransaction = new Stage();
                addTransaction.centerOnScreen();
                addTransaction.setTitle("Fresh Single Entry System");
                addTransaction.initModality(Modality.APPLICATION_MODAL);
                addTransaction.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    public void handle(WindowEvent event) {
                        Main.getStage().getScene().getRoot().setEffect(null);
                    }
                });
                try {
                	FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/freshentry.fxml"));
                	final FreshEntryController controller=new FreshEntryController(FreshEntryController.REGULAR);
                	loader.setController(controller);
                    Parent parent = loader.load();
                    Scene scene = new Scene(parent, 757, 500);
                    scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
                        public void handle(KeyEvent event) {
                         if (event.getCode() == KeyCode.ESCAPE) {
                                Main.getStage().getScene().getRoot().setEffect(null);
                                addTransaction.close();
                            }
                         controller.keyPressed(event.getCode());
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
    
    public static ArrayList<Node> getAllNodes(Parent parent) {
        ArrayList<Node> nodes = new ArrayList<Node>();
        for (Node node : parent.getChildrenUnmodifiable()) {
            nodes.add(node);
        }
        return nodes;
   }
}
