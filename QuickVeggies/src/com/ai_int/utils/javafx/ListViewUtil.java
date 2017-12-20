package com.ai_int.utils.javafx;

import java.util.LinkedHashMap;
import java.util.Map;

import com.quickveggies.misc.Utils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

public class ListViewUtil {

	public static void addColumnSettingsButtonHandler(final TableView<?> tv, final Pane parentPane,
			final Button settingsButton) {
		SettingButtonActionEventHandler btnActionEvent = new SettingButtonActionEventHandler(tv, parentPane,
				settingsButton);
		if (settingsButton.getOnAction() instanceof SettingButtonActionEventHandler) {
			SettingButtonActionEventHandler tmpEH = (SettingButtonActionEventHandler) settingsButton.getOnAction();
			tmpEH.dispose();
		}
		settingsButton.setOnAction(btnActionEvent);

	}

	private static class SettingButtonActionEventHandler implements EventHandler<ActionEvent> {
		private ListView<String> listView;
		private final TableView<?> tv;
		private final Button settingsButton;

		public SettingButtonActionEventHandler(final TableView<?> tv, final Pane parentPane, final Button settingsButton) {
			this.tv = tv;
			this.settingsButton = settingsButton;
			listView = new ListView<String>();
			/*
			 * We are adding the list view to to main panel, as other panels are not big enough
			 * to accommodate the width/height of the child panel. The toFront() method is then used to bring
			 * the list view to the front. 
			 */
			parentPane.getChildren().add(listView);
			listView.toFront();
			listView.setVisible(false);
			listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		}

		@SuppressWarnings({ "rawtypes" })
		@Override
		public void handle(ActionEvent event) {
			if (listView.isVisible()) {
				listView.setVisible(false);
				return;
			} else
				listView.setVisible(true);
			double pane_y = settingsButton.getParent().getLayoutY();
			double pane_x = settingsButton.getParent().getLayoutX();
			double listview_y = settingsButton.getHeight() + pane_y + 3;
			final Map<String, TableColumn> columnMap = new LinkedHashMap<>();
			for (TableColumn tc : tv.getColumns()) {
				if (Utils.isEmptyString(tc.getText())) {
					continue;
				}
				columnMap.put(tc.getText(), tc);
			}
			// listView = new ListView<>();
			listView.setPrefWidth(137);
			listView.setPrefHeight(179);
			listView.setLayoutY(listview_y);
			listView.setLayoutX(pane_x);
			listView.setItems(FXCollections.observableArrayList(columnMap.keySet()));
			listView.setCellFactory(CheckBoxListCell.forListView(new Callback<String, ObservableValue<Boolean>>() {
				@Override
				public ObservableValue<Boolean> call(String param) {
					TableColumn tc = columnMap.get(param);
					return tc.visibleProperty();
				}
			}));
			listView.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					if (!newValue) {
						// focus lost
						listView.setVisible(false);
					}
				}
			});

		}
		
		public void dispose() {
			if (this.listView != null) {
				this.listView.setVisible(false);
				this.listView = null;
			}
		}

	}

}
