package com.quickveggies.entities;

public class User 
{
    private int id;
    private String name;
    private String password;
    private String email;
    private int role;
    private boolean bool_status;
    private String usertype;
    
    public User()
    {
    	
    }

    public User(String name, String password, String email, int role) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.role = role;
    }
    
    public User(int id,String name, String password, String email) {
    	this.id=id;
        this.name = name;
        this.password = password;
        this.email = email;
        //this.bool_status = bool_status;
    }

    public User(int id, String name, String password, String email, int role) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    
    public User(String name, String password, String email, int role, boolean bool_status, String usertype) {
		super();
		//this.id = id;
		this.name = name;
		this.password = password;
		this.email = email;
		this.role = role;
		this.bool_status = bool_status;
		this.usertype = usertype;
	}

	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isBool_status() {
		return bool_status;
	}

	public void setBool_status(boolean bool_status) {
		this.bool_status = bool_status;
	}

	
	public String getUsertype() {
		return usertype;
	}

	public void setUsertype(String usertype) {
		this.usertype = usertype;
	}

	@Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
