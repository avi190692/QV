package com.quickveggies.controller.popup;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.nio.ByteBuffer;

import javax.swing.SwingUtilities;
import javafx.event.ActionEvent;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.stage.WindowEvent;
import javafx.embed.swing.SwingNode;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PagePanel;

import com.ai_int.utils.PrintUtil;

/**
 * FXML Controller class
 *
 * @author Sergey Orlov <serg.merlin@gmail.com>
 */
public class PrintpopupController implements Initializable {

    @FXML
    ScrollPane previewPane;

    @FXML
    Button printButton;
    
    @FXML
    Button nextButton;
    
    @FXML
    Button prevButton;

    @FXML
    Label pagesNumberLabel;

    private final List<String> printFileNames;
    private SwingNode swingNode;
    
    private int pagesNumber = 0;
    private int activeFile = 0;
    private int activePage = 0;

    public PrintpopupController(List<String> printFileNames) {
        this.printFileNames = printFileNames;
    }

    //Initializes the controller class
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        swingNode = new SwingNode();
        previewPane.setContent(swingNode);
        printButton.setOnAction((ActionEvent event) -> {
            Stage stage = (Stage) previewPane.getScene().getWindow();
            stage.setOnCloseRequest((WindowEvent event1) -> {
                event1.consume();
            });
            printButton.setDisable(true);
            printButton.setText("Printing...");
            new Thread() {

                @Override
                public void run() {
                    printFileNames.forEach(fileName -> {
                        try {
                            PrintUtil.printPDF(fileName, true);
                        }
                        finally {
                            try {
                                File file = new File(fileName);
                                if (file.exists()) {
                                    file.delete();
                                }
                            }
                            catch (Exception ex) {
                                Logger.getLogger(PrintpopupController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            Platform.runLater(() -> {
                                stage.setOnCloseRequest(null);
                                stage.hide();
                            });
                        }
                    });
                }
            }.start();
        });
        nextButton.setOnAction((ActionEvent event) -> {
            if (activeFile < printFileNames.size() - 1) {
                activeFile++;
                updatePreview(activeFile, activePage);
            }
        });
        prevButton.setOnAction((ActionEvent event) -> {
            if (activeFile > 0) {
                activeFile--;
                updatePreview(activeFile, activePage);
            }
        });
        if (printFileNames.size() > 0) {
            updatePreview(activeFile, activePage);
        }
    }
    
    private void updatePreview(int fileNumber, int pageNumber) {
        SwingUtilities.invokeLater(() -> {
            final PagePanel panel = new PagePanel();
            //load a pdf from a byte buffer
            try {
                FileInputStream fileInputStream = new FileInputStream(printFileNames.get(fileNumber));
                byte[] pdfContent = new byte[fileInputStream.available()];
                fileInputStream.read(pdfContent, 0, fileInputStream.available());
                ByteBuffer buffer = ByteBuffer.wrap(pdfContent);
                PDFFile pdfFile = new PDFFile(buffer);
                // show the first page
                PDFPage page = pdfFile.getPage(pageNumber, true);
                panel.setSize((int)previewPane.getWidth(), (int)previewPane.getHeight());
                panel.showPage(page);
                pagesNumber = pdfFile.getNumPages();
            }
            catch (IOException ex) {
                Logger.getLogger(PrintpopupController.class.getName()).log(Level.SEVERE, null, ex);
            }
            Platform.runLater(() -> {
                pagesNumberLabel.setText(printFileNames.size() + " pages");
            });
            //Swing Panel to JavaFX Node
            swingNode.setContent(panel);
        });
    }
}
