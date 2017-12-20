package com.quickveggies.entities;

/**
 *
 * @author Sergey Orlov <serg.merlin@gmail.com>
 */
public class AccountEntryPayment {
    
    private Integer id;
    private Integer accountEntryId;
    private String paymentTable;
    private Integer paymentId;

    public AccountEntryPayment(Integer id, Integer accountEntryId, String paymentTable, Integer paymentId) {
        this.id = id;
        this.accountEntryId = accountEntryId;
        this.paymentTable = paymentTable;
        this.paymentId = paymentId;
    }
    
    public AccountEntryPayment() {
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAccountEntryId() {
        return accountEntryId;
    }

    public void setAccountEntryId(Integer accountEntryId) {
        this.accountEntryId = accountEntryId;
    }

    public String getPaymentTable() {
        return paymentTable;
    }

    public void setPaymentTable(String paymentTable) {
        this.paymentTable = paymentTable;
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }
}
