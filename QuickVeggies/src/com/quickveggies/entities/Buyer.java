package com.quickveggies.entities;

import java.io.InputStream;

public class Buyer{
    private int id;
    private String title;
    private String firstName ;
    private String lastName ;
    private String company;
    private String proprietor;
    private String mobile;
    private String mobile2;
    private String email;
    private String shopno;
    private String city;
    private String email2;
    private String parentCompany;
    private String paymentMethod;
    private String creditPeriod;
    private String buyerType="Regular";
	private Double milestone;
	private String imagePath;
	private String type="Buyer";
	public static final String GODOWN_BUYER_TITLE = "Godown";
	public static final String COLD_STORE_BUYER_TITLE = "Cold Store";
    
	public static  enum CreditPeriodSourceEnum {
		DAY_1("1 day", 1), DAY_3("3 days", 3), DAY_7("7 days", 7), DAY_15("15 days", 15), DAY_30("30 days",
				30), DAY_45("45 days", 45);

		private String title;
		
		private int intValue;
		
		private CreditPeriodSourceEnum(String title, int value) {
			this.title = title;
			this.intValue = value;

		}

		public String getTitle() {
			return title;
		}
		
		public int getIntValue() {
			return intValue;
		}
		
		public static CreditPeriodSourceEnum getEnumForTitle(String title) {
			for (CreditPeriodSourceEnum e1 : values()) {
				if (e1.title.equalsIgnoreCase(title)) {
					return e1;
				}
			}
			return null;
		}

	};

    public Buyer() {
    }

    public Buyer(int id, String title, String firstName,
                 String lastName, String company, String proprietor,
                 String mobile, String mobile2, String email,
                 String shopno, String city, String email2,
                 String parentCompany, String paymentMethod,
                 String creditPeriod,String buyerType, Double milestone) 
    {
    	System.out.println("m here...");
        this.id = id;
        this.title = title;
        this.milestone = milestone;
        this.firstName = firstName;
        this.lastName = lastName;
        this.company = company;
        this.proprietor = proprietor;
        this.mobile = mobile;
        this.mobile2 = mobile2;
        this.email = email;
        this.shopno = shopno;
        this.city = city;
        this.email2 = email2;
        this.parentCompany = parentCompany;
        this.paymentMethod = paymentMethod;
        this.creditPeriod = creditPeriod;
        this.buyerType=buyerType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getShopno() {
        return shopno;
    }

    public void setShopno(String shopno) {
        this.shopno = shopno;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEmail2() {
        return email2;
    }

    public void setEmail2(String email2) {
        this.email2 = email2;
    }

    public String getParentCompany() {
        return parentCompany;
    }

    public void setParentCompany(String parentCompany) {
        this.parentCompany = parentCompany;
    }


    public String getCreditPeriod() {
        return creditPeriod;
    }

    public void setCreditPeriod(String creditPeriod) {
        this.creditPeriod = creditPeriod;
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBuyerType() {
		return buyerType;
	}

	public void setBuyerType(String buyerType) {
		this.buyerType = buyerType;
	}
	
	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	
	public Double getMilestone() {
		return milestone;
	}

	public void setMilestone(Double milestone) {
		this.milestone = milestone;
	}
	

}
