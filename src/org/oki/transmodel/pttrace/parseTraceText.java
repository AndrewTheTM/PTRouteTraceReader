package org.oki.transmodel.pttrace;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class parseTraceText implements Callable<TraceRouteObject> {

	private String s;
	private int c;
	private static Hashtable<String,Integer> routeList;
	parseTraceText(String s, int c, Hashtable<String,Integer> h){
		this.s=s;
		this.c=c;
		parseTraceText.routeList=h;
	}
	
	public parseTraceText(Hashtable<String,Integer> h){
		parseTraceText.routeList=h;
	}
	@Override
	public TraceRouteObject call() throws Exception {
		int O=0, D=0;
		double cost=0.0;
		Pattern odPattern=Pattern.compile("REval Route\\(s\\) from Origin ([0-9]*) to Destination ([0-9]*)");
		Pattern linePattern=Pattern.compile("(.*) lines (.*)");
		Pattern costPattern=Pattern.compile("Cost=\\s?([0-9]*\\.[0-9]*) Probability=([0-1]\\.[0-9]*)");
		int rg=0;
		String[] lines = s.split("\n");
		String[] routeLists=new String[4];
		for(String line:lines){
			Matcher mOD=odPattern.matcher(line);
			Matcher mLine=linePattern.matcher(line);
			Matcher mCost=costPattern.matcher(line);
			if(mOD.matches()){
				O=Integer.parseInt(mOD.group(1));
				D=Integer.parseInt(mOD.group(2));
			}else if(mLine.matches()){
				routeLists[rg]=mLine.group(2);
			}else if(mCost.matches()){
				cost=Double.parseDouble(mCost.group(1));
			}
		}
		
		TraceRouteObject t=new TraceRouteObject(O,D,c);
		t.Cost=cost;
		if(routeLists[0]!=null && !routeLists[0].isEmpty())
			for(String route:routeLists[0].split("\\s"))
				t.Routes1.add(route);
		if(routeLists[1]!=null && !routeLists[1].isEmpty())
			for(String route:routeLists[1].split("\\s"))
				t.Routes2.add(route);
		if(routeLists[2]!=null && !routeLists[2].isEmpty())
			for(String route:routeLists[2].split("\\s"))
				t.Routes3.add(route);
		if(routeLists[3]!=null && !routeLists[3].isEmpty())
			for(String route:routeLists[3].split("\\s"))
				t.Routes4.add(route);
		
		//FIXME: At some point, maybe something a little less lame should be here ... this is critical now!
		
		
		t.Operator=(int) routeList.get(t.Routes1.get(0));
		
		return t;
	}
	
	/**
	 * Gets the operator of a route name.  Returns main operator from the line input DBF table or 0 if none found.
	 * @param r ArrayList<String> of routes to check for
	 * @return Integer value of main operator.  0=none found, other outputs=1..999. 
	 */
	public static int getOperator(ArrayList<String> r){
		/*
		 * BIG IMPORTANT NOTE
		 * This returns the MINIMUM operator number found.  This is because in my use, Operator 1 is the
		 * biggest and most common, Operator 2 is the second most, etc.
		 */
		//int opCount[]=new int[1000];
		int minOp=9999;
		for(String s:r)
			if(routeList.get(s)!=null)
				minOp=Math.min(routeList.get(s),minOp);
		if(minOp==9999)
			return 0;
		return minOp;
	}
}
