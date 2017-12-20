package com.quickveggies.entities;

public class StorageBuyerDeal {
	
	  private Integer buyerDealLineId ;
	  private Integer strorageDealLineId ;
	  
	  public StorageBuyerDeal(Integer buyerDealLineId, Integer strorageDealLineId) {
			this.strorageDealLineId = strorageDealLineId;
			this.buyerDealLineId = buyerDealLineId;
	  }
	  
	  
	public Integer getStrorageDealLineId() {
		return strorageDealLineId;
	}
	public void setStrorageDealLineId(Integer strorageDealLineId) {
		this.strorageDealLineId = strorageDealLineId;
	}
	public Integer getBuyerDealLineId() {
		return buyerDealLineId;
	}
	public void setBuyerDealLineId(Integer buyerDealLineId) {
		this.buyerDealLineId = buyerDealLineId;
	}


}
