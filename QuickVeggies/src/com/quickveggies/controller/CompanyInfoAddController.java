package com.quickveggies.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

import com.quickveggies.GeneralMethods;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Company;
import com.quickveggies.misc.BoundedNumericTextField;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class CompanyInfoAddController implements Initializable {

	@FXML
	private TextField txtCompanyName;

	@FXML
	private TextField txtAddress;

	@FXML
	private TextField txtWebsite;

	@FXML
	private BoundedNumericTextField txtPhone;

	@FXML
	private TextField txtEmail;

	@FXML
	private TextField txtIndustryType;

	@FXML
	private PasswordField pwdNew;

	@FXML
	private PasswordField pwdConfirm;

	@FXML
	private ImageView imgLogo;

	@FXML
	private Button btnSave;

	@FXML
	private Button btnUpload;
	
	@FXML
	private Pane imagePanel;
	
	private File imgFile;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		txtPhone.setMaxLength(15);
		btnUpload.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				uploadImage();
			}
		});
		
		btnSave.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				if (!arePasswordsValid()) return;
				saveCompanyInfo(true);
				btnSave.getScene().getWindow().hide();
			}
		});
	}
	
	private boolean arePasswordsValid() {
		String EMPTY_STR = "";
    	String newP = pwdNew.getText() == null ? EMPTY_STR : pwdNew.getText().trim();
    	String confP = pwdConfirm.getText() == null ? EMPTY_STR : pwdConfirm.getText().trim();
    	if (newP.isEmpty() && confP.isEmpty()) {
    		return true;
    	}
		if (!newP.equals(confP)) {
			GeneralMethods.errorMsg("New password doesn't match with confirm password");
			return false;
		}
		return true;
	}

	
    private void saveCompanyInfo(boolean isNew) {
    	Company c = new Company();
    	c.setAddress(txtAddress.getText());
    	c.setEmail(txtEmail.getText());
    	c.setIndustryType(txtIndustryType.getText());
    	if (imgFile != null && imgFile.exists())
			try {
				c.setLogo(new FileInputStream(imgFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
    	c.setName(txtCompanyName.getText());
    	c.setPassword(pwdNew.getText());
    	c.setPhone(txtPhone.getText());
    	c.setWebsite(txtWebsite.getText());
    	DatabaseClient dbc = DatabaseClient.getInstance();
    	if (isNew) {
        	dbc.addCompany(c);
    	} else {
    		dbc.updateCompany(c);
    	}
    }
    
    private void uploadImage() {
		Stage mainStage = new Stage();
		FileChooser fileChooser = new FileChooser();
		 fileChooser.setTitle("Open Resource File");
		 fileChooser.getExtensionFilters().addAll( new ExtensionFilter
		         ("Image Files", "*.png", "*.jpg", "*.gif", "*.bmp"));
		 File selectedFile = fileChooser.showOpenDialog(mainStage);
		 if (selectedFile != null) {
		    try (InputStream is = new FileInputStream(selectedFile)){
		    	imgFile = selectedFile;
		    	imgLogo.setImage(new Image(is));
		    	imagePanel.setStyle("-fx-border-color: none;");
			} catch (FileNotFoundException e) {
				GeneralMethods.errorMsg("Cannot find specified file:" + selectedFile.getName());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		 }
    	
    }

}
