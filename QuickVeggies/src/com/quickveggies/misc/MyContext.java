package com.quickveggies.misc;

import com.quickveggies.entities.AccountMaster;

public class MyContext {
	
	private final static MyContext instance = new MyContext();

    public static MyContext getInstance() {
        return instance;
    }
    
    
    private AccountMaster accountMaster = new AccountMaster();

	public AccountMaster getAccountMaster() {
		return accountMaster;
	}
	
	
	
	private ButtonFlagIndicator buttonFlagIndicator = new ButtonFlagIndicator();

	public ButtonFlagIndicator getButtonFlagIndicator() {
		return buttonFlagIndicator;
	}
	
	
	
    
    

}
