package com.quickveggies.misc;

import java.sql.SQLException;
import java.util.Optional;

import com.quickveggies.dao.DatabaseClient;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

public class DeleteTableButtonCell<S, T> extends TableCell<S, T> {

    private final Button deleteButton = new Button();
    private Runnable onButtonPress;
    private DatabaseClient dbclient = DatabaseClient.getInstance();
    private String value = null;
    private String multiDeleteKeyword = null;
    private String[] multiDeleteTableNames = null;

    public DeleteTableButtonCell(final String tablename, final String keyword) {
        this(tablename, keyword, null);
    }
    
    public DeleteTableButtonCell(final String tablename, final String keyword,
            final Runnable onButtonPress) {
        this.onButtonPress = onButtonPress;
        //Set the image for the button
        deleteButton.setPrefSize(30, 30);
        BackgroundImage backgroundImage = new BackgroundImage(
                new Image(getClass().getResource("/icons/delete.png").toExternalForm(), 30, 30, true, true),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        Background background = new Background(backgroundImage);
        deleteButton.setBackground(background);

        deleteButton.setOnMouseClicked((MouseEvent event) -> {
            String msg = "This will delete the record.  Are you sure  ? ";
            Alert alert = new Alert(AlertType.WARNING, msg, ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> optBType = alert.showAndWait();
            ButtonType btype = optBType.orElse(ButtonType.NO);
            if (btype.equals(ButtonType.NO)) {
                return;
            }
            if (this.onButtonPress != null) {
                //Run event handler if it exists
                this.onButtonPress.run();
                return;
            }
            System.out.println("Deleting entry from table" + tablename);
            if (value != null) {
                try {
                    if ("expenditures".equalsIgnoreCase(tablename)) {
                        dbclient.deleteExpenditureEntry(Integer.valueOf(value), true);
                    }
                    else if (multiDeleteTableNames == null) {
                        dbclient.deleteTableEntries(tablename, keyword, value, true);
                    }
                    else {
                        deleteMultiple();
                    }
                }
                catch (SQLException e) {
                    System.out.println("sqlexception in deleteTableEntries");
                    e.printStackTrace();
                }
            }
            else {
                System.out.print("null delete value in DeleteTableButtonCell\n");
            }
        });
    }

    public void setMultipleDelete(String[] tableNames, String keyword) {
        this.multiDeleteTableNames = tableNames;
        this.multiDeleteKeyword = keyword;
    }

    private void deleteMultiple() throws SQLException {
        for (String tablename : multiDeleteTableNames) {
            dbclient.deleteTableEntries(tablename, multiDeleteKeyword, value, true);
        }
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            try {
                value = item.toString();
            } catch (NullPointerException e) {
            }
            setGraphic(deleteButton);
        }
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    public Runnable getOnButtonPress() {
        return onButtonPress;
    }

    public void setOnButtonPress(Runnable onButtonPress) {
        this.onButtonPress = onButtonPress;
    }
}
