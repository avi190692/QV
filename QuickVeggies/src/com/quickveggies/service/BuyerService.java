package com.quickveggies.service;

import java.sql.SQLException;
import java.util.List;

import com.quickveggies.DTO.BuyerDTO;
import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Buyer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class BuyerService {
	
	public ObservableList<BuyerDTO> getBuyerDto(){
		ObservableList<BuyerDTO> buyerDTOList = FXCollections.observableArrayList();
		try {
			
			List<Buyer> buyerList = DatabaseClient.getInstance().getBuyers();
			buyerList.forEach(buyer -> buyerDTOList.add(new BuyerDTO(buyer.getEmail(),buyer.getMilestone())));
			buyerList.forEach(buyer -> System.out.println(buyer.getMilestone()));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return buyerDTOList;
	}
}
