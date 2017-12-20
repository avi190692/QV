package com.quickveggies.controller.popup;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PagePanel;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

import javax.swing.SwingUtilities;

/**
 *
 * @author Sergey Orlov <serg.merlin@gmail.com>
 */
public class AuditLogEntryPopupController implements Initializable {
    
    @FXML
    AnchorPane basePane;
    
    @FXML
    ScrollPane previewPane;

    @FXML
    Button backButton;
    
    @FXML
    Button beforeEditButton;
    
    @FXML
    Button afterEditButton;
    
    @FXML
    Label dateLabel;

    @FXML
    Label descriptionLabel;
    
    private final String fileName, oldValuesFileName;
    private SwingNode swingNode;
    private String eventDate, description;
    
    public AuditLogEntryPopupController(String fileName, String oldValuesFileName) {
        this.fileName = fileName;
        this.oldValuesFileName = oldValuesFileName;
        this.eventDate = "";
        this.description = "";
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        swingNode = new SwingNode();
        previewPane.setContent(swingNode);
        backButton.setOnAction((ActionEvent event) -> {
            previewPane.getScene().getWindow().hide();
        });
        if (fileName != null) {
            updatePreview(fileName);
        }
        beforeEditButton.setVisible(oldValuesFileName != null);
        afterEditButton.setVisible(oldValuesFileName != null);
        beforeEditButton.setOnAction((ActionEvent event) -> {
            if (oldValuesFileName != null) {
                updatePreview(oldValuesFileName);
            }
        });
        afterEditButton.setOnAction((ActionEvent event) -> {
            if (fileName != null) {
                updatePreview(fileName);
            }
        });
        Platform.runLater(() -> {
            dateLabel.setText(eventDate);
            descriptionLabel.setText(description);
            basePane.getScene().getWindow().setWidth(1024);
            basePane.getScene().getWindow().setHeight(640);
            basePane.setPrefSize(1024, 640);
            basePane.getScene().getWindow().requestFocus();
        });
    }
    
    private void updatePreview(String fileName) {
        SwingUtilities.invokeLater(() -> {
            final PagePanel panel = new PagePanel();
            //load a pdf from a byte buffer
            try {
                FileInputStream fileInputStream = new FileInputStream(fileName);
                byte[] pdfContent = new byte[fileInputStream.available()];
                fileInputStream.read(pdfContent, 0, fileInputStream.available());
                ByteBuffer buffer = ByteBuffer.wrap(pdfContent);
                PDFFile pdfFile = new PDFFile(buffer);
                // show the first page
                PDFPage page = pdfFile.getPage(0, true);
                panel.setSize((int)previewPane.getWidth(), (int)previewPane.getHeight());
                panel.showPage(page);
                Platform.runLater(() -> {
                    swingNode.requestFocus();
                });
            }
            catch (IOException ex) {
                Logger.getLogger(PrintpopupController.class.getName()).log(Level.SEVERE, null, ex);
            }
            //Swing Panel to JavaFX Node
            swingNode.setContent(panel);
        });
        Platform.runLater(() -> {
            dateLabel.setText(eventDate);
            descriptionLabel.setText(description);
        });
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventData(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
