package com.quickveggies.misc.tests;

import com.quickveggies.controller.dashboard.DSupplierController;
import com.quickveggies.model.EntityType;

import junit.framework.TestCase;

public class TestDSupplierController extends TestCase {
	
	public void testTotalAmountPaid() {
		//Long amount = DSupplierController.getAmountPaidBetween(15, PartyType.SUPPLIER);
		Long amount = DSupplierController.getAmountReceivedFromParty(15, EntityType.BUYER);
		assertTrue(amount != null);
		System.out.println(amount);
	}

}
