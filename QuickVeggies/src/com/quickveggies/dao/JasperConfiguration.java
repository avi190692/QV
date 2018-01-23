package com.quickveggies.dao;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;


public class JasperConfiguration {
	
	
	public void jasperReport(HttpServletRequest request,HttpServletResponse response,Map<String,Object> param,String fileName,String format)
	{
		// location of the file + filename
		String file =""+fileName;
		
		Connection conn=null;
		
		
		try 
		{
			//## 1
			Class.forName("org.postgresql.Driver");
			//## 2
			conn=DriverManager.getConnection("jdbc:postgresql://localhost:5432/qvdv","postgresql","postgresql");
			//## 3
			OutputStream ostream = response.getOutputStream();
			if(format.equals("csv"))
			{
				 response.setContentType("application/csv");
			}
			else if(format.equals("text"))
			{
				 response.setContentType("application/txt");
			}
			else if(format.equals("excel"))
			{
				 response.setContentType("application/xls");
			}
			else
			{
				 response.setContentType("application/pdf");
			}
			//## 4
			JasperPrint jprint = JasperFillManager.fillReport(file,param,conn);
			
			//## 5
			JRExporter exporter = new JRPdfExporter();
			           exporter.setParameter(JRExporterParameter.OUTPUT_STREAM,ostream);
			           exporter.setParameter(JRExporterParameter.JASPER_PRINT,jprint);
			           exporter.exportReport();
			           
			//## 6        
		    ostream.close();
		    //## 7
		    conn.close();                      
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
