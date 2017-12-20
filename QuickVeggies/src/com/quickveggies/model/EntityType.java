package com.quickveggies.model;

public enum EntityType {
	BUYER("Buyer", "buyers1"), SUPPLIER("Supplier", "suppliers1"), LADAAN("Ladaan", "buyers1"),BIJAK("Bijak", "buyers1"),
	EXPENDITURE("Expenditure", "expenditures;"), UNIVERSAL_CASH("Universal", ""), UNIVERSAL_BANK("Universal", "");

	private final String value;
	private final String tableName;

	private EntityType(String name, String tableName) {
		this.value = name;
		this.tableName = tableName;
	}

	public String getValue() {
		return this.value;
	}

	public String getTableName() {
		return tableName;
	}
	
	public static EntityType getEntityTypeForValue(String value) {
		if (value == null) {
			return null;
		}
		EntityType result = null;
		for (EntityType et : EntityType.values() ) {
			if (et.value.toLowerCase().equalsIgnoreCase(value)) {
				result = et;
				break;
			}
		}
		return result;
	}
}