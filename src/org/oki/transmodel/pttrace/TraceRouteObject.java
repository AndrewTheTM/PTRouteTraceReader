/**
 * 
 */
package org.oki.transmodel.pttrace;

import java.util.ArrayList;

/**
 * @author arohne
 *
 */
public class TraceRouteObject {
	int I;
	int J;
	int UserClass;
	int Transfers;
	int Operator;
	double Cost;
	ArrayList<String> Routes1;
	ArrayList<String> Routes2;
	ArrayList<String> Routes3;
	ArrayList<String> Routes4;

	/**
	 * @param I From Zone
	 * @param J To Zone
	 * @param UserClass Transit User Class
	 */
	TraceRouteObject(int I, int J, int UserClass){
		this.I=I;
		this.J=J;
		this.UserClass=UserClass;
		this.Transfers=0;
		this.Operator=0;
		this.Cost=0.0;
		this.Routes1=new ArrayList<String>();
		this.Routes2=new ArrayList<String>();
		this.Routes3=new ArrayList<String>();
		this.Routes4=new ArrayList<String>();
	}
}
