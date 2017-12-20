package com.quickveggies.entities;

/**
 *
 * @author serg.merlin
 */
public enum PartyType {
    
    BUYERS(0, "Buyers"), SUPPLIERS(1, "Suppliers"),
    BUYER_SUPPLIERS(2, "Buyers And Suppliers"), LADAAN(3, "Ladaan"),
    BIJAK(4, "Bijak"), EXPENDITURE_TYPES(5, "Expenditure Types"),
    UNIVERSAL(6, "Universal");
    
    Integer index;
    String title;

    PartyType(Integer index, String title) {
        this.index = index;
        this.title = title;
        
    }
    
    @Override
    public String toString() {
        return title;
    }
    
    public String getTitle() {
        return title;
    }

    public Integer getIndex() {
        return index;
    }
}
