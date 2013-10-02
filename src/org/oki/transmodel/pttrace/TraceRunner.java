/**
 * 
 */
package org.oki.transmodel.pttrace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hexiong.jdbf.DBFWriter;
import com.hexiong.jdbf.JDBFException;
import com.hexiong.jdbf.JDBField;
import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;

/**
 * @author arohne
 *
 */
public class TraceRunner {

	static ArrayList<TraceRouteObject> tro;
	static Hashtable<String,Integer> routeList;
	static ArrayList<ArrayList<String>> workObject;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length!=3){
			System.out.println("\n\nUSAGE: TraceRunner <line DBF> <REPORTO File> <Output DBF File>\n\n");
			System.exit(1);
		}
		
		
		String[] inputFieldMap={"Name","Operator"};
		ArrayList<Object[]> dbfOutput=new ArrayList<Object[]>();
		routeList=new Hashtable<String,Integer>();
		try {
			dbfOutput=readDBF(args[0],inputFieldMap);
			for(Object[] o:dbfOutput){
				Double d=Double.parseDouble((String) o[1].toString());
				routeList.put(((String) o[0]).trim(), d.intValue());
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (DBFException e1) {
			e1.printStackTrace();
		}
		
		
		tro=new ArrayList<TraceRouteObject>();
		
		//This is 8 because 8 userclasses are allowed in PT.  Note that workObject.get(0)=UserClass1
		workObject=new ArrayList<ArrayList<String>>();
		for(int i=0;i<8;i++)
			workObject.add(new ArrayList<String>());
		
		try {
			readTextFile(args[1]);

			List<Future<TraceRouteObject>> futuresList = new ArrayList<Future<TraceRouteObject>>();
			int nrOfProcessors=Runtime.getRuntime().availableProcessors();
			ExecutorService eservice = Executors.newFixedThreadPool(nrOfProcessors);
			for(int c=0;c<workObject.size();c++){
				ArrayList<String> ial=workObject.get(c);
				for(String s:ial)
					futuresList.add(eservice.submit(new parseTraceText(s,c,routeList)));
			}
			Object taskResult;
			for(Future<TraceRouteObject> f:futuresList){
				try{
					taskResult=f.get();
					if(taskResult instanceof TraceRouteObject){
						tro.add((TraceRouteObject) taskResult);
					}
				}catch(InterruptedException e){
					e.printStackTrace();
				}catch(ExecutionException e){
					e.printStackTrace();
				}finally{
					eservice.shutdown();
				}
			}
			
			writeDBF(args[2], tro);
			
			System.out.println("Done!");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}
	
	private static void readTextFile(String fileName) throws IOException{
		BufferedReader br=new BufferedReader(new FileReader(new File(fileName)));
		String line;
		Pattern userClassPattern=Pattern.compile("User Class ([0-9])");
		Pattern rEvalPattern=Pattern.compile("REval Route\\(s\\) from Origin ([0-9]*) to Destination ([0-9]*)");
		
		boolean collect=false;
		String holding="";
		int UC=0;
		int elCount=0;
		while((line=br.readLine())!=null){
			Matcher mUserClass=userClassPattern.matcher(line);
			Matcher mREval=rEvalPattern.matcher(line);	
			if(mUserClass.find()){
				collect=false;
				UC=(Integer.parseInt(mUserClass.group(1)))-1;
			}
			if(mREval.find()){
				collect=true;
				holding+=line+"\n";
			}else if(collect){
				if(line.length()==0){
					if(elCount==0)
						elCount+=1;
					else if(elCount>0){
						elCount=0;
						workObject.get(UC).add(holding);
						collect=false;
						holding="";
					}
				}else{
					holding+=line+"\n";
				}
			}
			
		}
		br.close();
	}
	
	/**
	 * Reads the selected contents of a DBF and stuffs them into an arraylist of arrays of objects
	 * @param DBFFileName The file path to the DBF to read
	 * @param inputFieldMap A String array of fields to read
	 * @return An Arraylist of Object arrays loaded with the contents
	 * @throws FileNotFoundException if the DBF file is not found
	 * @throws DBFException if there is a different problem with the DBF
	 */
	static ArrayList<Object[]> readDBF(String DBFFileName, String[] inputFieldMap) throws FileNotFoundException, DBFException{
		InputStream iStream = new FileInputStream(DBFFileName);
		DBFReader reader=new DBFReader(iStream);
		int numberOfFields=reader.getFieldCount();
		ArrayList<Object[]> outObj=new ArrayList<Object[]>();
		
		Object[] row;
		int rowCount=0;
		while((row=reader.nextRecord())!=null){
			rowCount++;
			if((rowCount % 10000)==0 || rowCount==1)
				System.out.println("Reading DBF row "+rowCount);
			Object[] newObject = new Object[inputFieldMap.length];
			for(int f=0;f<numberOfFields;f++){
				DBFField field=reader.getField(f);
				for(int ff=0;ff<inputFieldMap.length;ff++)
					if(field.getName().toLowerCase().equals(inputFieldMap[ff].toLowerCase())){
						newObject[ff]=row[f];
					}
			}
			outObj.add(newObject);
		}
		return outObj;
	}
	
	/**
	 * Writes an object out to a DBF
	 * @param DBFFileName The file path and name to write the DBF to
	 * @param objectToWrite The object to write
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws JDBFException
	 */
	static void writeDBF(String DBFFileName, ArrayList<?> objectToWrite) throws IllegalArgumentException, IllegalAccessException, IOException, JDBFException{
		Object o=objectToWrite.get(0);
		Class c=o.getClass();
		Field[] cdf=c.getDeclaredFields();
		JDBField[] jdbFields=new JDBField[5]; //cdf.length];
		int fldCount=0;
		String fieldDefs[]=new String[5]; //cdf.length];
		for(int fc=0;fc<5;fc++){
		//for(Field f:cdf){
			Field f=cdf[fc];
			switch(f.getType().toString()){
			case "int":
				jdbFields[fldCount]=new JDBField(f.getName().substring(0, Math.min(10,f.getName().length())),'N',20,0);
				fieldDefs[fldCount]="N";
				break;
			case "double":
				jdbFields[fldCount]=new JDBField(f.getName().substring(0, Math.min(10,f.getName().length())),'F',20,8);
				fieldDefs[fldCount]="F";
				break;
			case "java.lang.String":
				jdbFields[fldCount]=new JDBField(f.getName().substring(0, Math.min(10,f.getName().length())),'C',32,0);
				fieldDefs[fldCount]="C";
				break;
			case "java.util.Date":
				jdbFields[fldCount]=new JDBField(f.getName().substring(0, Math.min(10,f.getName().length())),'D',8,0);
				fieldDefs[fldCount]="D";
				break;
			case "boolean":
				jdbFields[fldCount]=new JDBField(f.getName().substring(0, Math.min(10,f.getName().length())),'C',6,0);
				fieldDefs[fldCount]="C";
				break;
			//default:
				//jdbFields[fldCount]=new JDBField(f.getName().substring(0, Math.min(10,f.getName().length())),'C',32,0);
				//fieldDefs[fldCount]="C";
				//break;
			}
			fldCount++;
		}
		
		DBFWriter writer=new DBFWriter(DBFFileName,jdbFields);
		
		fldCount=jdbFields.length;

		for(Object o2:objectToWrite){
			Object[] rowData=new Object[fldCount];
			Class c2=o2.getClass();
			Field[] fwa=c2.getDeclaredFields();
			int fldCnt=0;
			 //C, D, F, N
			for(int fc=0;fc<5;fc++){
			//for(Field fw:fwa){
				Field fw=fwa[fc];
				if(fw.get(o2)!=null){
					switch(fieldDefs[fldCnt]){
						case("C"):
							rowData[fldCnt]=fw.get(o2).toString().substring(0, Math.min(fw.get(o2).toString().length(), 32));
							break;
						case("D"):
							rowData[fldCnt]=fw.get(o2);
							break;
						case("L"):
							rowData[fldCnt]=fw.get(o2);
							break;
						case("N"):
							rowData[fldCnt]=Double.parseDouble(fw.get(o2).toString());
							break;
						case("F"):
							rowData[fldCnt]=Double.parseDouble(fw.get(o2).toString());
							break;
						default:
							rowData[fldCnt]=fw.get(o2);
							break;
					}
				}else
					rowData[fldCnt]="";
				fldCnt++;
			}
			writer.addRecord(rowData);		
		}
		writer.close();
	}

}
