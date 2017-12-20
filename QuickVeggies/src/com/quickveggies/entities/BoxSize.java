package com.quickveggies.entities;

public class BoxSize {

	private int id;

	private String name;
	
	public BoxSize() {}
	
	public BoxSize(String name) {
		this.name = name;
	}
	
	public BoxSize(int id, String name) {
		this.name = name;
		this.id = id;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String toString() {
		return id+" "+name;
	}

}
