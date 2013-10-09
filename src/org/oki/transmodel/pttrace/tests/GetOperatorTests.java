package org.oki.transmodel.pttrace.tests;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.junit.Test;
import org.oki.transmodel.pttrace.TraceRunner;
import org.oki.transmodel.pttrace.parseTraceText;

import com.linuxense.javadbf.DBFException;

public class GetOperatorTests {
	private static Hashtable<String,Integer> routeList;
	
	@Test
	public void test_1(){
		if(routeList==null)
			ReadLineDBF();
		parseTraceText ptt=new parseTraceText(routeList);
		System.out.println("Test if 1/6 is different");
		ArrayList<String>s=new ArrayList<String>();
		s.add("Rt17MAIB");
		s.add("Rt20IB"); 
		s.add("Rt21OB"); 
		s.add("Rt4XCTCIB"); 
		s.add("Rt21IB"); 
		s.add("Rt24OB");
		assertTrue(parseTraceText.getOperator(s)==1);
	}
	
	@Test
	public void test_2(){
		if(routeList==null)
			ReadLineDBF();
		parseTraceText ptt=new parseTraceText(routeList);
		System.out.println("Test if 2/6 is different");
		ArrayList<String>s=new ArrayList<String>();
		s.add("Rt17MAIB");
		s.add("Rt20IB"); 
		s.add("Rt21OB"); 
		s.add("Rt4XCTCIB"); 
		s.add("Rt3TankOB"); 
		s.add("Rt24OB");
		assertTrue(parseTraceText.getOperator(s)==1);
	}
	
	@Test
	public void test_3(){
		if(routeList==null)
			ReadLineDBF();
		parseTraceText ptt=new parseTraceText(routeList);
		System.out.println("Test if 2/2/2");
		ArrayList<String>s=new ArrayList<String>();
		s.add("Rt2XCTCIB");
		s.add("Rt20IB"); 
		s.add("Rt4XCTCIB"); 
		s.add("Rt3TankOB"); 
		s.add("Rt1TankOB");
		s.add("Rt21OB"); 
		assertTrue(parseTraceText.getOperator(s)==1);
	}
	
	@Test
	public void test_4(){
		if(routeList==null)
			ReadLineDBF();
		parseTraceText ptt=new parseTraceText(routeList);
		System.out.println("Test if a bad route");
		ArrayList<String>s=new ArrayList<String>();
		s.add("Rt2XCTCIB");
		s.add("Rt20IB"); 
		s.add("Rt4XCTCIB"); 
		s.add("Rt3TankOB"); 
		s.add("I'm bad, I'm bad, I'm really really bad");
		s.add("Rt21OB"); 
		assertTrue(parseTraceText.getOperator(s)==1);
	}
	
	@Test
	public void test_5(){
		if(routeList==null)
			ReadLineDBF();
		parseTraceText ptt=new parseTraceText(routeList);
		System.out.println("Test tank");
		ArrayList<String>s=new ArrayList<String>();
		s.add("Rt2XCTCIB"); 
		s.add("Rt3TankOB"); 
		s.add("Rt1TankOB"); 
		assertTrue(parseTraceText.getOperator(s)==2);
	}
	
	@Test
	public void test_6(){
		if(routeList==null)
			ReadLineDBF();
		parseTraceText ptt=new parseTraceText(routeList);
		System.out.println("Test CTC");
		ArrayList<String>s=new ArrayList<String>();
		s.add("Rt2XCTCIB");
		s.add("Rt4XCTCIB"); 
		assertTrue(parseTraceText.getOperator(s)==6);
	}
	
	@Test
	public void test_7(){
		if(routeList==null)
			ReadLineDBF();
		parseTraceText ptt=new parseTraceText(routeList);
		System.out.println("Test if all bad route");
		ArrayList<String>s=new ArrayList<String>();
		s.add("I'm bad, I'm bad, I'm really really bad");
		s.add("I'm worse");
		assertTrue(parseTraceText.getOperator(s)==0);
		
	}
	
	private static void ReadLineDBF(){
		String routeDBF="C:\\Modelrun\\10h10b10a08aV80\\LineList.dbf";
		String[] inputFieldMap={"Name","Operator"};
		ArrayList<Object[]> dbfOutput=new ArrayList<Object[]>();
		routeList=new Hashtable<String,Integer>();
		try {
			dbfOutput=TraceRunner.readDBF(routeDBF,inputFieldMap);
			for(Object[] o:dbfOutput){
				Double d=Double.parseDouble((String) o[1].toString());
				routeList.put(((String) o[0]).trim(), d.intValue());
			}
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (DBFException e1) {
			e1.printStackTrace();
		}
	}
}
