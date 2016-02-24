package Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import Client.User;

public class Auditer {
	
	private PrintWriter writer;
	private String location;
	private String todaysDateWithTime,todaysDateNoTime;
	private Calendar  cal;
	private SimpleDateFormat compactsdf, longsdf;
	
	public Auditer(){
		init();
	}
	
	private void init() {
		URL tempLocation = server.class.getProtectionDomain().getCodeSource().getLocation();
		location = "" + tempLocation;
		location = location.substring(5, location.length());		
		
		cal = Calendar.getInstance();
		compactsdf = new SimpleDateFormat("yyyyMMddHHmmss");
		longsdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		todaysDateWithTime = compactsdf.format(cal.getTime());
		todaysDateNoTime = (new SimpleDateFormat("yyyyMMdd")).format(cal.getTime());
		new File(location + "/AuditLogs/" + (todaysDateNoTime)).mkdirs();
		File file = new File(location + "/AuditLogs/" + todaysDateNoTime + "/Auditlog" + todaysDateWithTime);
		try {
			writer = new PrintWriter(new FileWriter(file, true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.println("Server started at " + (longsdf).format(cal.getTime()));
	}
	
	synchronized private void print(String s){
		try {
			writer = new PrintWriter(new FileWriter(location + "/AuditLogs/" + todaysDateNoTime + "/" + todaysDateWithTime, true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.append("\n" + (longsdf).format(cal.getTime()) + " - " + s);
		System.out.println(s);
		writer.close();
	}
	
	public void println(String s){
		print(s);
	}
	
	public void println(User user, String s){
		print("\"" + s + "\" from " + user.getID());
	}
	
	public void errorprintln(User user, String s){
		print("Error cause by " + user.getID() + ". Error message: " + s);
	}

}
