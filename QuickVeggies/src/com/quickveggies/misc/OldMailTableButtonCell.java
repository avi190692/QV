package com.quickveggies.misc;


import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.Buyer;
import com.quickveggies.entities.Supplier;

import javafx.scene.control.TableCell;


public class OldMailTableButtonCell<S,T> extends TableCell<S,T>{
	private DatabaseClient dbclient=DatabaseClient.getInstance();
	private MailButton mailButton=null;
	private String mailSender=null;
	public OldMailTableButtonCell(javafx.fxml.Initializable callingWindow,String mailSender){
        this.mailSender=mailSender;
	}
	
    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
        	String title=getItem().toString();
        	String mailReceiver=null;
        	//determine whether buyer/supplier
        	try{
        		mailReceiver=((Buyer)(dbclient.getBuyerByName(title))).getEmail();
        	}catch(Exception e){
        		try{
        			mailReceiver=((Supplier)(dbclient.getSupplierByName(title))).getEmail();
        		}catch(Exception ex){System.out.print("invalid casting in MailTableButtonCell\n");}
        	}
        	
    		mailButton=new MailButton(mailReceiver,mailSender);
            setGraphic(mailButton);
        }
    }

	public MailButton getMailButton() {
		return mailButton;
	}

	public void setMailButton(MailButton mailButton) {
		this.mailButton = mailButton;
	}
	
  
}
