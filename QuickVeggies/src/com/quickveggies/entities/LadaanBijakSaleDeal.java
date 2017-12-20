package com.quickveggies.entities;

public class LadaanBijakSaleDeal {
	private String date = "";
	private String amountedTotal = "0";
	private String freight = "0";
	private String comission = "0";
	private String cases = "";
	private String saleNo = "";
	private String buyerRate = "0";
	private String aggregatedAmount = "0";
	private String initialSaleDate = "";
	
	private String dealId = "";
	private String buyerType;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getAmountedTotal() {
		return amountedTotal;
	}

	public void setAmountedTotal(String amountedTotal) {
		this.amountedTotal = amountedTotal;
	}

	public String getCases() {
		return cases;
	}

	public void setCases(String cases) {
		this.cases = cases;
	}

	public String getSaleNo() {
		return saleNo;
	}

	public void setSaleNo(int saleNo) {
		this.saleNo = String.valueOf(saleNo);
	}

	public String getBuyerRate() {
		return buyerRate;
	}

	public void setBuyerRate(String buyerRate) {
		this.buyerRate = buyerRate;

	}


	public String getBuyerType() {
		return buyerType;
	}

	public void setBuyerType(String buyerType) {
		this.buyerType = buyerType;
	}


	public String getAggregatedAmount() {
		return aggregatedAmount;
	}

	public void setAggregatedAmount(String aggregatedAmount) {
		this.aggregatedAmount = aggregatedAmount;
	}

	public String getFreight() {
		return freight;
	}

	public void setFreight(String freight) {
		this.freight = freight;
	}

	public String getComission() {
		return comission;
	}

	public void setComission(String comission) {
		this.comission = comission;
	}

	public String getDealId() {
		return dealId;
	}

	public void setDealId(String dealId) {
		this.dealId = dealId;
	}

	public String getInitialSaleDate() {
		return initialSaleDate;
	}

	public void setInitialSaleDate(String initialSaleDate) {
		this.initialSaleDate = initialSaleDate;
	}
	

}
