package com.quickveggies.service;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

import com.quickveggies.DTO.BuyerDTO;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Buyer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class BuyerService {

	public ObservableList<BuyerDTO> getBuyerDto() {
		ObservableList<BuyerDTO> buyerDTOList = FXCollections.observableArrayList();
		try {
			List<Buyer> buyerList = DatabaseClient.getInstance().getBuyers();
			buyerList.forEach(buyer -> {
				Double toalSpent;

				toalSpent = DatabaseClient.getInstance().getAggregatedAmtByTitle(buyer.getTitle()).stream()
						.mapToDouble(amount -> amount.doubleValue()).sum();
			
				
				Double budgetProgress = (toalSpent / buyer.getMilestone()) * 100;
				System.out.println(buyer.getFirstName()+buyer.getLastName()+" Totoal Spent Money = " +toalSpent +" Mileston "+ buyer.getMilestone() +"Budget Progress" + budgetProgress);
				buyerDTOList.add(
						new BuyerDTO(buyer.getEmail(), buyer.getMilestone(), buyer.getFirstName() + buyer.getLastName(),
								buyer.getCompany(), buyer.getCreditPeriod(), budgetProgress));
				});
			
			buyerList.forEach(buyer -> System.out.println(buyer.getCompany()));
		} catch (NoSuchElementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return buyerDTOList;
	}
}
