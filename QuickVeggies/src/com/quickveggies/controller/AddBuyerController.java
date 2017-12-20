package com.quickveggies.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.quickveggies.GeneralMethods;
import com.quickveggies.PaymentMethodSource;
import com.quickveggies.UserGlobalParameters;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Buyer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.stage.FileChooser.ExtensionFilter;

public class AddBuyerController implements Initializable {

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
    private Button create;
    
    @FXML
    private ComboBox<String> ladaanBijak;
    
    @FXML
    private ImageView imvPhoto;
    
    @FXML
    private Button btnUploadPhoto;
    
    @FXML
    private Pane imagePanel;
    
    private File imgFile;
    
    private ObservableList<String> buyerTypes=FXCollections.observableArrayList();
    
    private static final String[] creditPeriodSource = UserGlobalParameters.creditPeriodSource;
    
    
	String[] buyerTypesArr = UserGlobalParameters.buyerTypes;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	for (String bt : buyerTypesArr) {
    		buyerTypes.add(bt);
    	}
    	
    	btnUploadPhoto.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				uploadImage(event);
			}
		});
   
    	
    	paymentMethod.setItems( FXCollections.observableArrayList(PaymentMethodSource.getValueList()));
    	creditPeriod.setItems(FXCollections.observableArrayList(creditPeriodSource));
    	paymentMethod.setValue(PaymentMethodSource.Cash.toString());
    	creditPeriod.setValue(creditPeriodSource[0]);

    	ladaanBijak.setItems(buyerTypes);
    	ladaanBijak.setValue("Regular");
        create.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	try{
            		Long.valueOf(mobile.getText());
            	}catch(NumberFormatException e){
            		GeneralMethods.errorMsg("Please enter appropriate mobile number!");
            		return;
            	}
            	if (mobile2.getText() != null && !mobile2.getText().trim().isEmpty()) {
            		try {
                		Long.valueOf(mobile2.getText());
                		
                	}catch(NumberFormatException e1){ 
                		GeneralMethods.errorMsg("Please enter appropriate mobile 2 number, or leave it empty!");
                		return;
                	}
            	}
            	if(!email.getText().matches(GeneralMethods.emailPattern)){
            		GeneralMethods.errorMsg("Email provided is not property formatted!");
            		return;
            	}
            	prepareGuarantorCheckBox();
            	Integer key = 1;
            	for (Integer i : UserGlobalParameters.getPaymentMethodMap().keySet() ) {
            		if (paymentMethod.getValue().equals(UserGlobalParameters.getPaymentMethodMap().get(i))) {
            			key = i;
            			break;
            		}
            		System.out.println("INFO:The payment type in combo box is not associated with any of the predefined payment types, using default type!");
            	}
                Buyer buyer = new Buyer(0, "", firstName.getText(), lastName.getText(),
                        company.getText(), proprietor.getText(), mobile.getText(),
                        mobile2.getText(), email.getText(), shop.getText(), city.getText(),
                        email2.getText(),
                        company.getText(), key, //change to Payment Method get value.
                        (String) creditPeriod.getValue(),ladaanBijak.getValue());
                if (imgFile  != null) {
                	try {
                		buyer.setImageStream(new BufferedInputStream(new FileInputStream(imgFile)));
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
                }
                try {
                    DatabaseClient.getInstance().saveBuyer(buyer);
                    create.getScene().getWindow().hide();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                
            }
        });
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
    
    public void uploadImage(final Event event) {
    	if (!(event.getSource() instanceof Node)) 
    		throw new IllegalArgumentException("The source of the event should be instance of java FX node or any of its' subclass");
 		Window mainStage =  ((Node) event.getSource()).getScene().getWindow();
 		FileChooser fileChooser = new FileChooser();
 		 fileChooser.setTitle("Open Resource File");
 		 fileChooser.getExtensionFilters().addAll( new ExtensionFilter
 		         ("Image Files", "*.png", "*.jpg", "*.gif", "*.bmp"));
 		 File selectedFile = fileChooser.showOpenDialog(mainStage);
 		 if (selectedFile != null) {
 		    try (InputStream is = new FileInputStream(selectedFile)){
 		    	imgFile = selectedFile;
 		    	imvPhoto.setImage(new Image(is));
 		    	imagePanel.setStyle("-fx-border-color: none;");
 			} catch (FileNotFoundException e) {
 				GeneralMethods.errorMsg("Cannot find specified file:" + selectedFile.getName());
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 		 }
     	
     }

}
