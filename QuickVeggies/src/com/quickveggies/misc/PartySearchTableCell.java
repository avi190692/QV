package com.quickveggies.misc;

import com.quickveggies.entities.PartyType;

import javafx.scene.control.TableCell;

public class PartySearchTableCell<S, T> extends TableCell<S, T> {

    private SearchPartyButton searchButton = null;
    private Object linkedObject;
    //Object tableRow=null;

    public PartySearchTableCell(PartyType partyType) {
        super();
        searchButton = new SearchPartyButton();
        searchButton.setPrefSize(30, 15);
        searchButton.setPartyType(partyType);
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            setGraphic(searchButton);
            searchButton.setLinkedObject(PartySearchTableCell.this);
        }
    }
    
    

    public Object getLinkedObject() {
        return linkedObject;
    }

    public void setLinkedObject(Object linkedObject) {
        this.linkedObject = linkedObject;
    }
    
    public void clearChoice() {
        searchButton.clearChoice();
    }
}
