package com.quickveggies.entities;

public class DExpensesTableLine{
	 private int id;
	 private String date="";
	 private String amount="";
	 private String comment="";
	 private String billto="";
	 private String type="";
	 
	 public DExpensesTableLine(int id,String date,String amount,String comment,String billto,String type){
		 this.date=date;
		 this.amount=amount;
		 this.comment=comment;
		 this.billto=billto;
		 this.type=type;
		 this.id=id;
	 }
	 
	 
	 public DExpensesTableLine(String[] values){
           amount=values[0];
           date=values[1];
           comment=values[2];
           billto=values[3];
           type=values[4];
           id=Integer.parseInt(values[5]);
		 }
	 
	public String[] getAll(){
		return new String[]{""+getId(),getAmount(),getDate(),getComment(),getBillto(),getType()};
	}
	
	public void set(String dataToEdit,String newValue){
		switch (dataToEdit){
		case "amount":
			setAmount(newValue);
			break;
		case "date":
			setDate(newValue);
			break;
		case "comment":
			setComment(newValue);
			break;
		case "billto":
			setBillto(newValue);
			break;
		case "type":
			setType(newValue);
			break;
		default:
			System.out.print("dataToEdit for DExpensesTableLine wasn't found\n");
		}
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getAmount() {
		return amount;
	}
        
        public Integer getAmountInt() {
		return amount == null ? 0 : Integer.valueOf(amount);
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getBillto() {
		return billto;
	}

	public void setBillto(String billto) {
		this.billto = billto;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public boolean isTotalLine() {
            return false;
        }
}
