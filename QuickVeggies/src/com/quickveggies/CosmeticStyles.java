package com.quickveggies;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;

public class CosmeticStyles {
    private static String getDefaultButtonStyle() {
        return "-fx-background-color:   linear-gradient(to top, rgba(0,0,0,.27), rgba(255,255,255,.37));\n" +
                "-fx-border-radius: 3px;\n" +
                "-fx-padding: 0 0 0 10;\n"+
                "-fx-text-fill:  #f5f5f8;";
    }

    private static String getHoverDefaultButtonStyle() {
        return "-fx-padding: 0 0 0 10; -fx-background-color: linear-gradient(to top, rgba(0, 0, 0, 0.24) 0%, rgba(255, 255, 255, 0.14) 100%);";
    }

    public static void addHoverEffect(final Button button) {
        button.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                button.setStyle(getDefaultButtonStyle());
            }
        });
        button.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                button.setStyle(getHoverDefaultButtonStyle());
            }
        });
    }
    
    
    public static void addHoverEffect(Button... buttons){
        for (Button button : buttons) {
            addHoverEffect(button);
        }
    }
 
    public static void addHoverEffect(final MenuButton button) {
        button.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                button.setStyle(getDefaultButtonStyle());
            }
        });
        button.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                button.setStyle(getHoverDefaultButtonStyle());
            }
        });
    }
    
    public static void addHoverEffect(MenuButton... buttons){
        for (MenuButton button : buttons) {
            addHoverEffect(button);
        }
    }
}
