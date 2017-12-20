package com.quickveggies.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import com.quickveggies.GeneralMethods;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Company;
import com.quickveggies.misc.BoundedNumericTextField;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class CompanyInfoEditController implements Initializable {

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
	private PasswordField pwdOld;

	@FXML
	private PasswordField pwdNew;

	@FXML
	private PasswordField pwdConfirm;

	@FXML
	private  ImageView imgLogo;

	@FXML
	private  Button btnSave;

	@FXML
	private  Button btnUpload;

	@FXML
	private Pane imagePanel;
	
	private File imgFile;
	
	private Company company;

	String EMPTY_STR = "";

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
				if (pwdConfirm.getText() != null & pwdConfirm.getText().trim().isEmpty()) {
					pwdNew.setText(EMPTY_STR);
				}
				saveCompanyInfo(false);
				btnSave.getScene().getWindow().hide();
			}
		});
	}
	
	private boolean arePasswordsValid() {
		String existingP = company.getPassword() == null ? EMPTY_STR : company.getPassword();
    	String oldP = pwdOld.getText() == null ? EMPTY_STR : pwdOld.getText().trim();
    	String newP = pwdNew.getText() == null ? EMPTY_STR : pwdNew.getText().trim();
    	String confP = pwdConfirm.getText() == null ? EMPTY_STR : pwdConfirm.getText().trim();
    	if (oldP.isEmpty() && newP.isEmpty() && confP.isEmpty()) {
			pwdNew.setText(company.getPassword());
    		return true;
    	}
		if (!existingP.equals(oldP)) {
				GeneralMethods.errorMsg("Old password provided doesn't match with existing password in system");
				return false;
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
		         ("Image Files", "*.png", "*.jpg","*.jpeg", "*.gif"));
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

	public void setCompanyToEdit(Company c) {
		if (txtCompanyName != null && c != null) {
			txtAddress.setText(c.getAddress());
			txtCompanyName.setText(c.getName());
			txtEmail.setText(c.getEmail());
			txtIndustryType.setText(c.getIndustryType());
			txtPhone.setText(c.getPhone());
			txtWebsite.setText(c.getWebsite());
	}
		this.company = c;

	}
	
	public void setImage(Image image) {
		if (image != null && imgLogo != null) {
			imgLogo.setImage(image);
			imagePanel.setStyle("-fx-border-color: none;");
		}
	}

}
