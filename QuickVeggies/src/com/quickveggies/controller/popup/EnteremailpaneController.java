package com.quickveggies.controller.popup;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.ai_int.utils.FileUtil;
import com.quickveggies.Main;
import com.quickveggies.controller.dashboard.DashboardController;

public class EnteremailpaneController implements Initializable {

	@FXML
	private TextField sendTo;
	@FXML
	private TextField sendFrom;
	@FXML
	private TextField titleField;
	@FXML
	private TextArea msgField;
	@FXML
	private Button btnRemoveAttachment;
	@FXML
	private Button btnAddAttachment;
	@FXML
	private Label lblAttach;

	private final String to;
	private final String from;
        
	private String attachment;

	public EnteremailpaneController(String to, String from, String attachment) {
		this.to = to;
		this.from = from;
		this.attachment = attachment;
	}
        
        public static void showMailWindow(String to, String from, String attachment) {
            final Stage addTransaction = new Stage();
            addTransaction.centerOnScreen();
            addTransaction.setTitle("New email");
            addTransaction.initModality(Modality.APPLICATION_MODAL);
            try {
                FXMLLoader loader = new FXMLLoader(EnteremailpaneController.class.getResource("/fxml/enteremailpane.fxml"));
                EnteremailpaneController controller = new EnteremailpaneController(to, from, attachment);
                loader.setController(controller);
                Parent parent = loader.load();
                Scene scene = new Scene(parent);
                scene.setOnKeyPressed((KeyEvent event) -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        Main.getStage().getScene().getRoot().setEffect(null);
                        addTransaction.close();
                    }
                });
                addTransaction.setScene(scene);
                addTransaction.show();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    //Todo: Delete the file here, because this file has been auto generated. The file in the EnteremailpaneController can be one of the file selected by user, so we don't delete it there.
                    //if (attachment != null)
                    //new File(attachment).delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		sendTo.setText(to);
		sendTo.setEditable(false);
		sendFrom.setText(from);
		sendFrom.setEditable(false);
		if (attachment != null && !attachment.trim().isEmpty()) {
			File file = new File(attachment);
			lblAttach.setText(file.getName());
		}
		msgField.setPrefRowCount(30);
		btnAddAttachment.setOnAction((event) -> {
			File file = FileUtil.getFileToOpen(btnAddAttachment.getScene(), "Add attachment", FileUtil.getPdfExtMap());
			if (file != null) {
				this.attachment = file.getAbsolutePath();
				lblAttach.setText(file.getName());
			}
		});
		btnRemoveAttachment.setOnAction((event) -> {
			this.attachment = null;
			lblAttach.setText("");
		});
	}
        
        public SendEmailData getEmailData() {
            return SendEmailData.buildSendEmailData(sendTo.getText(), sendFrom.getText(),
                    titleField.getText(), msgField.getText(), attachment);
        }
}
