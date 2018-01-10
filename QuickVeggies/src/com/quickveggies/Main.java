package com.quickveggies;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import com.quickveggies.controller.dashboard.DBuyerController;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    private static final boolean DEBUG_MODE = false; //Todo: =false
    private static final int DASHBOARD_WIDTH = 792;
    private static final int DASHBOARD_HEIGHT = 660;
    private static File file = new File("output.txt");
    private static final String SMSTemplate = "Dear ${PartyName} your purchases for date ${Date} is valued ${TotalAmt} ."
            + "Pay within 3 days and avail a flat 1% cashback.";

    private QVCustomOutputStream qOs;
    private static Stage stage;

    public Main() {
        UserGlobalParameters.SET_SMS_TEMPLATE(SMSTemplate, false);
        try {
            qOs = new QVCustomOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            //TODO:uncomment below to move logging info to file instead of standard output
            // System.setOut(qOs);
            // System.setErr(qOs);
        } catch (FileNotFoundException e) {
            System.out.println("Error creating file");
        }
    }
   

    public Parent replaceSceneContent(String fxml) {
        Parent page = null;
        try {
            page = FXMLLoader.load(getClass().getResource(fxml));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene;
        if (!fxml.contains("register") && !fxml.contains("login")) {
            scene = new Scene(page, DASHBOARD_WIDTH, DASHBOARD_HEIGHT);
        } else {
            scene = new Scene(page);
        }
        scene.getStylesheets().add("/css/style.css");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Quick Veggies");
        stage.setFullScreen(true);
        stage.show();
       // stage.centerOnScreen();
       
        return page;
    }
    
    //## added by ss on 07Jan2018
    //## new window scene replacement.
    public Parent newWindowSceneReplacementContecnt(String fxml)
    {
    	Parent parent = null;
    	
    	final Stage accoutCodeCreation = new Stage();
		accoutCodeCreation.centerOnScreen();
		accoutCodeCreation.setTitle("Account Code Creation");
		accoutCodeCreation.initModality(Modality.APPLICATION_MODAL);
		accoutCodeCreation.setOnCloseRequest(new EventHandler<WindowEvent>() 
        {
            public void handle(WindowEvent event) 
            {
                Main.getStage().getScene().getRoot().setEffect(null);
            }
        });
		try 
		{
	            parent = FXMLLoader.load(DBuyerController.class.getResource(fxml));
	            Scene scene = new Scene(parent, 687, 400);
	            scene.setOnKeyPressed(new EventHandler<KeyEvent>() 
	            {
	                public void handle(KeyEvent event)
	                {
	                    if (event.getCode() == KeyCode.ESCAPE) 
	                    {
	                        Main.getStage().getScene().getRoot().setEffect(null);
	                        accoutCodeCreation.close();
	                    }
	                }
	            });
	            accoutCodeCreation.setScene(scene);
	            accoutCodeCreation.show();
	      } 
		catch (IOException e){
	            e.printStackTrace();}

    	
    	return parent;
    }
    
    

    public static void closeCurrentStage(Stage stage) {
        stage.close();
    }

    public static Stage getStage() {
        return stage;
    }

    //## main method to start the application
    public static void main(String[] args) {
        launch(args);
    }
    
    // ## implemented method from Application class
    @Override
    public void start(Stage primaryStage) throws Exception {
        
    	stage = primaryStage;
        stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/icons/program_icon.png")));
        String whatToLoad = "/fxml/login.fxml";
        if (DEBUG_MODE) 
        {
            //Todo: debug mode only
            whatToLoad = "/fxml/dashboardz.fxml";
        }
        Parent root = FXMLLoader.load(getClass().getResource(whatToLoad));
        stage.setTitle("Accounting System Login");
        stage.setScene(new Scene(root, DASHBOARD_WIDTH, DASHBOARD_HEIGHT));
        stage.setMinWidth(stage.getScene().getWidth());
        stage.setMinHeight(stage.getScene().getHeight());
        //## added by ss
        stage.setFullScreen(true);
        stage.show();
    }
}
