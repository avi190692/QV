package com.quickveggies.entities;

public class AccountEntryLine {

    public static final int DEFAULT = 0, INSOFTWARE = 1, EXCLUDED = 2;

    private String accountName;
    private String dateCol, descriptionCol;
    private double withdrawalCol, depositCol, balanceCol;
    private String chqnoCol;
    private Integer status;
    private String payee;
    private String expense;
    private String comment;
    private String transIdCol;
    private Integer id;
    private Integer parentId;
    private Integer paymentId;

    private String payeeType;

    public AccountEntryLine(String accountName, String transIdCol, String dateCol, String chqnoCol,
            String descriptionCol, double withdrawalCol, double depositCol, double balanceCol, int status, String payee,
            String expense, String comment, Integer parentId) {
        setAccountName(accountName);
        setDateCol(dateCol);
        setChqnoCol(chqnoCol);
        setDescriptionCol(descriptionCol);
        setWithdrawalCol(withdrawalCol);
        setDepositCol(depositCol);
        setBalanceCol(balanceCol);
        setStatus(status);
        setPayee(payee);
        setExpense(expense);
        setComment(comment);
        setTransIdCol(transIdCol);
        this.parentId = parentId;
    }
    
    public AccountEntryLine(String accountName, String transIdCol, String dateCol, String chqnoCol,
            String descriptionCol, double withdrawalCol, double depositCol, double balanceCol, int status, String payee,
            String expense, String comment) {
        this(accountName, transIdCol, dateCol, chqnoCol, descriptionCol, withdrawalCol,
                depositCol, balanceCol, status, payee, expense, comment, null);
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public String getExpense() {
        return expense;
    }

    public void setExpense(String expense) {
        this.expense = expense;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getDateCol() {
        return dateCol;
    }

    public void setDateCol(String dateCol) {
        this.dateCol = dateCol;
    }

    public String getChqnoCol() {
        return chqnoCol;
    }

    public void setChqnoCol(String chqnoCol) {
        this.chqnoCol = chqnoCol;
    }

    public String getDescriptionCol() {
        return descriptionCol;
    }

    public void setDescriptionCol(String descriptionCol) {
        this.descriptionCol = descriptionCol;
    }

    public double getWithdrawalCol() {
        return withdrawalCol;
    }

    public void setWithdrawalCol(double withdrawalCol) {
        this.withdrawalCol = withdrawalCol;
    }

    public double getDepositCol() {
        return depositCol;
    }

    public void setDepositCol(double depositCol) {
        this.depositCol = depositCol;
    }

    public double getBalanceCol() {
        return balanceCol;
    }

    public void setBalanceCol(double balanceCol) {
        this.balanceCol = balanceCol;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPayeeType() {
        return payeeType;
    }

    public void setPayeeType(String payeeType) {
        this.payeeType = payeeType;
    }

    public String getTransIdCol() {
        return transIdCol;
    }

    public void setTransIdCol(String transIdCol) {
        this.transIdCol = transIdCol;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }
}
