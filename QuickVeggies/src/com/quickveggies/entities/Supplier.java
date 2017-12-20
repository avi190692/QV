package com.quickveggies.entities;

import java.io.InputStream;

public class Supplier{
    
	private int id;
    private String title;
    private String firstName;
    private String lastName;
    private String company;
    private String proprietor;
    private String mobile;
    private String mobile2;
    private String email;
    private String village;
    private String po;
    private String tehsil;
    private String ac;
    private String bank;
    private String ifsc;
	private String type="Supplier";
	private InputStream imageStream;
    
    public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

    public Supplier() {
    }

    public Supplier(int id,String title, String firstName, String lastName,
                    String company, String proprietor, String mobile,
                    String mobile2, String email, String village,
                    String po, String tehsil, String ac,
                    String bank, String ifsc) {
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.company = company;
        this.proprietor = proprietor;
        this.mobile = mobile;
        this.mobile2 = mobile2;
        this.email = email;
        this.village = village;
        this.po = po;
        this.tehsil = tehsil;
        this.ac = ac;
        this.bank = bank;
        this.ifsc = ifsc;
        this.setId(id);
    }
    
    

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getProprietor() {
        return proprietor;
    }

    public void setProprietor(String proprietor) {
        this.proprietor = proprietor;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMobile2() {
        return mobile2;
    }

    public void setMobile2(String mobile2) {
        this.mobile2 = mobile2;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getPo() {
        return po;
    }

    public void setPo(String po) {
        this.po = po;
    }

    public String getTehsil() {
        return tehsil;
    }

    public void setTehsil(String tehsil) {
        this.tehsil = tehsil;
    }

    public String getAc() {
        return ac;
    }

    public void setAc(String ac) {
        this.ac = ac;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getIfsc() {
        return ifsc;
    }

    public void setIfsc(String ifsc) {
        this.ifsc = ifsc;
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public InputStream getImageStream() {
		return imageStream;
	}

	public void setImageStream(InputStream imageStream) {
		this.imageStream = imageStream;
	}


}
