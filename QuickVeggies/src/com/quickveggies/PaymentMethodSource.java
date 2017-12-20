package com.quickveggies;

import java.util.ArrayList;
import java.util.List;

public enum PaymentMethodSource {
    
	Cash, Bank;

	public static List<String> getValueList() {
		List<String> list = new ArrayList<>();
		list.add("Cash");
		list.add("Bank");
		return list;

	}
}