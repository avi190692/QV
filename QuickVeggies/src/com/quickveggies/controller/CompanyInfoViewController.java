package com.quickveggies.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import com.quickveggies.Main;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Company;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class CompanyInfoViewController implements Initializable {
	
	@FXML
	private Pane imagePane;
	

	@FXML
	private TextField txtCompanyName;

	@FXML
	private TextField txtAddress;

	@FXML
	private TextField txtWebsite;

	@FXML
	private TextField txtPhone;

	@FXML 
	private TextField txtEmail;

	@FXML
	private TextField txtIndustryType;

	@FXML
	private PasswordField pwdOld;

	@FXML
	private ScrollPane paneProducts;
	
	@FXML
	private Button btnAddCompany;
	
	@FXML
	private Button btnEditCompany;
	
	@FXML
	private  ImageView imgLogo;

	final static SessionDataController session = SessionDataController.getInstance();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		final DatabaseClient dbc = DatabaseClient.getInstance();
		final Company c = dbc.getCompany();
		if (c != null) {
			txtAddress.setText(c.getAddress());
			txtCompanyName.setText(c.getName());
			txtEmail.setText(c.getEmail());
			txtIndustryType.setText(c.getIndustryType());
			txtPhone.setText(c.getPhone());
			txtWebsite.setText(c.getWebsite());
			pwdOld.setText(c.getPassword());
			if (c.getLogo() != null) {
				try {
					int ht = (int) imagePane.getPrefHeight();
					int wt = (int) imagePane.getPrefWidth();
					WritableImage img = new WritableImage(wt ,ht);
		            BufferedImage read = ImageIO.read(c.getLogo());
		            img = SwingFXUtils.toFXImage(read, null);
		            imagePane.setStyle("-fx-border-color: none;");
		            imgLogo.setFitHeight(ht);
		            imgLogo.setFitWidth(wt);
		            imgLogo.setImage(img);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			btnAddCompany.setVisible(false);
			btnEditCompany.setVisible(true);
		} else {
			btnAddCompany.setVisible(true);
			btnEditCompany.setVisible(false);
		}
		btnEditCompany.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if ( c == null) {
					return;
				}
				System.out.println(c.getLogo());
				handleAddEditButton("/fxml/companyedit.fxml", "Edit Company Info", true, c);;
			}
		});
		
		btnAddCompany.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
				handleAddEditButton("/fxml/companyadd.fxml", "Add Company Info", false, null);;
			}
		});
		
	}

	private void handleAddEditButton(String resource, String title, boolean editMode, Company company) {
		try {
			final Stage compInfoStage = new Stage();
			compInfoStage.centerOnScreen();
			compInfoStage.setTitle(title);
			compInfoStage.initModality(Modality.APPLICATION_MODAL);
			compInfoStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent event) {
					Main.getStage().getScene().getRoot().setEffect(null);
				}
			});
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
				
				Scene scene = new Scene((Parent) loader.load());
				if (editMode) {
					CompanyInfoEditController comController = loader.getController();
					comController.setCompanyToEdit(company);
					comController.setImage(imgLogo.getImage());
				}
				scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
					public void handle(KeyEvent event) {
						if (event.getCode() == KeyCode.ESCAPE) {
							Main.getStage().getScene().getRoot().setEffect(null);
							compInfoStage.close();
						}
					}
				});
				EventHandler<WindowEvent> expenseWindowEvent = new EventHandler<WindowEvent>() {
					public void handle(WindowEvent event) {
						try {
							ScrollPane paneProducts = (ScrollPane) session.getSettingPagePane().getChildren().get(1);
							VBox content = (VBox) paneProducts.getContent();
							content.getChildren().set(3,
									(Node) FXMLLoader.load(getClass().getResource("/fxml/companyviewer.fxml")));

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				compInfoStage.setOnCloseRequest(expenseWindowEvent);
				compInfoStage.setOnHidden(expenseWindowEvent);
				compInfoStage.setScene(scene);
				compInfoStage.show();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
