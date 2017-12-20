package com.quickveggies.misc;

import java.io.IOException;

import com.quickveggies.Main;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Event handler for add and edit button of fruits settings GUI
 * 
 * @author Shoeb
 *
 */
public class FruitButtonEventHandler implements EventHandler<ActionEvent> {

	private String resource;
	private String title;
	private Pane settingsPane;

	public FruitButtonEventHandler(String resource, String title, Pane settingsPane) {
		this.resource = resource;
		this.title = title;
		this.settingsPane = settingsPane;
	}

	@Override
	public void handle(ActionEvent event) {
		if (this.resource == null) {
			new IllegalStateException("Please specify valid resource to be loaded");
		}
		// Main.getStage().getScene().getRoot().setEffect(new GaussianBlur());
		final Stage fruitWindow = new Stage();
		fruitWindow.centerOnScreen();
		fruitWindow.setTitle(title);
		fruitWindow.initModality(Modality.APPLICATION_MODAL);
		fruitWindow.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent event) {
				Main.getStage().getScene().getRoot().setEffect(null);
			}
		});
		try {
			Parent parent = FXMLLoader.load(getClass().getResource(resource));
			Scene scene = new Scene(parent);
			scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
				public void handle(KeyEvent event) {
					if (event.getCode() == KeyCode.ESCAPE) {
						Main.getStage().getScene().getRoot().setEffect(null);
						fruitWindow.close();
					}
				}
			});
			EventHandler<WindowEvent> fruitAddWindowEvent = new EventHandler<WindowEvent>() {
				public void handle(WindowEvent event) {
					try {
				   		ScrollPane paneProducts = (ScrollPane) settingsPane.getChildren().get(1);
				   		VBox content = (VBox) paneProducts.getContent();
			    		content.getChildren().set(0, (Node) FXMLLoader.load(getClass().getResource("/fruitviewer.fxml")));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			fruitWindow.setOnCloseRequest(fruitAddWindowEvent);
			fruitWindow.setOnHidden(fruitAddWindowEvent);
			fruitWindow.setScene(scene);
			fruitWindow.show();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
