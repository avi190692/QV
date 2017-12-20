package com.quickveggies.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.quickveggies.GeneralMethods;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AutoCompleteTextField extends TextField {

    /**
     * The existing autocomplete entries.
     */
    private SortedSet<String> entries;
    /**
     * The popup used to select an entry.
     */
    private ContextMenu entriesPopup;
    private TextField[] linkedTextFields = null; // any txt fields that sync
    // with this one
    private int selectedIndex = 0;
    public static int ENTRY_TXT = 0, ENTRY_IND = 1;
    private int linkedFieldsReturnType = 0;

    private LinkedList<String> searchResult = new LinkedList<String>();
    private SortedSet<String> lowerCaseEntries = new TreeSet<String>();
    private SortedSet<String> lcNonNumericEntries = new TreeSet<>();
    private javafx.fxml.Initializable callingWindow = null;
    private String fxmlRes = null;
    private String title = null;
    private String newWindowTrigger = null;
    private Object controller = null;

    /**
     * Construct a new AutoCompleteTextField.
     */
    public void linkToWindow(javafx.fxml.Initializable callingWindow, String fxmlRes,
            String title, String newWindowTrigger, Object controller) {
        this.callingWindow = callingWindow;
        this.fxmlRes = fxmlRes;
        this.title = title;
        this.newWindowTrigger = newWindowTrigger;
        this.controller = controller;
    }

    public void setEntries(SortedSet<String> entries) {
        this.entries = entries;
        this.lowerCaseEntries = new TreeSet<String>();
        for (String str : entries) {
            str = str.toLowerCase();
            this.lowerCaseEntries.add(str);
            String[] searchTxtArr = str.split(" ");
            if (searchTxtArr.length > 1) {
                char[] chars = searchTxtArr[0].trim().toCharArray();
                if (Character.isDigit(chars[0])) {
                    if (str.length() > chars.length) {
                        String nonNumStr = str.substring(chars.length, str.length()).trim();
                        this.lcNonNumericEntries.add(nonNumStr);
                    }
                }
            }
        }

        searchResultsAll();
    }

    public void setLinkedTextFields(TextField[] fields) {
        linkedTextFields = fields;
    }

    public void setLinkedFieldsReturnType(int type) {
        linkedFieldsReturnType = type;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public ContextMenu getMenu() {
        return entriesPopup;
    }

    private void searchResultsByStr() {
        searchResult.clear();
        // if a newWindowTrigger is present, it must always appear at the top of
        // the list:
        if (newWindowTrigger != null) {
            searchResult.add(entries.first());
        }
        // searchResult.addAll(entries.subSet(getText(), getText() +
        // Character.MAX_VALUE));
        String txt = getText().toLowerCase();

        SortedSet<String> searchedSet = lowerCaseEntries.subSet(txt, txt + Character.MAX_VALUE);
        List<String> lowerCaseList = Arrays.asList(lowerCaseEntries.toArray(new String[lowerCaseEntries.size()]));
        List<String> mainList = Arrays.asList(entries.toArray(new String[entries.size()]));
        SortedSet<String> originalSubSet = new TreeSet<>();

        String first = searchedSet.isEmpty() ? "" : searchedSet.first();
        String last = searchedSet.isEmpty() ? "" : searchedSet.last();
        //	System.out.println(last);
        int idx = lowerCaseList.indexOf(first);
        if (idx >= 0) {
            first = mainList.get(idx);
            originalSubSet = entries.subSet(first, last);
        }
        if (originalSubSet.isEmpty()) {
            searchedSet = lcNonNumericEntries.subSet(txt, txt + Character.MAX_VALUE);
            first = searchedSet.isEmpty() ? "" : searchedSet.first();
            last = searchedSet.isEmpty() ? "" : searchedSet.last();
            //		System.out.println(last);
            idx = nonNumericStringIndex(first);
            //		System.out.println(idx);
            first = mainList.get(idx);
            originalSubSet = entries.subSet(first, first + Character.MAX_VALUE);

        }
        searchResult.addAll(originalSubSet);
    }

    private int nonNumericStringIndex(String str) {
        int index = 0;
        List<String> list = new ArrayList<>(lowerCaseEntries);
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            String[] searchTxtArr = s.trim().split(" ");
            if (searchTxtArr.length > 1) {
                if (searchTxtArr[1].equals(str.trim())) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    private void searchResultsAll() {
        searchResult.clear();
        if (getText() != null) {
            searchResult.addAll(entries.subSet(getText(), getText() + Character.MAX_VALUE));
        }
    }

    public AutoCompleteTextField() {
        super();
        entries = new TreeSet<>();
        entriesPopup = new ContextMenu();

        textProperty().addListener((ObservableValue<? extends String> observableValue, String s, String s2) -> {
            if (getText() == null || getText().length() == 0) {
                entriesPopup.hide();
            } else {
                if (entries.size() > 0) {
                    searchResultsByStr();
                    populatePopup(searchResult);
                    if (!entriesPopup.isShowing()) {
                        try {
                            entriesPopup.show(AutoCompleteTextField.this, Side.BOTTOM, 0, 0);
                        } catch (IllegalArgumentException e) {
                        }
                    }
                } else {
                    entriesPopup.hide();
                }
            }
        });
    }

    /**
     * Get the existing set of autocomplete entries.
     *
     * @return The existing autocomplete entries.
     */
    public SortedSet<String> getEntries() {
        return entries;
    }

    /**
     * Populate the entry set with the given search results. Display is limited
     * to 10 entries, for performance.
     *
     * @param searchResult The set of matching strings.
     */
    private void populatePopup(List<String> searchResult) {
        List<CustomMenuItem> menuItems = new LinkedList<>();
        final String STR_ADD_NEW = "Add new...";
        boolean hasAddNewItem = false;
        // If you'd like more entries, modify this line.
        int maxEntries = 10;
        int count = Math.min(searchResult.size(), maxEntries);
        for (int i = 0; i < count; i++) {
            final String result = searchResult.get(i);
            if (result.trim().equalsIgnoreCase(STR_ADD_NEW)) {
                hasAddNewItem = true;
            }
            Label entryLabel = new Label(result);
            CustomMenuItem item = new CustomMenuItem(entryLabel, true);
            item.setOnAction((ActionEvent actionEvent) -> {
                if (result.equals(newWindowTrigger)) {
                    setText("");
                    getParent().requestFocus();
                    GeneralMethods.openNewWindow(callingWindow, fxmlRes, null, null);
                } else {
                    setText(result);
                    selectedIndex = Arrays.asList(entries.toArray()).indexOf(result);
                }
                entriesPopup.hide();
            });
            menuItems.add(item);
        }
        /**
         * Changed by Shoeb When the pop up menu has entries, for some reason
         * the newWindowTrigger doesn't seem to part of the list, so adding it
         * in such situation
         */
        if (!hasAddNewItem) {
            CustomMenuItem addNew = new CustomMenuItem(new Label(newWindowTrigger), true);
            addNew.setOnAction((ActionEvent event) -> {
                getParent().requestFocus();
                GeneralMethods.openNewWindow(callingWindow, fxmlRes, null, null);
            });
            menuItems.add(addNew);
        }
        entriesPopup.getItems().clear();
        entriesPopup.getItems().addAll(menuItems);

    }

    public void hidePopup() {
        entriesPopup.hide();
    }
}
