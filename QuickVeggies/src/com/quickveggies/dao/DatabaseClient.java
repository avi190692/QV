package com.quickveggies.dao;

import static com.quickveggies.entities.Buyer.COLD_STORE_BUYER_TITLE;
import static com.quickveggies.entities.Buyer.GODOWN_BUYER_TITLE;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ai.util.dates.DateUtil;
import com.quickveggies.GeneralMethods;
import com.quickveggies.controller.ChargeTypeValueMap;
import com.quickveggies.controller.SessionDataController;
import com.quickveggies.entities.Account;
import com.quickveggies.entities.AccountEntryLine;
import com.quickveggies.entities.AccountEntryPayment;
import com.quickveggies.entities.AccountMaster;
import com.quickveggies.entities.AuditLog;
import com.quickveggies.entities.BoxSize;
import com.quickveggies.entities.Buyer;
import com.quickveggies.entities.Charge;
import com.quickveggies.entities.Company;
import com.quickveggies.entities.DBuyerTableLine;
import com.quickveggies.entities.DSalesTableLine;
import com.quickveggies.entities.DSupplierTableLine;
import com.quickveggies.entities.Expenditure;
import com.quickveggies.entities.ExpenseInfo;
import com.quickveggies.entities.LadaanBijakSaleDeal;
import com.quickveggies.entities.MoneyPaidRecd;
import com.quickveggies.entities.QualityType;
import com.quickveggies.entities.StorageBuyerDeal;
import com.quickveggies.entities.Supplier;
import com.quickveggies.entities.Template;
import com.quickveggies.entities.User;
import com.quickveggies.misc.CommonFunctions;
import com.quickveggies.model.EntityType;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DatabaseClient {

    private static final String STORAGE_BUYER_DEALS = "storagebuyerdeals";
    private static Connection connection;
    private static DatabaseClient instance = null;

    private final static Map<String, String> TABLE_MAP = new LinkedHashMap<>();

    static {
        TABLE_MAP.put("arrival", "Arrival");
        TABLE_MAP.put("buyerDeals", "Buyer Deal");
        TABLE_MAP.put("ladaanBijakSaleDeals", "Ladaan/Bijak Sale Deal");
        TABLE_MAP.put("storageBuyerDeals", "Storage buyer deal");
        TABLE_MAP.put("supplierDeals", "Supplier Deal");
        TABLE_MAP.put("templates", "Template");
        TABLE_MAP.put("buyers1", "buyer");
        TABLE_MAP.put("accountEntries", "account entry");
        TABLE_MAP.put("accounts", "account");
        TABLE_MAP.put("suppliers1", "supplier");
        TABLE_MAP.put("charges", "charges");
        TABLE_MAP.put("expenditures", "expenditure");
    }

    protected DatabaseClient() {
    }

    public static DatabaseClient getInstance() {
        
        if (instance == null) {
            instance = new DatabaseClient();
            
               
            try 
            {
                connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/qvdb?user=postgres&password=postgres");
            } 
            catch (SQLException ex) 
            {
                Logger.getLogger(DatabaseClient.class.getName()).log(Level.SEVERE, null, ex);
            }
       
  
            }
        
        return instance;
    }
    // added by ss for account code upload
    public void dataClean()
    {
    	String cleaningDate ="delete from public.accountmaster_draftupload";
    	try 
    	{
			Statement smt = connection.createStatement();
					  smt.execute(cleaningDate);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
    
    private static final String account_code_upload_draft="insert into public.accountmaster_draftupload(accountcode,accountname,amount,reportflag,accounttype,subgllink,finyear)"+
                                                          "values(?,?,?,?,?,?,?)";
    public void accountCodeUpoadDraftMode(String accountcode,String accountname,String amount,String report_flag,String accounttype,String subgllink)
    {
    	
    	         
    	try 
    	{
			PreparedStatement pst = connection.prepareStatement(account_code_upload_draft);
			                  pst.setString(1,accountcode);
			                  pst.setString(2,accountname);
			                  pst.setDouble(3,Double.parseDouble(amount));
			                  pst.setString(4,report_flag);
			                  pst.setString(5,accounttype);
			                  pst.setString(6,subgllink);
			                  pst.setString(7,new CommonFunctions().financialYear());
			                  
			                  pst.executeUpdate();  
			                  
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    //added by ss for account code entry
    private static final String account_code_entry = "insert into public.accountmaster (accountcode,accountname,amount,fin_year,report_flag,active_flag,month,account_type)"+
    											     "values(?,?,?,?,?,?,?,?)";
    public void accountCodeEntry(AccountMaster accountMaster)
    {
    	 try 
    	 {
			PreparedStatement psmt = connection.prepareStatement(account_code_entry);
							  psmt.setString(1,accountMaster.getAccountcode());
							  psmt.setString(2,accountMaster.getAccountname());
							  psmt.setDouble(3,accountMaster.getAmount());
							  psmt.setString(4,accountMaster.getFin_year());
							  psmt.setString(5,accountMaster.getReport_flag());
							  psmt.setBoolean(6,accountMaster.isActive_flag());
							  psmt.setString(7,accountMaster.getMonth());
							  psmt.setString(8,accountMaster.getAccountType());
							  
							  psmt.execute();
				  
		 } 
    	 catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
    }
    private static final String account_info_update="update public.accountmaster set accountname=?,amount=?,report_flag=?,active_flag=?,account_type=?,subgl_link=? where accountcode=?";
    public void accountInfoUpdate(AccountMaster accountMaster)
    {
    	 try 
    	 {
			PreparedStatement psmt = connection.prepareStatement(account_info_update);
							  
							  psmt.setString(1,accountMaster.getAccountname());
							  psmt.setDouble(2,accountMaster.getAmount());
							  psmt.setString(3,accountMaster.getReport_flag());
							  psmt.setBoolean(4,accountMaster.isActive_flag());							
							  psmt.setString(5,accountMaster.getAccountType());
							  psmt.setString(6,accountMaster.getSubglLink());
							  
							  psmt.setString(7,accountMaster.getAccountcode());
							  
							  psmt.executeUpdate();
				  
		 } 
    	 catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    //## added by ss for account code logic test
    private static final String account_code_search = "select * from public.accountmaster where accountcode=?";
    public List<String> accountCodeSearch(String accountCode)
    {
    	List<String> acm = new ArrayList<String>();
			PreparedStatement psmt;
			try 
			{
				psmt = connection.prepareStatement(account_code_search);
				psmt.setString(1,accountCode);
			    ResultSet rs = psmt.executeQuery();
			    while(rs.next())
			    {
			    	acm.add(rs.getString(2));
			    } 
			} 
			catch (SQLException e)
			{
				e.printStackTrace();
			}
	   return acm;		
    }
    
    //## added by ss for account search engine
    //private static final String account_code_search_engine_bycodename = "select * from public.accountmaster where accountcode=? and accountname like %?%";
    public List<AccountMaster> accountCodeSearch(String searchString,String searchChoice)
    {
    	List<AccountMaster> acm = new ArrayList<AccountMaster>();
    	
			PreparedStatement psmt;
			try 
			{
				if(searchChoice.equals("accountCodeEdit"))
				{
					psmt = connection.prepareStatement("select * from public.accountmaster where accountcode = '"+searchString+"'");
					ResultSet rs = psmt.executeQuery();
					while(rs.next())
					{
						AccountMaster am = new AccountMaster();
						
						am.setAccountcode(rs.getString(2));
						am.setAccountname(rs.getString(3));
						am.setAmount(rs.getDouble(4));
						am.setFin_year(rs.getString(5));
						am.setReport_flag(rs.getString(7));		
						am.setActive_flag(rs.getBoolean(8));
						am.setAccountType(rs.getString(10));	
						am.setSubglLink(rs.getString(11));
						
						acm.add(am);
					} 
				}
				
			  else if(searchChoice.equals("Type"))
				{
					psmt = connection.prepareStatement("select * from public.accountmaster where report_flag like '%"+searchString+"%'");
					ResultSet rs = psmt.executeQuery();
					while(rs.next())
					{
						AccountMaster am = new AccountMaster();
						
						am.setAccountcode(rs.getString(2));
						am.setAccountname(rs.getString(3));
						am.setReport_flag(rs.getString(7));
						am.setAmount(rs.getDouble(4));
						
						acm.add(am);
					} 
				}
				
				else if(searchChoice.equals("Name"))
				{
					psmt = connection.prepareStatement("select * from public.accountmaster where accountname like '%"+searchString+"%'");
					ResultSet rs = psmt.executeQuery();
					while(rs.next())
					{
						AccountMaster am = new AccountMaster();
						
						am.setAccountcode(rs.getString(2));
						am.setAccountname(rs.getString(3));
						am.setReport_flag(rs.getString(7));
						am.setAmount(rs.getDouble(4));
						
						acm.add(am);
					} 
				}
				else if(searchChoice.equals("Balance"))
				{
					psmt = connection.prepareStatement("select * from public.accountmaster where amount like '%"+searchString+"%'");
					ResultSet rs = psmt.executeQuery();
					while(rs.next())
					{
						AccountMaster am = new AccountMaster();
						
						am.setAccountcode(rs.getString(2));
						am.setAccountname(rs.getString(3));
						am.setReport_flag(rs.getString(7));
						am.setAmount(rs.getDouble(4));
						
						acm.add(am);
					} 
				}
				else
				{
					psmt = connection.prepareStatement("select * from public.accountmaster where accountcode like '%"+searchString+"%'");
					ResultSet rs = psmt.executeQuery();
					while(rs.next())
					{
						AccountMaster am = new AccountMaster();
						
						am.setAccountcode(rs.getString(2));
						am.setAccountname(rs.getString(3));
						am.setReport_flag(rs.getString(7));
						am.setAmount(rs.getDouble(4));
						
						acm.add(am);
					} 
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
	   return acm;		
    }
    
   //## audit log entry for account code creation
    private static final String audit_log_entry="insert into auditlog (userid,eventdetail,eventobject,eventobjectid,amount) values (?,?,?,?,?)";
    public void insertLog_Audit(AuditLog auditlog)
    {
    	PreparedStatement psmt;
    	try 
    	{
			psmt = connection.prepareStatement(audit_log_entry);
			psmt.setString(1,auditlog.getUserId());
			psmt.setString(2,auditlog.getEventDetail());
			psmt.setString(3,auditlog.getEventObject());
			psmt.setInt(4,auditlog.getEventObjectId());
			psmt.setDouble(5,auditlog.getAmount());
			
			psmt.execute();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }
    
    

	public int countSpecificRows(String tablename, String columnName, String value) throws SQLException {
		int result = 0;
		PreparedStatement statement = connection
				.prepareStatement("SELECT * FROM " + tablename + " WHERE " + columnName + "='" + value + "';");
		ResultSet set = statement.executeQuery();
		while (set.next()) {
			result++;
		}
		return result;
	}

	public String getStringEntryFromSQL(String tablename, String searchword, String value, String targetword) {
		try {
			ResultSet set = getResult("select * from " + tablename + " where " + searchword + "='" + value + "';");
			if (!set.next()) {
				throw new NoSuchElementException();
			}
			return set.getString(targetword);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public int getIntEntryFromSQL(String tablename, String[] searchwords, String[] values, String targetword) {
		try {
			String searchCommand = "select * from " + tablename + " where ";
			for (int i = 0; i < searchwords.length; i++) {
				if (i < searchwords.length - 1) {
					searchCommand += searchwords[i] + "='" + values[i] + "' and ";
				} else {
					searchCommand += searchwords[i] + "='" + values[i] + "';";
				}
			}
			ResultSet set = getResult(searchCommand);
			if (!set.next()) {
				throw new NoSuchElementException();
			}
			return set.getInt(targetword);
		} catch (SQLException e) {
			e.printStackTrace();
			GeneralMethods.errorMsg("entry not found");
			return 0;
		}
	}

	public User getUserById(int id) throws SQLException, NoSuchElementException {
		ResultSet set = getResult("select * from users where id='" + id + "';");
		if (set.next()) {
			return getUserByName(set.getString("name"));
		} else {
			throw new NoSuchElementException("No such user in database");
		}
	}

	public String[] getBuyerDealEntryLineFromSql(int id) throws SQLException, NoSuchElementException {
		ResultSet set = getResult("select * from buyerDeals where id='" + id + "';");
		String title, date, sum, boxes, rate, amountReceived, dealID, aggregatedAmt, fruit, qualityType, boxSizeType;
		if (set.next()) {
			title = set.getString("buyerTitle");
			date = set.getString("dealDate");
			rate = set.getString("buyerRate");
			sum = set.getString("buyerPay");
			boxes = set.getString("boxes");
			amountReceived = set.getString("amountReceived");
			dealID = set.getInt("dealID") + "";
			aggregatedAmt = set.getString("aggregatedAmount");
			qualityType = set.getString("qualityType");
			boxSizeType = set.getString("boxSizeType");
			fruit = set.getString("fruit");
			return new String[] { "" + id, title, date, rate, sum, boxes, amountReceived, aggregatedAmt, dealID, fruit,
					qualityType, boxSizeType };
		} else {
			throw new NoSuchElementException();
		}
	}

	public List<Double> getAggregatedAmtByTitle(String title) {
		List<Double> aggregatedAmts = new ArrayList<Double>();

		try {
			ResultSet set = getResult("select aggregatedAmount from buyerDeals where buyerTitle='" + title + "';");

			if (set.next()) {
				aggregatedAmts.add(Double.parseDouble(set.getString("aggregatedAmount")));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return aggregatedAmts;
	}

	public DBuyerTableLine getBuyerDealEntry(int id) throws SQLException, NoSuchElementException {
		ResultSet set = getResult("select * from buyerDeals where id='" + id + "';");
		String title, date, sum, boxes, rate, amountReceived, dealID, aggregatedAmt, fruit, qualityType, boxSizeType;
		if (set.next()) {
			title = set.getString("buyerTitle");
			date = set.getString("dealDate");
			rate = set.getString("buyerRate");
			sum = set.getString("buyerPay");
			boxes = set.getString("boxes");
			amountReceived = set.getString("amountReceived");
			dealID = set.getInt("dealID") + "";
			aggregatedAmt = set.getString("aggregatedAmount");
			qualityType = set.getString("qualityType");
			boxSizeType = set.getString("boxSizeType");
			fruit = set.getString("fruit");
			return new DBuyerTableLine(title, date, rate, sum, boxes, "" + set.getInt("id"), amountReceived,
					aggregatedAmt, dealID, fruit, qualityType, boxSizeType);
		} else {
			throw new NoSuchElementException();
		}
	}

	public List<DBuyerTableLine> getBuyerDealEntries(String buyerTitle, String[] skipBuyers)
			throws SQLException, NoSuchElementException {
		List<DBuyerTableLine> lines = new ArrayList<>();
		String query = "SELECT * FROM buyerDeals WHERE 1=1 ";
		if (buyerTitle != null && !buyerTitle.isEmpty()) {
			query += " AND buyerTitle='" + buyerTitle + "' ";
		}
		if (skipBuyers != null && skipBuyers.length > 0) {
			query += " AND NOT buyerTitle IN (";
			for (String buyer : skipBuyers) {
				query += "'" + buyer + "',";
			}
			query = query.substring(0, query.length() - 1);
			query += ")";
		}
		ResultSet set = getResult(query + " ORDER BY dealDate;");
		String title, date, sum, boxes, rate, amountReceived, dealID, aggregatedAmt, fruit, qualityType, boxSizeType;
		while (set.next()) {
			title = set.getString("buyerTitle");
			date = set.getString("dealDate");
			rate = set.getString("buyerRate");
			sum = set.getString("buyerPay");
			boxes = set.getString("boxes");
			amountReceived = set.getString("amountReceived");
			dealID = set.getInt("dealID") + "";
			aggregatedAmt = set.getString("aggregatedAmount");
			qualityType = set.getString("qualityType");
			boxSizeType = set.getString("boxSizeType");
			fruit = set.getString("fruit");
			lines.add(new DBuyerTableLine(title, date, rate, sum, boxes, "" + set.getInt("id"), amountReceived,
					aggregatedAmt, dealID, fruit, qualityType, boxSizeType));
		}
		return lines;
	}

	public String[] getExpenseEntryLineFromSql(int id) throws SQLException, NoSuchElementException {
		ResultSet set = getResult("select * from expenses where id='" + id + "';");
		String amount, date, comment, billto, type;
		if (set.next()) {
			amount = set.getString("amount");
			date = set.getString("date");
			comment = set.getString("comment");
			billto = set.getString("billto");
			type = set.getString("type");
			return new String[] { "" + id, amount, date, comment, billto, type };
		} else {
			throw new NoSuchElementException();
		}
	}

	public String getLastExpenseType(int id) throws SQLException, NoSuchElementException {
		ResultSet set = getResult("select * from last_expenses where id='" + id + "';");
		if (set.next()) {
			return set.getString("type");
		} else {
			throw new NoSuchElementException();
		}
	}

	public String getLastExpense(int id) throws SQLException, NoSuchElementException {
		ResultSet set = getResult("select * from last_expenses where id='" + id + "';");
		if (set.next()) {
			return set.getString("category");
		} else {
			throw new NoSuchElementException();
		}
	}

	public List<String[]> getSupplierDealEntryLines(String title) throws SQLException {
		List<String[]> lines = new ArrayList<>();
		String query = "SELECT * FROM supplierDeals ";
		if (title != null && !title.isEmpty()) {
			query += " WHERE supplierTitle='" + title + "' ";
		}
		ResultSet set = getResult(query + ";");
		String supplierTitle, date, rate, net, cases, agent, amountReceived, dealStr, fruit, qualityType, boxSizeType;
		while (set.next()) {
			supplierTitle = set.getString("supplierTitle");
			if (supplierTitle == null || "".equals(supplierTitle)) {
				continue;
			}
			date = set.getString("date");
			rate = set.getString("supplierRate");
			net = set.getString("net");
			cases = set.getString("cases");
			agent = set.getString("agent");
			amountReceived = set.getString("amountReceived");
			qualityType = set.getString("qualityType");
			boxSizeType = set.getString("boxSizeType");
			fruit = set.getString("fruit");
			int dealId = set.getInt("dealID");
			String proprietor = getSupplierByName(supplierTitle).getProprietor();
			String amanat = getSalesEntryLineByDealId(dealId).getAmanat();
			dealStr = dealId + "";
			lines.add(new String[] { "" + set.getInt("id"), supplierTitle.trim(), date, rate, net, cases, agent,
					amountReceived, dealStr, proprietor, amanat, fruit, qualityType, boxSizeType });
		}
		return lines;
	}

	public List<DSupplierTableLine> getSupplierDealEntries(String title) throws SQLException {
		List<DSupplierTableLine> lines = new ArrayList<>();
		String query = "SELECT * FROM supplierDeals ";
		if (title != null && !title.isEmpty()) {
			query += " WHERE supplierTitle='" + title + "' ";
		}
		ResultSet set = getResult(query + ";");
		String supplierTitle, date, rate, net, cases, agent, amountReceived, dealStr, fruit, qualityType, boxSizeType;
		while (set.next()) {
			supplierTitle = set.getString("supplierTitle");
			if (supplierTitle == null || "".equals(supplierTitle)) {
				continue;
			}
			date = set.getString("date");
			rate = set.getString("supplierRate");
			net = set.getString("net");
			cases = set.getString("cases");
			agent = set.getString("agent");
			amountReceived = set.getString("amountReceived");
			qualityType = set.getString("qualityType");
			boxSizeType = set.getString("boxSizeType");
			fruit = set.getString("fruit");
			int dealId = set.getInt("dealID");
			String proprietor = getSupplierByName(supplierTitle).getProprietor();
			String amanat = getSalesEntryLineByDealId(dealId).getAmanat();
			lines.add(new DSupplierTableLine("" + set.getInt("id"), supplierTitle.trim(), date, rate, net, cases, agent,
					amountReceived, dealId + "", proprietor, amanat, fruit, qualityType, boxSizeType));
		}
		return lines;
	}

	public DSupplierTableLine getSupplierDealEntry(int id) throws SQLException {
		String query = "SELECT * FROM supplierDeals WHERE id=" + id + ";";
		ResultSet set = getResult(query);
		String supplierTitle, date, rate, net, cases, agent, amountReceived, fruit, qualityType, boxSizeType;
		if (set.next()) {
			supplierTitle = set.getString("supplierTitle").trim();
			date = set.getString("date");
			rate = set.getString("supplierRate");
			net = set.getString("net");
			cases = set.getString("cases");
			agent = set.getString("agent");
			amountReceived = set.getString("amountReceived");
			qualityType = set.getString("qualityType");
			boxSizeType = set.getString("boxSizeType");
			fruit = set.getString("fruit");
			int dealId = set.getInt("dealID");
			String proprietor = getSupplierByName(supplierTitle).getProprietor();
			String amanat = getSalesEntryLineByDealId(dealId).getAmanat();
			return new DSupplierTableLine("" + set.getInt("id"), supplierTitle, date, rate, net, cases, agent,
					amountReceived, dealId + "", proprietor, amanat, fruit, qualityType, boxSizeType);
		} else {
			throw new NoSuchElementException("Supplier deal isn't found");
		}
	}

	public Template getTemplate(String accountName) throws SQLException, NoSuchElementException {
		PreparedStatement query = connection.prepareStatement("" + "select * from templates where accountName=?;");
		query.setString(1, accountName);
		ResultSet set = query.executeQuery();
		if (set.next()) {
			return new Template(set.getString("accountName"), set.getInt("transIdCol"), set.getInt("dateCol"),
					set.getInt("chqnoCol"), set.getInt("descriptionCol"), set.getInt("withdrawalCol"),
					set.getInt("depositCol"), set.getInt("balanceCol"));
		} else {
			throw new NoSuchElementException();
		}
	}

	public ObservableList<AccountEntryLine> getAccountEntryLines(String accountName)
			throws SQLException, NoSuchElementException {
		ObservableList<AccountEntryLine> result = FXCollections.observableArrayList();
		PreparedStatement query = connection.prepareStatement("" + "select * from accountEntries where accountName=?;");
		query.setString(1, accountName);
		ResultSet set = query.executeQuery();
		while (set.next()) {
			AccountEntryLine line = new AccountEntryLine(set.getString("accountName"), set.getString("transIdCol"),
					set.getString("dateCol"), set.getString("chqnoCol"), set.getString("descriptionCol"),
					set.getDouble("withdrawalCol"), set.getDouble("depositCol"), set.getDouble("balanceCol"),
					set.getInt("status"), set.getString("payee"), set.getString("expense"), set.getString("comment"),
					set.getInt("parentId"));
			line.setId(set.getInt("id"));
			result.add(line);
		}
		if (result.isEmpty()) {
			throw new NoSuchElementException();
		} else {
			return result;
		}
	}

	public AccountEntryLine getAccountEntryLine(Integer id) throws SQLException, NoSuchElementException {
		PreparedStatement query = connection.prepareStatement("" + "select * from accountEntries where id=?;");
		query.setInt(1, id);
		ResultSet set = query.executeQuery();
		if (set.next()) {
			AccountEntryLine line = new AccountEntryLine(set.getString("accountName"), set.getString("transIdCol"),
					set.getString("dateCol"), set.getString("chqnoCol"), set.getString("descriptionCol"),
					set.getDouble("withdrawalCol"), set.getDouble("depositCol"), set.getDouble("balanceCol"),
					set.getInt("status"), set.getString("payee"), set.getString("expense"), set.getString("comment"),
					set.getInt("parentId"));
			line.setId(set.getInt("id"));
			return line;
		} else {
			throw new NoSuchElementException();
		}
	}

    public DSalesTableLine getSalesEntryLineFromSql(int id) throws SQLException, NoSuchElementException {
        ResultSet set = getResult("select * from arrival where id='" + id + "';");

		String fruit, date, challan, supplier, totalQuantity, fullCase, halfCase, agent, truck, driver, gross, charges,
				net, remarks, dealID, type, amanat;

		if (set.next()) {
			fruit = set.getString("fruit");
			date = set.getString("date");
			gross = set.getString("gross");
			agent = set.getString("fwagent");
			challan = set.getString("challan");
			halfCase = set.getString("halfCase");
			fullCase = set.getString("fullCase");
			truck = set.getString("truck");
			driver = set.getString("driver");
			charges = set.getString("charges");
			remarks = set.getString("remarks");
			net = set.getString("net");
			supplier = set.getString("supplier");
			totalQuantity = set.getString("totalQuantity");
			dealID = "" + set.getInt("dealID");
			type = set.getString("type");
			amanat = set.getString("amanat");
			return new DSalesTableLine(fruit, "" + id, date, challan, supplier, totalQuantity, fullCase, halfCase,
					agent, truck, driver, gross, charges, net, remarks, dealID, type, amanat);
		} else {
			throw new NoSuchElementException();
		}
	}

	public List<DSalesTableLine> getSalesEntries() throws SQLException, NoSuchElementException {
		ResultSet set = getResult("SELECT * FROM arrival;");

		String fruit, date, challan, supplier, totalQuantity, fullCase, halfCase, agent, truck, driver, gross, charges,
				net, remarks, dealID, type, amanat;
		List<DSalesTableLine> values = new ArrayList<>();

		while (set.next()) {
			fruit = set.getString("fruit");
			date = set.getString("date");
			gross = set.getString("gross");
			agent = set.getString("fwagent");
			challan = set.getString("challan");
			halfCase = set.getString("halfCase");
			fullCase = set.getString("fullCase");
			truck = set.getString("truck");
			driver = set.getString("driver");
			charges = set.getString("charges");
			remarks = set.getString("remarks");
			net = set.getString("net");
			supplier = set.getString("supplier");
			totalQuantity = set.getString("totalQuantity");
			dealID = "" + set.getInt("dealID");
			type = set.getString("type");
			amanat = set.getString("amanat");
			values.add(new DSalesTableLine(fruit, "" + set.getLong("id"), date, challan, supplier, totalQuantity,
					fullCase, halfCase, agent, truck, driver, gross, charges, net, remarks, dealID, type, amanat));
		}
		return values;
	}

	public int saveEntryToSql(String tableName, String[] colNames, String[] values) {
		int generatedId = 0;
		String sqlCommand = "insert into " + tableName + " (" + colNames[0];
		for (int i = 1; i < colNames.length; i++) {
			sqlCommand += "," + colNames[i];
		}
		sqlCommand += ") values('" + values[0] + "'";
		for (int i = 1; i < values.length; i++) {
			sqlCommand += ",'" + values[i] + "'";
		}
		sqlCommand += ")";
		try {
			PreparedStatement statement = connection.prepareStatement(sqlCommand, Statement.RETURN_GENERATED_KEYS);
			statement.executeUpdate();
			generatedId = getGeneratedKey(statement);
			String baseMsg = "ADDED Entry for %s (Entry No: %d )";
			auditEntry(baseMsg, tableName, values, generatedId);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.print("sql exception in saveEntryToSql");
		}
		return generatedId;
	}

	private void auditEntry(String baseMsg, String tableName, String[] values, int generatedId) {
		String formattedTableName = TABLE_MAP.get(tableName);
		String auditMsg = null;
		if (tableName.toLowerCase().equals("buyerdeals")) {
			if (values[0].equalsIgnoreCase(COLD_STORE_BUYER_TITLE)) {
				tableName = STORAGE_BUYER_DEALS;
				auditMsg = String.format(baseMsg, formattedTableName + " from " + COLD_STORE_BUYER_TITLE, generatedId);
			} else if (values[0].equalsIgnoreCase(GODOWN_BUYER_TITLE)) {
				tableName = STORAGE_BUYER_DEALS;
				auditMsg = String.format(baseMsg, formattedTableName + " from " + GODOWN_BUYER_TITLE, generatedId);
			}
		} else if (tableName.toLowerCase().equals("supplierdeals")) {
			if (values[0].equalsIgnoreCase(COLD_STORE_BUYER_TITLE)) {
				auditMsg = String.format(baseMsg, formattedTableName + " from " + COLD_STORE_BUYER_TITLE, generatedId);
			} else if (values[0].equalsIgnoreCase(GODOWN_BUYER_TITLE)) {
				auditMsg = String.format(baseMsg, formattedTableName + " from " + GODOWN_BUYER_TITLE, generatedId);
			}
		}
		if (auditMsg == null) {
			auditMsg = String.format(baseMsg, formattedTableName, generatedId);
		}
		insertAuditRecord(new AuditLog(0, getCurrentUser(), new Date(), auditMsg, tableName, generatedId));
	}

	public DSalesTableLine getSalesEntryLineByDealId(int dealid) throws SQLException, NoSuchElementException {
		ResultSet set = getResult("select * from arrival where dealID='" + dealid + "';");
		if (!set.next()) {
			throw new NoSuchElementException();
		}
		return getSalesEntryLineFromSql(set.getInt("id"));
	}

	public List<DSalesTableLine> getSalesEntryLineBySupplierName(String supplier)
			throws SQLException, NoSuchElementException {
		List<DSalesTableLine> lines = new ArrayList<>();
		ResultSet set = getResult("select * from arrival where supplier='" + supplier + "';");
		while (set.next()) {
			lines.add(new DSalesTableLine(set.getString("fruit"), set.getString("id"), set.getString("date"),
					set.getString("challan"), set.getString("supplier"), set.getString("totalQuantity"),
					set.getString("fullCase"), set.getString("halfCase"), set.getString("fwagent"),
					set.getString("truck"), set.getString("driver"), set.getString("gross"), set.getString("charges"),
					set.getString("net"), set.getString("remarks"), set.getString("dealId"), set.getString("type"),
					set.getString("amanat")));
		}
		return lines;
	}

	public void saveBuyer(Buyer buyer) throws SQLException {
		String firstName = buyer.getFirstName();
		int id = getRowsNum("buyers1");
		String title = String.valueOf(id).concat(" ").concat(firstName);
		String lastName = buyer.getLastName();

		try {// check if buyer exists
			this.getBuyerByName(title);
			GeneralMethods.errorMsg("Saving failed - title already exists in database");
			return;
		} catch (NoSuchElementException e) {
		}

		String company = buyer.getCompany();
		String proprietor = buyer.getProprietor();
		String mobile = buyer.getMobile();
		String mobile2 = buyer.getMobile2();
		String email = buyer.getEmail();
		String shopno = buyer.getShopno();
		String city = buyer.getCity();
		String email2 = buyer.getEmail2();
		String parentCompany = buyer.getParentCompany();
		String paymentMethod = buyer.getPaymentMethod();
		String creditPeriod = buyer.getCreditPeriod();
		String buyerType = buyer.getBuyerType();
		Double milestone = buyer.getMilestone();
		// Statement execStmt = connection.createStatement();
		// execStmt.execute("SET IDENTITY_INSERT buyers1 ON");
		String sql = "INSERT INTO buyers1"
				+ " (title,firstName,lastName,company,proprietor,mobile,mobile2,email,shopno,city,email2,parentCompany,creditPeriod,paymentMethod,buyerType,photo,milestone) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, title);
		statement.setString(2, firstName);
		statement.setString(3, lastName);
		statement.setString(4, company);
		statement.setString(5, proprietor);
		statement.setString(6, mobile);
		statement.setString(7, mobile2);
		statement.setString(8, email);
		statement.setString(9, shopno);
		statement.setString(10, city);
		statement.setString(11, email2);
		statement.setString(12, parentCompany);
		statement.setString(13, creditPeriod);
		statement.setString(14, paymentMethod);
		statement.setString(15, buyerType);
		statement.setString(16, buyer.getImagePath());
		statement.setDouble(17, milestone);
		statement.executeUpdate();
		int genId = getGeneratedKey(statement);
		insertAuditRecord(new AuditLog(0, getCurrentUser(), new Date() , "ADDED new buyer:".concat(title), "buyers1", genId));

	}

	public boolean checkIfTitleExists(String tableName, String title) throws SQLException {
		ResultSet set = getResult("select * from " + tableName + " where title='" + title + "';");
		if (set.next()) {
			return true;
		} else {
			return false;
		}
	}

	public Vector<Vector<Object>> getAllBuyers() throws SQLException {
		Vector<Vector<Object>> objects = new Vector<>();
		ResultSet resultSet = connection.prepareStatement("SELECT * FROM buyers").executeQuery();
		while (resultSet.next()) {
			Vector<Object> vector = new Vector<>();
			for (int i = 1; i <= 15; i++) {
				vector.add(resultSet.getObject(i));
			}
			objects.add(vector);
		}
		return objects;
	}

	public void saveAccountEntryLine(AccountEntryLine entryline) {
		String sql = "INSERT INTO accountEntries (accountName, dateCol, chqnoCol, descriptionCol, withdrawalCol, depositCol, "
				+ "balanceCol, status, payee, expense, comment, transIdCol, parentId) values (?, ?, ?, ?, ?, ?, ?, ? , ? , ? , ?, ?, ?);";
		try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, entryline.getAccountName());
			ps.setString(2, entryline.getDateCol());
			ps.setString(3, entryline.getChqnoCol());
			ps.setString(4, entryline.getDescriptionCol());
			ps.setDouble(5, entryline.getWithdrawalCol());
			ps.setDouble(6, entryline.getDepositCol());
			ps.setDouble(7, entryline.getBalanceCol());
			ps.setInt(8, entryline.getStatus());
			ps.setString(9, entryline.getPayee());
			ps.setString(10, entryline.getExpense());
			ps.setString(11, entryline.getComment());
			ps.setString(12, entryline.getTransIdCol());

			if (entryline.getParentId() != null) {
				ps.setInt(13, entryline.getParentId());
			} else {
				ps.setInt(13, java.sql.Types.INTEGER);
			}
			ps.executeUpdate();
			int genId = getGeneratedKey(ps);
			entryline.setId(genId);
			// Don't need to log every line
			//
			// insertAuditRecord(new AuditLog(0, getCurrentUser(), null,
			// "ADDED transaction entry to account:
			// ".concat(entryline.getAccountName()),
			// "accountEntries", genId));
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public void setAccountEntryStatus(int id, int newStatus, String auditLog) {
		System.err.println("id=" + id + " status" + newStatus);
		String sql = "UPDATE accountEntries SET status=? WHERE id=?;";
		try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, newStatus);
			ps.setInt(2, id);
			ps.executeUpdate();
			if (auditLog == null) {
				if (newStatus == 0) {
					insertAuditRecord(new AuditLog(0, getCurrentUser(), new Date() ,
							"UPDATED transaction entry to account: " + id, "accountEntries", id));
				}
			} else if (!"".equals(auditLog)) {
				insertAuditRecord(new AuditLog(0, getCurrentUser(), new Date() , auditLog, "accountEntries", id));
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public void deleteAccountEntry(int id) {
		try (PreparedStatement ps = connection.prepareStatement("DELETE FROM accountEntries WHERE id = ?")) {
			ps.setInt(1, id);
			ps.executeUpdate();
			// insertAuditRecord(new AuditLog(0, getCurrentUser(), null,
			// "DELETED exepense info :".concat(id), null, 0));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean hasAccountEntry(String date, double withdrawal, double deposit, String desc) {
		boolean hasRecord = false;
		String sql = "Select * from accountEntries where dateCol=? and withdrawalCol=? and depositCol=? and descriptionCol=?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, date);
			ps.setDouble(2, withdrawal);
			ps.setDouble(3, deposit);
			ps.setString(4, desc);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				hasRecord = true;
			}
		} catch (SQLException x) {
			x.printStackTrace();
		}
		return hasRecord;
	}

	public void saveAccount(Account account) throws SQLException {
		String accountNumber = account.getAccountNumber();
		int accountType = account.getAccountType();
		double balance = account.getBalance();
		double initBalance = account.getInitBalance();
		String accountName = account.getAccountName();
		String phone = account.getPhone();
		String description = account.getDescription();
		String bankName = account.getBankName();
		int lastupdated = account.getLastupdated();
		// Statement execStmt = connection.createStatement();
		// execStmt.execute("SET IDENTITY_INSERT banks ON");
		PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO accounts (acc_name,acc_type,acc_number,bank_name,balance,initBalance,phone,description,lastupdated) VALUES(?,?,?,?,?,?,?,?,?)",
				Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, accountName);
		statement.setInt(2, accountType);
		statement.setString(3, accountNumber);
		statement.setString(4, bankName);
		statement.setDouble(5, balance);
		statement.setDouble(6, initBalance);
		statement.setString(7, phone);
		statement.setString(8, description);
		statement.setInt(9, lastupdated);
		statement.executeUpdate();
		Integer key = getGeneratedKey(statement);
		insertAuditRecord(
				new AuditLog(0, getCurrentUser(), new Date() , "ADDED bank account ".concat(accountName), "accounts", key));
	}

	public void updateAccount(Account account) {
		String tableName = "accounts";
		String sqlCommand = "UPDATE " + tableName + " SET lastupdated=?, acc_type=?, "
				+ "bank_name=?, acc_name=?, acc_number=?, phone=?, description=?, "
				+ "initBalance=?, balance=? WHERE id=?";
		try {
			System.out.println(sqlCommand);
			PreparedStatement statement = connection.prepareStatement(sqlCommand);
			statement.setInt(1, account.getLastupdated());
			statement.setInt(2, account.getAccountType());
			statement.setString(3, account.getBankName());
			statement.setString(4, account.getAccountName());
			statement.setString(5, account.getAccountNumber());
			statement.setString(6, account.getPhone());
			statement.setString(7, account.getDescription());
			statement.setDouble(8, account.getInitBalance());
			statement.setDouble(9, account.getBalance());
			statement.setInt(10, account.getId());
			statement.executeUpdate();
			// String auditMsg = String.format("UPDATED account (Entry No: %d
			// )", account.getId());
			// insertAuditRecord(new AuditLog(account.getId(), getCurrentUser(),
			// null, auditMsg, tableName, account.getId()));
			String auditMsg = "Added new entries in bank account " + account.getBankName();
			insertAuditRecord(
					new AuditLog(account.getId(), getCurrentUser(), new Date() , auditMsg, tableName, account.getId()));
		} catch (SQLException e) {
			System.out.print("sql exception in updateAccount " + e.getMessage());
		}
	}

	public Account getAccountByName(String accountName) throws SQLException {
		PreparedStatement query = connection.prepareStatement("SELECT * FROM accounts WHERE acc_name=?;");
		query.setString(1, accountName);
		ResultSet set = query.executeQuery();
		Account account = null;
		while (set.next()) {
			int id = set.getInt(1);
			String accountNumber = set.getString("acc_number");
			int accountType = set.getInt("acc_type");
			double balance = set.getDouble("balance");
			double initBalance = set.getDouble("initBalance");
			String phone = set.getString("phone");
			String description = set.getString("description");
			String bankName = set.getString("bank_name");
			int lastupdated = set.getInt("lastupdated");
			account = new Account(id, accountNumber, accountType, balance, initBalance, accountName, bankName, phone,
					description, lastupdated);
		}
		return account;
	}

	public Account getAccountById(int id) throws SQLException {
		ResultSet set = connection.prepareStatement("SELECT * FROM accounts WHERE id='" + id + "';").executeQuery();
		set.next();
		String accountName = set.getString("acc_name");
		return getAccountByName(accountName);
	}

	public void saveUser(User user) throws SQLException {
		String name = user.getName();
		String email = user.getEmail();
		String password = user.getPassword();
		int role = user.getRole();
		String userType = user.getUsertype();

		PreparedStatement prepStmnt = connection.prepareStatement(
				"insert into users (name,pass,email,role,usertype,bool_activestatus) " + "values(?,?,?,?,?,?)");
		prepStmnt.setString(1, name);
		prepStmnt.setString(2, password);
		prepStmnt.setString(3, email);
		prepStmnt.setInt(4, role);
		prepStmnt.setString(5, userType);
		prepStmnt.setBoolean(6, true);

		prepStmnt.executeUpdate();

		System.out.println("Database: user " + name + " successfully created.");
	}

	// ## changed by ss on 21-Dec-2017
	public User getUserByName(String name) throws SQLException, NoSuchElementException {
		ResultSet user = getResult("select * from users where name='" + name + "';");
		if (user.next()) {
			int id = user.getInt("id");
			int role = user.getInt("role");
			String password = user.getString("pass");
			String email = user.getString("email");
			boolean usrStatus = user.getBoolean("bool_activestatus");
			String usrType = user.getString("usertype");
			User receivedUser = new User(name, password, email, role, usrStatus, usrType);
			return receivedUser;
		} else {
			throw new NoSuchElementException("No such user in database");
		}
	}

	// ## added by SS
	// ## for approval of normal user
	public User getUserForApproval() throws SQLException {
		ResultSet user = getResult("select * from users where bool_activestatus='" + false + "';");
		if (user.next()) {
			int id = user.getInt("id");
			String name = user.getString("name");
			String password = user.getString("pass");
			String email = user.getString("email");
			User receivedUser = new User(id, name, password, email);
			return receivedUser;
		} else {
			throw new NoSuchElementException("No User left for approval!");
		}
	}

	public Buyer getBuyerById(int id) throws SQLException, NoSuchElementException {
		ResultSet rs = getResult("select * from buyers1 where id='" + id + "';");
		if (rs.next()) {
			String title = rs.getString("title");
			String firstName = rs.getString("firstName");
			String lastName = rs.getString("lastName");
			String company = rs.getString("company");
			String proprietor = rs.getString("proprietor");
			String mobile = rs.getString("mobile");
			String mobile2 = rs.getString("mobile2");
			String email = rs.getString("email");
			String shopno = rs.getString("shopno");
			String city = rs.getString("city");
			String email2 = rs.getString("email2");
			String parentCompany = rs.getString("parentCompany");
			String paymentMethod = rs.getString("paymentMethod");
			System.out.println("paymentMethod:::" + paymentMethod);
			String creditPeriod = rs.getString("creditPeriod");
			String buyerType = rs.getString("buyerType");

			Buyer receivedBuyer = new Buyer(id, title, firstName, lastName, company, proprietor, mobile, mobile2, email,
					shopno, city, email2, parentCompany, paymentMethod, creditPeriod, buyerType, 0.0);
			// Blob photo = rs.getBlob("photo");
			// if (photo != null)
			// {
			// receivedBuyer.setImageStream(photo.getBinaryStream());
			// }

			return receivedBuyer;
		} else {
			throw new NoSuchElementException("No Buyer user in database");
		}
	}

	public List<Buyer> getBuyers() throws SQLException {
		List<Buyer> list = new ArrayList<>();
		ResultSet rs = getResult("select * from buyers1;");
		while (rs.next()) {
			Integer id = rs.getInt("id");
			String title = rs.getString("title");
			String firstName = rs.getString("firstName");
			String lastName = rs.getString("lastName");
			String company = rs.getString("company");
			String proprietor = rs.getString("proprietor");
			String mobile = rs.getString("mobile");
			String mobile2 = rs.getString("mobile2");
			String email = rs.getString("email");
			String shopno = rs.getString("shopno");
			String city = rs.getString("city");
			String email2 = rs.getString("email2");
			String parentCompany = rs.getString("parentCompany");
			String paymentMethod = rs.getString("paymentMethod");
			String creditPeriod = rs.getString("creditPeriod");
			String buyerType = rs.getString("buyerType");
			String mileston = rs.getString("milestone");
			Buyer receivedBuyer = new Buyer(id, title, firstName, lastName, company, proprietor, mobile, mobile2, email,
					shopno, city, email2, parentCompany, paymentMethod, creditPeriod, buyerType,
					Double.parseDouble(mileston));

			String photo = rs.getString("photo");
			if (photo != null) {
				receivedBuyer.setImagePath(photo);
			}
			list.add(receivedBuyer);
		}
		return list;
	}

	public void updateTableEntry(String tableName, int lineId, String[] cols, String[] values, boolean skipFirst,
			String auditLogMsg) {
		String sqlCommand = "UPDATE " + tableName + " SET ";
		int valuesStartIndex = 0;
		if (skipFirst) {
			valuesStartIndex = 1;
		}
		sqlCommand += cols[0] + "='" + values[valuesStartIndex] + "'";
		/*
		 * skip 1st value which is the sql autogenerated id, that mustn't be
		 * altered by user
		 */
		for (int i = valuesStartIndex + 1; i < cols.length; i++) {
			sqlCommand += "," + cols[i] + "=?";
		}
		sqlCommand += " where id='" + lineId + "'";
		String oldValues = null;
		try {
			if (tableName.equals("buyerDeals")) {
				DBuyerTableLine line = getBuyerDealEntry(lineId);
				oldValues = line == null ? null : line.serialize();
			} else if (tableName.equals("supplierDeals")) {
				DSupplierTableLine line = getSupplierDealEntry(lineId);
				oldValues = line == null ? null : line.serialize();
			}
		} catch (SQLException | NoSuchElementException ex) {
			Logger.getLogger(DatabaseClient.class.getName()).log(Level.SEVERE, null, ex);
		}
		final String oldValuesFinal = oldValues;
		try {
			System.out.println(sqlCommand);
			PreparedStatement statement = connection.prepareStatement(sqlCommand);
			for (int i = valuesStartIndex + 1; i < cols.length; i++) {
				statement.setString(i - valuesStartIndex, values[i + valuesStartIndex]);
			}
			statement.executeUpdate();
			String auditMsg;
			if (auditLogMsg == null) {
				auditMsg = String.format("UPDATED Entry for %s (Entry No: %d )", TABLE_MAP.get(tableName), lineId);
			} else {
				auditMsg = auditLogMsg;
			}
			if (!"".equals(auditMsg)) {
				insertAuditRecord(new AuditLog(lineId, getCurrentUser(), new Date() , auditMsg, tableName, lineId) {
					{
						setOldValues(oldValuesFinal);
					}
				});
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.print("sql exception in updateEntry " + e.getMessage());
		}
	}

	// ## added by ss
	public void bankAccountUpdate(String number, String accountName, int accountType, double balance,
			double initBalance, String bankName, String phone, String description, int lastupdated, int id) {
		String sql = "update accounts set acc_number='" + number + "',acc_name='" + accountName + "',acc_type='"
				+ accountType + "',balance='" + balance + "',initbalance='" + initBalance + "', bank_name='" + bankName
				+ "',phone='" + phone + "',description='" + description + "',lastupdated='" + lastupdated
				+ "'where id='" + id + "'";
		try {
			Statement smt = connection.createStatement();
			smt.executeUpdate(sql);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void updateTableEntry(String tableName, int lineId, String[] cols, String[] values, boolean skipFirst) {
		updateTableEntry(tableName, lineId, cols, values, skipFirst, null);
	}

	public void updateTableEntry(String tableName, int lineId, String col, String value, String auditLogMsg) {
		String sqlCommand = "UPDATE " + tableName + " SET ";
		sqlCommand += col + "='" + value + "'";
		sqlCommand += " WHERE id='" + lineId + "'";

		try {
			PreparedStatement statement = connection.prepareStatement(sqlCommand);
			statement.executeUpdate();
			String normalizeTabName = TABLE_MAP.get(tableName);
			if (auditLogMsg != null) {
				if (!"".equals(auditLogMsg)) {
					insertAuditRecord(new AuditLog(0, getCurrentUser(), new Date(), auditLogMsg, tableName, lineId));
				}
				return;
			}
			insertAuditRecord(new AuditLog(0, getCurrentUser(), new Date(), "UPDATED Entry for ".concat(normalizeTabName),
					tableName, lineId));
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.print("sql exception in updateEntry " + e.getMessage());
		}
	}

	public void deleteTableEntries(String tablename, String keywordName, String keywordValue, boolean resetIdColumn,
			boolean writeAuditLog) {
		try {
			String deleteCommand = null, findCommand = null;
			if (!keywordName.equals("ALL") || !keywordValue.equals("ALL")) {
				findCommand = "select * from " + tablename + " where " + keywordName + "='" + keywordValue + "';";
				deleteCommand = "delete from " + tablename + " where " + keywordName + "='" + keywordValue + "';";
			} else {
				findCommand = "select * from " + tablename;
				deleteCommand = "delete from " + tablename;
			}
			int oldRowsNum = getRowsNum(tablename);
			int newRowsNum = oldRowsNum;
			ResultSet rowsToDelete = getResult(findCommand);
			// find how many rows are left
			while (rowsToDelete.next()) {
				newRowsNum--;
			}
			connection.prepareStatement(deleteCommand).executeUpdate();
			if (writeAuditLog) {
				insertAuditRecord(new AuditLog(0, getCurrentUser(), new Date(),
						"DELETED Entry for ".concat(TABLE_MAP.get(tablename)), null, 0));
			}
			if (!resetIdColumn) {
				return;
			}
			// reorganize the id column
			resetIdColumn(tablename, oldRowsNum);
			String reseedIdCommand = "DBCC CHECKIDENT ('" + tablename + "', RESEED, " + newRowsNum + ");";
			connection.prepareStatement(reseedIdCommand).executeUpdate();
		} catch (SQLException e) {
			System.out.print("sql exception in deleteTableEntries " + e.getMessage());
		}
	}

	public void deleteTableEntries(String tablename, String keywordName, String keywordValue, boolean resetIdColumn) {
		deleteTableEntries(tablename, keywordName, keywordValue, resetIdColumn, true);
	}

	public void resetIdColumn(String tablename, int oldRowsNum) throws SQLException {
		// prepare sql table with identity for changes
		PreparedStatement statement = connection
				.prepareStatement("ALTER TABLE " + tablename + " SWITCH TO temp_" + tablename + ";");
		statement.executeUpdate();

		int newid = 1;
		for (int rowind = 1; rowind <= oldRowsNum; rowind++) {
			// check if there is a line with this id
			ResultSet res = getResult("select * from temp_" + tablename + " where id='" + rowind + "';");
			if (!res.next()) {
				continue;
			}
			statement = connection.prepareStatement(
					"update temp_" + tablename + " set id='" + (newid++) + "' where id='" + rowind + "';");
			// System.out.println("update temp_"+tablename+" set id='"+newid+"'
			// where id='"+rowind+"';");
			statement.executeUpdate();
		}

		statement = connection.prepareStatement("ALTER TABLE temp_" + tablename + " SWITCH TO " + tablename + ";");
		statement.executeUpdate();
	}

	public Buyer getBuyerByName(String name) throws SQLException, NoSuchElementException {
		ResultSet rs = getResult("select * from buyers1 where title='" + name + "';");
		// System.out.print("buyer title="+name+"\n");
		if (rs.next()) {
			Integer id = rs.getInt("id");
			String title = name;
			String firstName = rs.getString("firstName");
			String lastName = rs.getString("lastName");
			String company = rs.getString("company");
			String proprietor = rs.getString("proprietor");
			String mobile = rs.getString("mobile");
			String mobile2 = rs.getString("mobile2");
			String email = rs.getString("email");
			String shopno = rs.getString("shopno");
			String city = rs.getString("city");
			String email2 = rs.getString("email2");
			String parentCompany = rs.getString("parentCompany");
			String paymentMethod = rs.getString("paymentMethod");
			String creditPeriod = rs.getString("creditPeriod");
			String buyerType = rs.getString("buyerType");

			Buyer receivedBuyer = new Buyer(id, title, firstName, lastName, company, proprietor, mobile, mobile2, email,
					shopno, city, email2, parentCompany, paymentMethod, creditPeriod, buyerType, 0.0);

			String photo = rs.getString("photo");
			if (photo != null) {
				receivedBuyer.setImagePath(photo);
			}
			return receivedBuyer;
		} else {
			throw new NoSuchElementException("No Buyer user in database");
		}
	}

	public Supplier getSupplierByName(String name) throws SQLException, NoSuchElementException {
		ResultSet rs = getResult("select * from suppliers1 where title='" + name + "';");
		if (rs.next()) {
			Integer id = rs.getInt("id");
			String title = name;
			String firstName = rs.getString("firstName");
			String lastName = rs.getString("lastName");
			String company = rs.getString("company");
			String proprietor = rs.getString("proprietor");
			String mobile = rs.getString("mobile");
			String mobile2 = rs.getString("mobile2");
			String email = rs.getString("email");
			String village = rs.getString("village");
			String po = rs.getString("po");
			String tehsil = rs.getString("tehsil");
			String ac = rs.getString("ac");
			String bank = rs.getString("bank");
			String ifsc = rs.getString("ifsc");

			Supplier receivedSupplier = new Supplier(id, title, firstName, lastName, company, proprietor, mobile,
					mobile2, email, village, po, tehsil, ac, bank, ifsc);
			String photo = rs.getString("photo");
			if (photo != null) {
				receivedSupplier.setImagePath(photo);
			}
			return receivedSupplier;
		} else {
			throw new NoSuchElementException("No Supplier user in database");
		}
	}

	public Supplier getSupplierById(int id) throws SQLException, NoSuchElementException {
		ResultSet rs = getResult("select * from suppliers1 where id='" + id + "';");
		if (rs.next()) {
			String title = rs.getString("title");
			String firstName = rs.getString("firstName");
			String lastName = rs.getString("lastName");
			String company = rs.getString("company");
			String proprietor = rs.getString("proprietor");
			String mobile = rs.getString("mobile");
			String mobile2 = rs.getString("mobile2");
			String email = rs.getString("email");
			String village = rs.getString("village");
			String po = rs.getString("po");
			String tehsil = rs.getString("tehsil");
			String ac = rs.getString("ac");
			String bank = rs.getString("bank");
			String ifsc = rs.getString("ifsc");
			String photo = rs.getString("photo");

			Supplier receivedSupplier = new Supplier(id, title, firstName, lastName, company, proprietor, mobile,
					mobile2, email, village, po, tehsil, ac, bank, ifsc);

			if (photo != null) {
				receivedSupplier.setImagePath(photo);
			}
			return receivedSupplier;
		} else {
			throw new NoSuchElementException("No supplier user in database");
		}
	}

	private ResultSet getResult(String query) throws SQLException {
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(query);
		return resultSet;
	}

	public void saveSupplier(Supplier supplier) throws SQLException {
		String firstName = supplier.getFirstName();
		int id = getRowsNum("suppliers1");
		String title = String.valueOf(id).concat(" ").concat(firstName);
		try {// check if supplier exists
			this.getSupplierByName(title);
			GeneralMethods.errorMsg("Saving failed - title already exists in database");
			return;
		} catch (NoSuchElementException e) {
		}

		String lastName = supplier.getLastName();
		String company = supplier.getCompany();
		String proprietor = supplier.getProprietor();
		String mobile = supplier.getMobile();
		String mobile2 = supplier.getMobile2();
		String email = supplier.getEmail();
		String village = supplier.getVillage();
		String po = supplier.getPo();
		String tehsil = supplier.getTehsil();
		String ac = supplier.getAc();
		String bank = supplier.getBank();
		String ifsc = supplier.getIfsc();
		String sql = "insert into suppliers1 (title,firstName,lastName,company,proprietor,mobile,mobile2,email,village,po,tehsil,ac,bank,ifsc, photo) "
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, title);
		statement.setString(2, firstName);
		statement.setString(3, lastName);
		statement.setString(4, company);
		statement.setString(5, proprietor);
		statement.setString(6, mobile);
		statement.setString(7, mobile2);
		statement.setString(8, email);
		statement.setString(9, village);
		statement.setString(10, po);
		statement.setString(11, tehsil);
		statement.setString(12, ac);
		statement.setString(13, bank);
		statement.setString(14, ifsc);
		statement.setString(15, supplier.getImagePath());

		statement.executeUpdate();
		insertAuditRecord(new AuditLog(0, getCurrentUser(), new Date(), "ADDED new entry for supplier :".concat(title),
				"Suppliers1", getGeneratedKey(statement)));

	}

	public int getRowsNum(String tablename) {
		int rowsNum = 0;
		try {
			ResultSet result = getResult("SELECT COUNT(*) FROM " + tablename);
			while (result.next()) {
				rowsNum = result.getInt(1);
			}
		} catch (SQLException e) {
			System.out.print("sql exception in getRowsNum\n");
		}

		System.out.println("rowsNum:::" + rowsNum);
		return rowsNum;
	}

	/**
	 * Returns the highest id value used by grower number
	 *
	 * @return the highest id value, or 0 if there are no rows in the table
	 * @throws SQLException
	 */
	public int getNextTransIdForFreshEntry() throws SQLException {
		ResultSet set = getResult("select max(id) from arrival ;");
		if (!set.next()) {
			// Looks like there are no rows, so it seems to be first entry
			return 0;
		}
		return set.getInt(1);
	}

	/*
	 * Inserts the new fruit object only if there is no entry made earlier for
	 * the same fruit
	 */
	public int addFruit(String fruit) {
		int id = -1;
		Boolean autoCommit = null;
		try {
			PreparedStatement ps = connection.prepareStatement(INSERT_FRUIT_QRY);
			autoCommit = connection.getAutoCommit();
			connection.setAutoCommit(true);
			if (fruit == null || fruit.trim().isEmpty()) {
				throw new IllegalArgumentException("Fruit name cannot be empty");
			}
			ps.setString(1, fruit);
			ps.setString(2, fruit);
			// ps.executeUpdate();
			ps.execute();
			insertAuditRecord(
					new AuditLog(getCurrentUser(), new Date(), "ADDED Entry for fruit:".concat(fruit), null, 0));

			ps.close();
			ps = connection.prepareStatement("Select id from fruits where name=?");
			ps.setString(1, fruit);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				id = rs.getInt(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (autoCommit != null) {
				try {
					connection.setAutoCommit(autoCommit);
				} catch (SQLException ignored) {
				}
			}
		}
		return id;
	}

	/**
	 * Adds the qualities to table
	 *
	 * @param qualities
	 * @author Shoeb
	 */
	// ## chnaged by ss
	private void addFruitQualities(List<String> qualities) {
		try (PreparedStatement ps = connection.prepareStatement(INSERT_QUALITY_QRY)) {
			for (String quality : qualities) {
				if (quality == null || quality.trim().isEmpty()) {
					continue;
				}
				ps.setString(1, quality);
				ps.setString(2, quality);
				ps.execute();
			}
			ps.executeBatch();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public List<String> getAllFruitTypes() {
		List<String> fNames = new ArrayList<>();
		String sql = "Select name from fruits;";
		try {
			ResultSet rs = getResult(sql);
			while (rs.next()) {
				fNames.add(rs.getString(1));
			}
		} catch (Exception ex) {
			GeneralMethods.errorMsg("Error getting fruit details from DB:" + ex.getMessage());
		}
		return fNames;
	}

	// ## changed by ss
	public void addFruitQualities(String fruitName, List<String> qualityStrings) {
		int fruitId = addFruit(fruitName);
		Map<String, QualityType> existingQualitiesMap = getDetailForQuality(qualityStrings);
		List<String> newQualityToInsertList = new ArrayList<>();
		for (String qualName : qualityStrings) {
			if (!existingQualitiesMap.containsKey(qualName)) {
				newQualityToInsertList.add(qualName);
			}
		}
		
		addFruitQualities(newQualityToInsertList);
		List<QualityType> combinedQualityList = new ArrayList<>();
		if (!newQualityToInsertList.isEmpty()) {
			Map<String, QualityType> newlyInsertedQuality = getDetailForQuality(newQualityToInsertList);
			System.out.println("Newly Inserted quality");
			newlyInsertedQuality.values().forEach(action ->  System.out.println(action));
			combinedQualityList.addAll(newlyInsertedQuality.values());
		}
		
		
		System.out.println("old Inserted quality");
		existingQualitiesMap.values().forEach(action ->  System.out.println(action));
		
		
		combinedQualityList.addAll(existingQualitiesMap.values());
		try (PreparedStatement ps = connection.prepareStatement(INSERT_FRUIT_QUALITY_QRY)) {
			StringBuilder sb = new StringBuilder();
			for (QualityType qt : combinedQualityList) {
				ps.setInt(1, fruitId);
				ps.setInt(2, qt.getId());
				ps.setInt(3, fruitId);
				ps.setInt(4, qt.getId());
				ps.execute();
				sb.append(qt.getName() + " , ");

			}
			ps.executeBatch();
			ps.execute();
			insertAuditRecord(
					new AuditLog(0, getCurrentUser(), new Date(), "ADDED Fruit Qualities :".concat(sb.toString()), null, 0));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Use this method to create a new list of box size types for a fruit, if
	 * box size types with the same name are found in the DB, then the old box
	 * size types would be used, otherwise new entries would be created. This
	 * method also associates the specified fruit with the passed in box size
	 * types.
	 *
	 * @param fruitName
	 * @param boxSizeStrings
	 */
	public void addFruitBoxSizes(String fruitName, List<String> boxSizeStrings) {
		int fruitId = addFruit(fruitName);
		Map<String, BoxSize> existingBoxSizeMap = getDetailForBoxSize(boxSizeStrings);
		List<String> newBoxToInsertList = new ArrayList<>();
		for (String bsName : boxSizeStrings) {
			if (!existingBoxSizeMap.containsKey(bsName)) {
				newBoxToInsertList.add(bsName);
			}
		}
		addBoxSizes(newBoxToInsertList);
		List<BoxSize> combinedBoxSizeList = new ArrayList<>();
		if (!newBoxToInsertList.isEmpty()) {
			Map<String, BoxSize> newlyInsertedBox = getDetailForBoxSize(newBoxToInsertList);
			combinedBoxSizeList.addAll(newlyInsertedBox.values());
		}
		combinedBoxSizeList.addAll(existingBoxSizeMap.values());
		try (PreparedStatement ps = connection.prepareStatement(INSERT_FRUIT_BOX_QRY)) {
			for (BoxSize bs : combinedBoxSizeList) {
				ps.setInt(1, fruitId);
				ps.setInt(2, bs.getId());
				ps.setInt(3, fruitId);
				ps.setInt(4, bs.getId());
				// ps.addBatch();
				ps.execute();
			}
			ps.executeBatch();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void addBoxSizes(List<String> boxSizes) {
		try (PreparedStatement ps = connection.prepareStatement(INSERT_BOX_SIZE_QRY)) {
			for (String boxSize : boxSizes) {
				if (boxSize == null || boxSize.trim().isEmpty()) {
					continue;
				}
				boxSize = boxSize.toLowerCase();

				ps.setString(1, boxSize);
				ps.setString(2, boxSize);
				// ps.addBatch();
				ps.execute();
			}
			ps.executeBatch();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Returns box size object for give box size name
	 *
	 * @param inBoxSizes
	 * @return
	 */
	public Map<String, BoxSize> getDetailForBoxSize(List<String> inBoxSizes) {
		Map<String, BoxSize> map = new LinkedHashMap<>();
		StringBuilder selectQry = new StringBuilder("Select * from boxSizes where name in (");
		if (inBoxSizes == null || inBoxSizes.isEmpty()) {
			throw new IllegalArgumentException("Empty box size list passed");
		}
		for (String bs : inBoxSizes) {
			if (bs == null || bs.trim().isEmpty()) {
				continue;
			}
			selectQry.append("'").append(bs.trim().toLowerCase()).append("'").append(",");
		}
		selectQry.deleteCharAt(selectQry.lastIndexOf(","));
		selectQry.append(")");
		try {
			ResultSet rs = getResult(selectQry.toString());
			while (rs.next()) {
				BoxSize box = new BoxSize();
				box.setId(rs.getInt("id"));
				box.setName(rs.getString("name"));
				map.put(box.getName(), box);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return map;
	}

	public Map<String, QualityType> getDetailForQuality(List<String> inQualityList) {
		Map<String, QualityType> map = new LinkedHashMap<>();
		StringBuilder selectQry = new StringBuilder("Select * from qualities where name in (");
		if (inQualityList == null || inQualityList.isEmpty()) {
			throw new IllegalArgumentException("Empty quality  list passed");
		}
		for (String qual : inQualityList) {
			if (qual == null || qual.trim().isEmpty()) {
				continue;
			}
			selectQry.append("'").append(qual.trim().toLowerCase()).append("'").append(",");
		}
		selectQry.deleteCharAt(selectQry.lastIndexOf(","));
		selectQry.append(")");
		try {
			ResultSet rs = getResult(selectQry.toString());
			while (rs.next()) {
				QualityType quality = new QualityType();
				quality.setId(rs.getInt("id"));
				quality.setName(rs.getString("name"));
				map.put(quality.getName(), quality);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return map;
	}

	public List<BoxSize> getBoxSizesForFruit(String fruitName) {
		List<BoxSize> boxSizes = new ArrayList<>();
		String boxesForFruitQry = "select * from boxSizes bs where bs.id in (select boxSize_id from fruitBoxSizes where fruit_id in(select id from fruits where name=?) )";
		try (PreparedStatement ps = connection.prepareStatement(boxesForFruitQry)) {
			ps.setString(1, fruitName);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				BoxSize bs = new BoxSize();
				bs.setId(rs.getInt("id"));
				bs.setName(rs.getString("name"));
				boxSizes.add(bs);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return boxSizes;
	}

	public List<QualityType> getQualityTypesForFruit(String fruitName) {
		List<QualityType> qualityTypes = new ArrayList<>();
		try (PreparedStatement ps = connection.prepareStatement(SELECT_FRUIT_QUALITY_QRY)) {
			ps.setString(1, fruitName);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				QualityType qt = new QualityType();
				qt.setId(rs.getInt("id"));
				qt.setName(rs.getString("name"));
				qualityTypes.add(qt);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return qualityTypes;
	}

	public void deleteExpenseInfo(String name) {
		try (PreparedStatement ps = connection.prepareStatement("DELETE FROM expenseInfo WHERE name = ?")) {
			ps.setString(1, name);
			ps.executeUpdate();
			insertAuditRecord(new AuditLog(0, getCurrentUser(), new Date(), "DELETED exepense info :".concat(name), null, 0));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void deleteBuyerExpenseInfo(String name) {
		try (PreparedStatement ps = connection.prepareStatement("DELETE FROM buyerExpenseInfo  WHERE name = ?")) {
			ps.setString(1, name);
			ps.executeUpdate();
			insertAuditRecord(
					new AuditLog(0, getCurrentUser(), new Date(), "DELETED buyer exepense info :".concat(name), null, 0));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void updateExpenseInfo(String name, String type, String defaultAmount) {

		try (PreparedStatement ps = connection
				.prepareStatement("UPDATE expenseInfo SET type=?,  defaultAmount=?  WHERE name = ?")) {
			ps.setString(1, type);
			ps.setString(2, defaultAmount);
			ps.setString(3, name);
			ps.executeUpdate();
			insertAuditRecord(new AuditLog(0, getCurrentUser(), new Date(), "UPDATED exepense info :".concat(name), null, 0));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		addExpenseInfo(name, type, defaultAmount);
	}

    public void updateBuyerExpenseInfo(String name, String type, String defaultAmount) {
         try (PreparedStatement ps = connection
                .prepareStatement("UPDATE buyerExpenseInfo SET type=?,  defaultAmount=?  WHERE name = ?")) {
            ps.setString(1, type);
            ps.setString(2, defaultAmount);
            ps.setString(3, name);
            ps.executeUpdate();
            insertAuditRecord(new AuditLog(0, getCurrentUser(), new Date(), "UPDATED buyer exepense info :".concat(name), null, 0));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        addBuyerExpenseInfo(name, type, defaultAmount);
    }

    public void addExpenseInfo(String name, String type, String defaultAmount) {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_EXPENSE_INFO_QRY)) {
            ps.setString(1, name);
            ps.setString(2, name);
            ps.setString(3, type);
            ps.setString(4, defaultAmount);
            ps.executeUpdate();
            insertAuditRecord(new AuditLog(0, getCurrentUser(), new Date(), "ADDED exepense info :".concat(name), null, 0));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void addBuyerExpenseInfo(String name, String type, String defaultAmount) {
		try (PreparedStatement ps = connection.prepareStatement(INSERT_BUYER_EXPENSE_INFO_QRY)) {
			ps.setString(1, name);
			ps.setString(2, type);
			ps.setString(3, defaultAmount);
			ps.setString(4, name);
			ps.executeQuery();
			insertAuditRecord(
					new AuditLog(0, getCurrentUser(), new Date(), "ADDED buyerExpenseInfo info :".concat(name), null, 0));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public ExpenseInfo getExpenseInfoFor(String name) {
		ExpenseInfo ei = new ExpenseInfo();
		try {
			PreparedStatement ps = connection.prepareStatement("Select * FROM expenseInfo  WHERE name = ?");
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ei.setId(rs.getInt("id"));
				ei.setName(rs.getString("name"));
				ei.setType(rs.getString("type"));
				ei.setDefaultAmount(rs.getString("defaultAmount"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ei;
	}

	public ExpenseInfo getBuyerExpenseInfoFor(String name) {
		ExpenseInfo ei = new ExpenseInfo();
		try {
			PreparedStatement ps = connection.prepareStatement("Select * FROM buyerExpenseInfo  WHERE name = ?");
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ei.setId(rs.getInt("id"));
				ei.setName(rs.getString("name"));
				ei.setType(rs.getString("type"));
				ei.setDefaultAmount(rs.getString("defaultAmount"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ei;
	}

	public List<ExpenseInfo> getExpenseInfoList() {
		List<ExpenseInfo> list = new ArrayList<>();
		try {
			ResultSet rs = getResult("Select * from expenseinfo  order by name");
			while (rs.next()) {
				ExpenseInfo ei = new ExpenseInfo();
				ei.setId(rs.getInt("id"));
				ei.setName(rs.getString("name"));
				ei.setType(rs.getString("type"));
				ei.setDefaultAmount(rs.getString("defaultAmount"));
				list.add(ei);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<ExpenseInfo> getBuyerExpenseInfoList() {
		List<ExpenseInfo> list = new ArrayList<>();
		try {
			ResultSet rs = getResult("Select * from buyerExpenseInfo order by name");
			while (rs.next()) {
				ExpenseInfo ei = new ExpenseInfo();
				ei.setId(rs.getInt("id"));
				ei.setName(rs.getString("name"));
				ei.setType(rs.getString("type"));
				ei.setDefaultAmount(rs.getString("defaultAmount"));
				list.add(ei);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public void deleteFruitDetails(String fruitName) {
		String sqlDelFruitQuals = "Delete from fruitQuality where fruit_id in(select id from fruits where name=?)";
		String sqlDelFruitBoxTypes = "Delete from fruitBoxSizes where fruit_id in(select id from fruits where name=?)";
		String[] queries = new String[] { sqlDelFruitBoxTypes, sqlDelFruitQuals };
		try {
			for (String query : queries) {
				PreparedStatement ps = connection.prepareStatement(query);
				ps.setString(1, fruitName);
				ps.executeUpdate();
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public void deleteFruit(String fruitName) {
		String query = "Delete from  fruits where name=?";
		try {
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setString(1, fruitName);
			ps.executeUpdate();
			insertAuditRecord(
					new AuditLog(0, getCurrentUser(), new Date(), "DELETED fruit entry:".concat(fruitName), null, 0));
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		deleteFruitDetails(fruitName);
	}

	public void addCompany(Company company) {
		String sql = "Select count(*) from companyInfo;";
		try (ResultSet rs = getResult(sql)) {
			if (rs.next()) {
				if (rs.getInt(1) < 1) {
					addOrUpdateCompanyInfo(company, true);
					insertAuditRecord(new AuditLog(0, getCurrentUser(), new Date(),
							"ADDED company:".concat(company.getName()), null, 0));

				} else {
					addOrUpdateCompanyInfo(company, false);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateCompany(Company company) {
		addOrUpdateCompanyInfo(company, false);
		insertAuditRecord(
				new AuditLog(0, getCurrentUser(), new Date(), "UPDATED company:".concat(company.getName()), null, 0));

	}

	private void addOrUpdateCompanyInfo(Company company, boolean isNew) {
		String sql = "";
		if (isNew) {
			sql = INSERT_COMPANY_QRY;
		} else {
			if (company.getLogo() != null) {
				sql = UPDATE_COMPANY_QRY;
			} else {
				sql = UPDATE_NOLOGO_COMPANY_QRY;
			}
		}
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, company.getName());
			ps.setString(2, company.getAddress());
			ps.setString(3, company.getWebsite());
			ps.setString(4, company.getPhone());
			ps.setString(5, company.getEmail());
			ps.setString(6, company.getIndustryType());
			ps.setString(7, company.getPassword());
			if (company.getLogo() != null || isNew) {
				ps.setBlob(8, company.getLogo());
				if (!isNew) {
					ps.setString(9, company.getName());
				}
			} else if (company.getLogo() == null && !isNew) {
				ps.setString(8, company.getName());
			}
			ps.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Integer addMoneyPaidRecdInfo(MoneyPaidRecd mpr, String auditLogMsg) {
		String sql = INSERT_PARTY_MONEY_QRY;
		try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, mpr.getTitle());
			ps.setString(2, mpr.getPartyType());
			ps.setString(3, mpr.getDate());
			ps.setString(4, mpr.getPaymentMode());
			ps.setString(5, mpr.getPaid());
			ps.setString(6, mpr.getReceived());
			ps.setString(7, mpr.getBankName());
			ps.setString(8, mpr.getChequeNo());
			ps.setString(9, mpr.getDepositDate());
			ps.setString(10, mpr.getIsAdvanced());
			ps.setString(11, mpr.getDescription());
			// ps.setBlob(12, mpr.getReceipt());

			ps.execute();
			String auditMsg;
			if (auditLogMsg != null && !auditLogMsg.isEmpty()) {
				auditMsg = auditLogMsg;
			} else if ("0".equals(mpr.getPaid())) {
				auditMsg = "MONEY received from " + mpr.getTitle();
			} else {
				auditMsg = "MONEY paid to " + mpr.getTitle();
			}
			int key = getGeneratedKey(ps);
			insertAuditRecord(new AuditLog(0, getCurrentUser(), new Date(), auditMsg, "partyMoney", key));
			return key;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public Integer addMoneyPaidRecdInfo(MoneyPaidRecd mpr) {
		return this.addMoneyPaidRecdInfo(mpr, "");
	}

	public Company getCompany() {
		String sql = "Select * from companyInfo;";
		Company c = null;
		try (ResultSet rs = getResult(sql)) {
			if (rs.next()) {
				c = new Company();
				c.setAddress(rs.getString("address"));
				c.setEmail(rs.getString("email"));
				c.setIndustryType(rs.getString("industryType"));
				Blob blob = rs.getBlob("logo");
				if (blob != null) {
					c.setLogo(blob.getBinaryStream());
				}
				c.setName(rs.getString("name"));
				c.setPassword(rs.getString("password"));
				c.setPhone(rs.getString("phone"));
				c.setWebsite(rs.getString("website"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return c;
	}

	public LadaanBijakSaleDeal getLadBijSaleDeal(int dealId) {
		String sql = "select * from ladaanBijakSaleDeals where dealId=?;";
		LadaanBijakSaleDeal deal = null;
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, dealId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				deal = prepareLadBijObjFromRs(rs);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return deal;
	}

	public List<LadaanBijakSaleDeal> getLadBijSaleDealsForBuyer(String buyerTitle) {
		String sql = "select * from  ladaanBijakSaleDeals where dealID in (select dealID from buyerDeals where buyerTitle = ?);";
		List<LadaanBijakSaleDeal> list = new ArrayList<>();
		LadaanBijakSaleDeal deal = null;
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, buyerTitle);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				deal = prepareLadBijObjFromRs(rs);
				list.add(deal);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	private LadaanBijakSaleDeal prepareLadBijObjFromRs(ResultSet rs) throws SQLException {
		LadaanBijakSaleDeal deal = new LadaanBijakSaleDeal();
		deal.setAggregatedAmount(rs.getString("aggregatedAmount"));
		deal.setAmountedTotal(rs.getString("totalAmount"));
		// c.setAmountReceived(rs.getString("industryType"));
		deal.setBuyerRate(rs.getString("buyerRate"));
		deal.setCases(rs.getString("boxes"));
		deal.setDealId(String.valueOf(rs.getInt("dealID")));
		deal.setSaleNo(rs.getInt("id"));
		deal.setFreight(rs.getString("freight"));
		deal.setComission(rs.getString("comission"));
		deal.setDate(rs.getString("dealDate"));
		return deal;

	}

	public int getNonEditedLadaanEntries() {
		int count = 0;
		try {
			ResultSet rs = getResult(QRY_DEALS_FOR_LADAAN_BUYER);
			List<Integer> buyerDealIds = new ArrayList<>();
			while (rs.next()) {
				buyerDealIds.add(rs.getInt(1));
			}
			if (buyerDealIds.isEmpty()) {
				return 0;
			}
			String ladanSql = "select count(*) from  ladaanBijakSaleDeals where dealID in (";
			for (int idx = 0; idx < buyerDealIds.size(); idx++) {
				Integer dealId = buyerDealIds.get(idx);
				ladanSql = ladanSql.concat(" " + dealId);
				if (idx < buyerDealIds.size() - 1) {
					ladanSql = ladanSql.concat(",");
				}
			}
			ladanSql = ladanSql.concat(");");
			ResultSet ladanRs = getResult(ladanSql);
			int ladaanCount = 0;
			if (ladanRs.next()) {
				ladaanCount = ladanRs.getInt(1);
			}
			count = buyerDealIds.size() - ladaanCount;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}

	public List<MoneyPaidRecd> getMoneyPaidRecdList(String partyTitle, EntityType pType) {
		List<MoneyPaidRecd> list = new ArrayList<>();
		String query = QRY_PAID_RECD_MONEY_FOR_PARTY + (partyTitle == null ? "" : QRY_PAID_RECD_MONEY_FOR_TITLE) + ";";
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, pType.getValue().trim().toLowerCase());
			if (partyTitle != null) {
				ps.setString(2, partyTitle.toLowerCase().trim());
			}
			ResultSet rs = ps.executeQuery();
			addMprRsToList(rs, list);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return list;
	}

	public List<MoneyPaidRecd> getAllMoneyPaidRecdList(EntityType pType) {
		List<MoneyPaidRecd> list = new ArrayList<>();
		try (PreparedStatement ps = connection.prepareStatement(QRY_PAID_RECD_MONEY_FOR_PARTY)) {
			ps.setString(1, pType.getValue().trim().toLowerCase());
			ResultSet rs = ps.executeQuery();
			addMprRsToList(rs, list);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return list;
	}

	public List<MoneyPaidRecd> getAdvanceMoneyPaidList(EntityType pType) {
		List<MoneyPaidRecd> list = new ArrayList<>();
		try (PreparedStatement ps = connection
				.prepareStatement("Select * from partyMoney where PartyType=? and isAdvanced='true' ")) {
			ps.setString(1, pType.getValue().trim().toLowerCase());
			ResultSet rs = ps.executeQuery();
			addMprRsToList(rs, list);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return list;
	}

	public MoneyPaidRecd getMoneyPaidRecd(int id) {
		MoneyPaidRecd value = new MoneyPaidRecd();
		try (PreparedStatement ps = connection.prepareStatement("Select * from partymoney where id=?")) {
			// changed by ss
			// ps.setString(1, id + "");
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				value.setDate(rs.getString("date"));
				value.setId(rs.getInt("id"));
				value.setPaid(rs.getString("paid"));
				value.setReceived(rs.getString("received"));
				value.setTitle(rs.getString("title"));
				value.setPartyType(rs.getString("partyType"));
				value.setDescription(rs.getString("description"));
				value.setPaymentMode(rs.getString("paymentMode"));
				value.setBankName(rs.getString("bankName"));
				value.setChequeNo(rs.getString("chequeNo"));
				value.setDepositDate(rs.getString("depositDate"));
				Blob blob = rs.getBlob("receipt");
				if (blob != null) {
					value.setReceipt(blob.getBinaryStream());
				}
			} else {
				throw new NoSuchElementException("No selected Id in partyMoney table");
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return value;
	}

	public boolean deleteMoneyPaidRecd(int id) {
		String sql = "DELETE FROM partyMoney WHERE id=?";
		try (final PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, id);
			ps.executeUpdate();
			insertAuditRecord(new AuditLog(0, getCurrentUser(), new Date(), "DELETED paid/received entry:" + id, null, 0));
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void addMprRsToList(ResultSet rs, List<MoneyPaidRecd> list) throws SQLException {
		while (rs.next()) {
			MoneyPaidRecd mpr = new MoneyPaidRecd();
			mpr.setDate(rs.getString("date"));
			mpr.setId(rs.getInt("id"));
			mpr.setPaid(rs.getString("paid"));
			mpr.setReceived(rs.getString("received"));
			mpr.setTitle(rs.getString("title"));
			mpr.setPartyType(rs.getString("partyType"));
			mpr.setDescription(rs.getString("description"));
			mpr.setPaymentMode(rs.getString("paymentMode"));
			mpr.setBankName(rs.getString("bankName"));
			mpr.setChequeNo(rs.getString("chequeNo"));
			mpr.setDepositDate(rs.getString("depositDate"));
			Blob blob = rs.getBlob("receipt");
			if (blob != null) {
				mpr.setReceipt(blob.getBinaryStream());
			}
			list.add(mpr);
		}
	}

	public void addDealCharges(Map<String, ChargeTypeValueMap> map, int dealID) {
		String chargeInsertQry = "Insert into charges (chargeName, value, chargeType, chargeRate) VALUES (?, ?,?,?)";
		String chargeDealEntryQuery = "Insert into dealCharges (chargeID,dealID) VALUES (?, ?) ";
		for (String chargeName : map.keySet()) {
			try (PreparedStatement ps1 = connection.prepareStatement(chargeInsertQry, Statement.RETURN_GENERATED_KEYS);
					PreparedStatement ps2 = connection.prepareStatement(chargeDealEntryQuery);) {
				ps1.setString(1, chargeName);
				ps1.setString(2, map.get(chargeName).totalValue.getText().toLowerCase());
				ps1.setString(3, map.get(chargeName).type.getValue().toLowerCase());
				ps1.setString(4, map.get(chargeName).rate.getText().trim());
				ps1.executeUpdate();

				ResultSet rsGen = ps1.getGeneratedKeys();
				if (!rsGen.next()) {
					throw new IllegalStateException("The database failed to return generated key for charges");
				}
				ps2.setInt(1, rsGen.getInt(1));
				ps2.setInt(2, dealID);
				ps2.executeUpdate();

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void addStorageBuyerDealInfo(Integer buyerDealLineId, Integer strorageDealLineId) {
		try (PreparedStatement ps = connection.prepareStatement(
				"INSERT INTO storageBuyerDeals (" + "buyerDealLineId,   strorageDealLineId)  VALUES (?, ?)")) {
			ps.setInt(1, buyerDealLineId);
			ps.setInt(2, strorageDealLineId);
			ps.executeUpdate();

		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public List<StorageBuyerDeal> getStorageBuyerDeals() {
		List<StorageBuyerDeal> list = new ArrayList<>();
		try (PreparedStatement ps = connection.prepareStatement("Select * from storageBuyerDeals;")) {
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				StorageBuyerDeal deal = new StorageBuyerDeal(rs.getInt(1), rs.getInt(2));
				list.add(deal);
			}
		} catch (SQLException x) {
			x.printStackTrace();
		}
		return list;
	}

	public StorageBuyerDeal getStorageBuyerDeal(Integer id) {
		try (PreparedStatement ps = connection.prepareStatement("Select * from storageBuyerDeals;")) {
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				StorageBuyerDeal deal = new StorageBuyerDeal(rs.getInt(1), rs.getInt(2));
				return deal;
			}
		} catch (SQLException x) {
			x.printStackTrace();
		}
		return null;
	}

	public List<Charge> getDealCharges(int dealID) {
		List<Charge> list = new ArrayList<>();
		String dealChargeQuery = "select * from charges where id in (select chargeID from dealCharges where dealID = ?);";
		try (PreparedStatement ps = connection.prepareStatement(dealChargeQuery)) {
			ps.setInt(1, dealID);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Charge chg = new Charge();
				chg.setId(rs.getInt("id"));
				chg.setName(rs.getString("chargeName"));
				chg.setAmount(rs.getString("value"));
				chg.setType(rs.getString("chargeType"));
				chg.setRate(rs.getString("chargeRate"));
				list.add(chg);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public Template getTemplate() {
		Template template = null;
		return template;

	}

	public void saveTemplate(Template template) {
		String query = "Select  * from templates  where accountName=?";
		String sql = "Insert into templates (accountName,dateCol, chqnoCol ,descriptionCol ,withdrawalCol ,depositCol ,balanceCol, transIdCol) VALUES (?,?,?,?,?,?,?,?) ";
		Template t = template;
		try (PreparedStatement queryPs = connection.prepareStatement(query);
				PreparedStatement ps = connection.prepareStatement(sql)) {
			queryPs.setString(1, t.getAccountName());
			ResultSet rs = queryPs.executeQuery();
			if (rs.next()) {
				System.out.println("Existing template found, updating...");
				updateTemplate(t);
				return;
			}
			ps.setString(1, t.getAccountName());
			ps.setInt(2, t.getDateCol());
			ps.setInt(3, t.getChqnoCol());
			ps.setInt(4, t.getDescriptionCol());
			ps.setInt(5, t.getWithdrawalCol());
			ps.setInt(6, t.getDepositCol());
			ps.setInt(7, t.getBalanceCol());
			ps.setInt(8, t.getTransIdCol());
			ps.executeUpdate();
			insertAuditRecord(new AuditLog(0, getCurrentUser(), new Date(),
					"ADDED Template for account:".concat(template.getAccountName()), null, 0));
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public void updateTemplate(Template template) {
		String sql = "UPDATE templates set dateCol= ? , chqnoCol= ?  ,descriptionCol = ? ,withdrawalCol = ? ,depositCol = ? ,balanceCol= ? WHERE accountName = ?";
		Template t = template;
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, t.getDateCol());
			ps.setInt(2, t.getChqnoCol());
			ps.setInt(3, t.getDescriptionCol());
			ps.setInt(4, t.getWithdrawalCol());
			ps.setInt(5, t.getDepositCol());
			ps.setInt(6, t.getBalanceCol());
			ps.setString(7, t.getAccountName());
			ps.executeUpdate();
			insertAuditRecord(new AuditLog(0, getCurrentUser(), new Date(),
					"UPDATED Template for account:".concat(template.getAccountName()), null, 0));

			System.out.println("Template updated");
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public void deleteExpenditureType(String name) {
		final String sql = "DELETE FROM expenditureType WHERE name = ?";
		try (final PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, name);
			ps.executeUpdate();
			insertAuditRecord(
					new AuditLog(0, getCurrentUser(), new Date(), "DELETED expenditure type:".concat(name), null, 0));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addExpenditureType(String name) {
		try (final PreparedStatement ps = connection.prepareStatement(INSERT_EXPENDITURE_TYPE_QRY)) {
			ps.setString(1, name);
			ps.setString(2, name);
			ps.executeUpdate();
			insertAuditRecord(new AuditLog(0, getCurrentUser(), new Date(), "ADDED expenditure type:".concat(name), null, 0));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<String> getExpenditureTypeList() {
		final String sql = "SELECT * FROM expendituretype";
		List<String> list = new ArrayList<>();
		try {
			ResultSet rs = getResult(sql);
			while (rs.next()) {
				list.add(rs.getString("name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public boolean addExpenditure(Expenditure xpr) {
		String sql = INSERT_EXPENDITURE_QRY;
		try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, xpr.getAmount());
			ps.setString(2, xpr.getDate());
			ps.setString(3, xpr.getComment());
			ps.setString(4, xpr.getPayee());
			ps.setString(5, xpr.getType());
			// ps.setBlob(6, xpr.getReceipt());

			ps.execute();
			insertAuditRecord(new AuditLog(0, getCurrentUser(), new Date(), "Expenditure entry recorded in system",
					"expenditures", getGeneratedKey(ps)));
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	private String getCurrentUser() {
		SessionDataController session = SessionDataController.getInstance();
		User user = session.getCurrentUser();
		String userName = "tzt";
		if (user != null) {
			userName = session.getCurrentUser().getName();
		}
		return userName;
	}

	public List<Expenditure> getExpenditureList() {
		String sql = "select * from expenditures;";
		List<Expenditure> list = new ArrayList<>();
		try {
			ResultSet rs = getResult(sql);
			while (rs.next()) {
				Expenditure xpr = new Expenditure();
				xpr.setAmount(rs.getString("amount"));
				xpr.setComment(rs.getString("comment"));
				xpr.setDate(rs.getString("date"));
				xpr.setId(rs.getInt("id"));
				xpr.setPayee(rs.getString("billto"));
				Blob blob = rs.getBlob("receipt");
				if (blob != null) {
					xpr.setReceipt(blob.getBinaryStream());
				}
				xpr.setType(rs.getString("type"));
				list.add(xpr);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return list;
	}

	public Expenditure getExpenditureById(int id) {
		String sql = "SELECT * FROM expenditures WHERE id=?;";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				Expenditure xpr = new Expenditure();
				xpr.setAmount(rs.getString("amount"));
				xpr.setComment(rs.getString("comment"));
				xpr.setDate(rs.getString("date"));
				xpr.setId(rs.getInt("id"));
				xpr.setPayee(rs.getString("billto"));
				Blob blob = rs.getBlob("receipt");
				if (blob != null) {
					xpr.setReceipt(blob.getBinaryStream());
				}
				xpr.setType(rs.getString("type"));
				return xpr;
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public void deleteExpenditureEntry(int id, boolean writeAuditLog) {
		try {
			String tableName = "expenditures";
			String deleteCommand;
			Expenditure expenditure = getExpenditureById(id);
			if (expenditure == null) {
				return;
			}
			deleteCommand = "delete from " + tableName + " where id=?;";
			PreparedStatement statement = connection.prepareStatement(deleteCommand);
			statement.setInt(1, id);
			statement.execute();
			if (writeAuditLog) {
				insertAuditRecord(new AuditLog(0, getCurrentUser(), new Date(),
						"DELETED Entry for " + TABLE_MAP.get(tableName) + " (" + expenditure.getType() + ")", null, 0) {
					{
						setName(expenditure.getPayee());
						try {
							String format = DateUtil.determineDateFormat(expenditure.getDate());
							DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
							setDate(Date.from(LocalDate.parse(expenditure.getDate(), formatter)
									.atStartOfDay(ZoneId.systemDefault()).toInstant()));
						} catch (Exception x) {
							setDate(null);
							x.printStackTrace();
						}
						setAmount(Double.parseDouble(expenditure.getAmount()));
					}
				});
			}
		} catch (SQLException e) {
			System.out.print("sql exception in deleteTableEntries " + e.getMessage());
		}
	}

	public AccountEntryPayment getAccountEntryPayment(int id) {
		String sql = "SELECT * FROM accountEntryPayments WHERE id=?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return new AccountEntryPayment(rs.getInt("id"), rs.getInt("account_entry_id"),
						rs.getString("payment_table"), rs.getInt("payment_id"));
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
		return new AccountEntryPayment();
	}

	public AccountEntryPayment getAccountEntryPayment(String paymentTable, int paymentId) {
		String sql = "SELECT * FROM accountEntryPayments WHERE payment_table=? AND payment_id=?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, paymentTable);
			ps.setInt(2, paymentId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return new AccountEntryPayment(rs.getInt("id"), rs.getInt("account_entry_id"),
						rs.getString("payment_table"), rs.getInt("payment_id"));
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
		return new AccountEntryPayment();
	}

	public List<AccountEntryPayment> getAccountEntryPayments(int accountEntryId) {
		List<AccountEntryPayment> result = new ArrayList<>();
		String sql = "SELECT * FROM accountEntryPayments WHERE account_entry_id=?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, accountEntryId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				result.add(new AccountEntryPayment(rs.getInt("id"), rs.getInt("account_entry_id"),
						rs.getString("payment_table"), rs.getInt("payment_id")));
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
		return result;
	}

	public Integer addAccountEntryPayment(AccountEntryPayment payment) {
		String sql = "INSERT INTO accountEntryPayments (account_entry_id, payment_table, payment_id)  VALUES (?, ?, ?)";
		try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, payment.getAccountEntryId());
			// ps.setInt(1, 0);
			ps.setString(2, payment.getPaymentTable());
			ps.setInt(3, payment.getPaymentId());
			ps.executeUpdate();
			return getGeneratedKey(ps);
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public boolean deleteAccountPayment(int id) {
		String sql = "DELETE FROM accountEntryPayments WHERE id=?";
		try (final PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, id);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean deleteAccountPaymentByEntryId(int accountEntryId) {
		String sql = "DELETE FROM accountEntryPayments WHERE account_entry_id=?";
		try (final PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, accountEntryId);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean deleteAccountEntryPaymentByEntryId(int entryId) {
		String sql = "DELETE FROM accountEntryPayments WHERE account_entry_id=?";
		try (final PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, entryId);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public int getStorageDealsCount(String storeType) {
		String sql = "select sum(CAST (boxes AS bigint)) from buyerDeals group by buyerTitle having buyerTitle=?;";
		int result = 0;
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, storeType);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getInt(1);
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
		return result;
	}

    //## for audit log insert
    //## changed by ss
    public void insertAuditRecord(AuditLog log) {
    	String sql = "INSERT INTO auditLog (userId, eventDetail, eventtime, "
                + " eventObjectId, oldValues, newValues, name, date, amount) "
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement ps = connection.prepareStatement(sql);) {
            ps.setString(1, log.getUserId());
            ps.setString(2, log.getEventDetail());
            ps.setDate(3, new java.sql.Date (log.getEventTime().getTime()));
            ps.setInt(4, log.getEventObjectId());
            ps.setString(5, log.getOldValues());
            ps.setString(6, log.getNewValues());
            ps.setString(7, log.getName());
            ps.setTimestamp(8, log.getDate() == null ? null : new java.sql.Timestamp(log.getDate().getTime()));
//            Date(8, log.getDate() == null ? null : new java.sql.Date(log.getDate().getTime()));
            ps.setDouble(9, log.getAmount() == null ? 0.0 : log.getAmount());
            //ps.executeUpdate();
            ps.execute();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public List<AuditLog> getAuditRecords() {
        String sql = "SELECT * FROM auditlog";
        List<AuditLog> list = new ArrayList<>();
        try {
            ResultSet rs = getResult(sql);
            while (rs.next()) 
            {
                AuditLog log = new AuditLog(rs.getInt(1), 
                		                    rs.getString("userid"),
                		                    rs.getDate("eventtime"),
                		                    rs.getString("eventdetail"), 
                		                    rs.getString("eventobject"),
                		                    rs.getInt("eventobjectid")) 
                {{//are outside the constructor
                            setOldValues(rs.getString("oldvalues"));
                            setNewValues(rs.getString("newvalues"));
                            setName(rs.getString("name"));
                            setDate(rs.getDate("date") == null ? null : new Date(rs.getDate("date").getTime()));
                            setAmount(rs.getDouble("amount"));
                }};
                list.add(log);
            }

		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return list;
	}

	private static int getGeneratedKey(PreparedStatement ps) throws SQLException {
		ResultSet genKeyRs = ps.getGeneratedKeys();
		if (genKeyRs != null && genKeyRs.next()) {
			return genKeyRs.getInt(1);
		}
		System.err.println(String.format("Unable to get the generated id for the object %s, setting it to 0",
				ps.getMetaData().getTableName(1)));
		return 0;

	}

	private static final String INSERT_EXPENDITURE_TYPE_QRY = "INSERT INTO expenditureType (name) SELECT ? WHERE NOT EXISTS (SELECT 1 FROM expenditureType WHERE expenditureType.name = ?)";

	private static final String QRY_PAID_RECD_MONEY_FOR_PARTY_ = "Select * from partyMoney where RTRIM(LTRIM(LOWER(partyType))) = ? ";

	private static final String QRY_PAID_RECD_MONEY_FOR_TITLE = " AND RTRIM(LTRIM(LOWER(title))) = ? ";

	private static final String QRY_PAID_RECD_MONEY_FOR_PARTY = "Select * from partyMoney where RTRIM(LTRIM(LOWER(partyType))) = ?";

	private static final String QRY_DEALS_FOR_LADAAN_BUYER = "select dealID from buyerDeals where buyerTitle in (select title from buyers1 where RTRIM(LTRIM(LOWER(buyerType))) IN ('ladaan', 'bijak'))";

	// private static final String INSERT_QUALITY_QRY = "IF NOT EXISTS (SELECT *
	// FROM qualities WHERE name = ? COLLATE SQL_Latin1_General_CP1_CI_AS)
	// INSERT INTO qualities (name) VALUES (?)";
	private static final String INSERT_QUALITY_QRY = "select case when (count(name)=0) then (select qualities_enter(?)) end from qualities where name =?";
	private static final String INSERT_COMPANY_QRY = "INSERT INTO companyInfo ("
			+ "name ,   address ,   website ,   phone ,  email ,  industryType ,  password ,  logo )  VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String UPDATE_COMPANY_QRY = "UPDATE companyInfo SET "
			+ "name=? ,   address =? ,   website = ?,   phone = ?,  email = ?,  industryType = ?,  password = ?,  logo = ? WHERE  name = ?";

	private static final String UPDATE_NOLOGO_COMPANY_QRY = "UPDATE companyInfo SET "
			+ "name=? ,   address =? ,   website = ?,   phone = ?,  email = ?,  industryType = ?,  password = ?   WHERE  name = ?";

	// ## changed by ss on 23-Dec-2017(all queries below are not supported by
	// postgres so changed.)

	// private static final String INSERT_BOX_SIZE_QRY = "IF NOT TRUE (SELECT *
	// FROM boxSizes WHERE name = ?) INSERT INTO boxSizes (name) VALUES (?)";
	private static final String INSERT_BOX_SIZE_QRY = "select case when (count(name) = 0) then (select boxsize_entry(?)) end from boxsizes where name = ?";

	// private static final String INSERT_FRUIT_BOX_QRY = "IF NOT EXISTS (SELECT
	// * FROM fruitBoxSizes WHERE fruit_id = ? AND boxSize_id = ?) Insert into
	// fruitBoxSizes (fruit_id, boxSize_id) values (?,?)";
	private static final String INSERT_FRUIT_BOX_QRY = "select case when  (count(boxsize_id) = 0) then (select fruitboxsize_entry(?,?)) end from fruitboxsizes where fruit_id = ? AND boxSize_id = ?";

	// private static final String INSERT_FRUIT_QUALITY_QRY = "IF NOT EXISTS
	// (SELECT * FROM fruitQuality WHERE fruit_id = ? AND quality_id = ?) Insert
	// into fruitQuality (fruit_id,quality_id) values (?,?)";
	private static final String INSERT_FRUIT_QUALITY_QRY = "select case when (count(quality_id)=0) then (fruitquality(?,?)) end from fruitquality where fruit_id = ? and quality_id = ?";
	// private static final String INSERT_FRUIT_QRY = "IF NOT EXISTS (SELECT *
	// FROM fruits WHERE name = ? COLLATE SQL_Latin1_General_CP1_CI_AS) INSERT
	// INTO fruits (name) VALUES (?)";
	private static final String INSERT_FRUIT_QRY = "select case when (count(name)=0) then (select fruit_entry(?)) end from fruits where name=?";

	private static final String SELECT_FRUIT_QUALITY_QRY = "select * from qualities qt where qt.id in (select quality_id from fruitQuality where fruit_id in(select id from fruits where name=?) )";

	private static final String INSERT_EXPENSE_INFO_QRY = "INSERT INTO expenseInfo (name, type, defaultAmount) SELECT ?,?,? WHERE NOT EXISTS (SELECT 1 FROM expenseInfo WHERE expenseInfo.name = ?)";
	// changed by ss
	// blocked the picture saving facilities
	private static final String INSERT_PARTY_MONEY_QRY = "INSERT INTO partyMoney ("
			+ "title , partyType , date , paymentMode , paid , received, bankName , chequeNo , depositDate , isAdvanced , description ) VALUES (?,?,?,?,?,?,?,?,?,?,?)";

	// private static final String INSERT_BUYER_EXPENSE_INFO_QRY = "IF NOT
	// EXISTS (SELECT * FROM buyerExpenseInfo WHERE name = ?) INSERT INTO
	// buyerExpenseInfo (name, type, defaultAmount ) VALUES (?,?,?)";
	private static final String INSERT_BUYER_EXPENSE_INFO_QRY = "INSERT INTO buyerExpenseInfo (name, type, defaultAmount )SELECT ?,?,? WHERE NOT EXISTS (SELECT 1 FROM buyerExpenseInfo WHERE buyerExpenseInfo.name = ?)";

	private static final String INSERT_EXPENDITURE_QRY = "INSERT INTO expenditures  ("
			+ "amount ,date , comment , billto , type) VALUES (?, ?, ?, ?, ?); ";

}