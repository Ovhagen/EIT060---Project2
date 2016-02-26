package Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.net.URL;


//import com.sun.corba.se.impl.legacy.connection.SocketFactoryAcceptorImpl;









import Client.Doctor;
import Client.Employee;
import Client.Government;
import Client.Nurse;
import Client.User;
import Client.Patient;

public class Database {

	public final static int GOVERNMENT = 3, DOCTOR = 2, NURSE = 1, USER = 0;
	private ArrayList<User> users;
	private HashMap<String, RecordEntry> records;
	private File file;
	private String location;
	private Auditer au;

	public Database(Auditer au){
		this.au = au;
		records = new HashMap<String, RecordEntry>();
		users = new ArrayList<>();
	}
	
	public void init() throws IOException {
		URL tempLocation = server.class.getProtectionDomain().getCodeSource().getLocation();
		location = "" + tempLocation;
		location = location.substring(5, location.length());		

		file = getLatestDatabase();
		if(file == null){
			file = new File(location + "DataBase/DataBase-infoFile.txt");
			saveDataBase();
			throw new IOException("There are no databases in the system. Created empty database");
		} else {
			au.println("Loading database " + file.getName());
			loadDataBase();
		}
	}


	synchronized private void loadDataBase() throws IOException {
		Scanner scan = null;
		try {
			scan = new Scanner(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = "";
		while (scan.hasNextLine() && !line.equals("-Users")) {
			line = scan.nextLine();
		}
		if(scan.hasNextLine()){
			line = scan.nextLine();
			if (line != null) {
				while (!line.equals("-Records")) {
					String[] infos = line.split(";");
					String userType = infos[0];
					String userID = infos[1];
					String name = infos[2];
					switch (userType) {
					case "G":
						Government government = new Government(userID, name);
						users.add(government);
						break;
					case "D":
						Doctor doctor = new Doctor(userID, name, new Integer(
								infos[3]));
						users.add(doctor);
						break;
					case "N":
						Nurse nurse = new Nurse(userID, name, new Integer(infos[3]));
						users.add(nurse);
						break;
					case "P":
						Patient patient = new Patient(userID, name);
						users.add(patient);
						break;
					}
					line = scan.nextLine();
				}
				if(scan.hasNextLine()){
					line = scan.nextLine();
				} else {
					line = null;
				}
				while (line != null) {
					String[] infos = line.split(";");
					String socialSecurityNumber = infos[0];
					int divisionID = new Integer(infos[3]);
					Record record = new Record(socialSecurityNumber, infos[1],
							infos[2], divisionID, infos[4]);
					int i = 5;
					int doctorID = -1;
					ArrayList<Integer> nurseIDs = new ArrayList<>();
					while (i < infos.length) {
						int employeeNumber = new Integer(infos[i]);
						if (employeeNumber >= 1000 && employeeNumber < 2000) {
							doctorID = employeeNumber;
						} else if (employeeNumber >= 2000){
							nurseIDs.add(employeeNumber);
						}
						i++;
					}
					RecordEntry recordEntry = new RecordEntry(record, divisionID,
							doctorID, nurseIDs, socialSecurityNumber);
					records.put(socialSecurityNumber, recordEntry);
					if (scan.hasNextLine()) {
						line = scan.nextLine();
					} else {
						line = null;
					}
				}
			}
		} else {
			throw new IOException("Could not load database properly");
		}

	}

	synchronized private void removeOldestFile(){
		File folder = new File(location + "/Database");
		File[] listOfFiles = folder.listFiles();
		long fileToRemove = Long.MAX_VALUE;
		ArrayList<String[]> dates = new ArrayList<>();
		if(listOfFiles.length > 7){
			for(File f : listOfFiles){
				if(f.getName().length() == 22){
					String d = f.getName().substring(8, f.getName().length());
					long date  = Long.parseLong(d);
					if(date < fileToRemove){
						fileToRemove = date;
					}
				}
			}
			File file = new File(location + "/Database/DataBase" + fileToRemove);
			boolean deleted = file.delete();
			if(deleted){
				au.println("DataBase" + fileToRemove + " was deleted");
			}
		}
	}
	
	private File getLatestDatabase(){
		File folder = new File(location + "/Database");
		File[] listOfFiles = folder.listFiles();
		long newestFile = 0;
		for(File f : listOfFiles){
			if(f.getName().length() == 22){
				String d = f.getName().substring(8, f.getName().length());
				long date  = Long.parseLong(d);
				if(date > newestFile){
					newestFile = date;
				}
			}
		}
		if(newestFile > 0){
			return new File(location + "/Database/DataBase" + newestFile);
		}else {
			return null;
		}
		
		
	}
	
	synchronized private void saveDataBase() throws IOException {
		File file = new File(location + "/Database/DataBase-infoFile.txt");
		BufferedReader scan = null;
		try {
			scan = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String todaysDate = sdf.format(cal.getTime());
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new File(location + "/Database/DataBase" + todaysDate));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = scan.readLine();
		while (line != null) {
			writer.println(line);
			line = scan.readLine();
		}
		scan.close();
		
		writer.println("-Users");
		ArrayList<String> socialSecurityNumbers = new ArrayList<>();
		for (User user : users) {
			String name = user.getName();
			String userID = user.getID();
			if(userID.length() < 5){ 		//Not a Patient
				int userType = new Integer(userID);
				if(userType < 1000){
					writer.println("G;" + userID + ";" + name);
				} else {
					Employee employee = (Employee) user;
					int division = employee.getDivisionID();
					if(userType < 2000){
						writer.println("D;" + userID + ";" + name + ";" + division);
					} else {
						writer.println("N;" + userID + ";" + name + ";" + division);
					}
				}
			} else{
				socialSecurityNumbers.add(userID);
				writer.println("P;" + userID + ";" + name);
			}
		}
		
		writer.println("-Records");
		
		for(String ssn : socialSecurityNumbers){
			RecordEntry re = records.get(ssn);
			if(re != null){
				Record record = re.getRecord();
				writer.print(ssn + ";" + record.getFirstName() + ";" + record.getSurName() + ";" + 
								re.getDivisionID() + ";" + record.getComment() + ";" + re.getDoctorID());
				ArrayList<Integer> nurseIDs = re.getNurseIDs();
				for(int id : nurseIDs){
					writer.print(";" + id);
				}
				writer.println();
			}
		}
		writer.close();
		au.println("Database" + todaysDate + " was created.");
		removeOldestFile();
	}

	public Record getRecord(String socialSecurityNumber, User user) throws NullPointerException, AuthorizationException {
		RecordEntry re = null;
		try {
			re = getRecordEntry(socialSecurityNumber, user);
		} catch (NullPointerException | AuthorizationException e) {if(e instanceof NullPointerException){
				throw new NullPointerException("These is no record for social security number " + socialSecurityNumber);
			} else {
				throw e;
			}
			
		}
		au.println(user, "Returned record " + socialSecurityNumber);
		return re.getRecord();
	}
	
	public RecordEntry getRecordEntry(String socialSecurityNumber, User user) throws NullPointerException, AuthorizationException {
		try {
			RecordEntry re = records.get(socialSecurityNumber);
			if (user instanceof Government) {
				return re;
			} else if (user instanceof Employee) {
				Employee employee = (Employee) user;
				if (employee.getDivisionID() == re.getDivisionID()) {
					return re;
				} else{
					throw new AuthorizationException("You do not have permission to see that record");
				}
			} else {
				if (re.getSocialSecurityNumber().equals(user.getID())){
					
					return re;
				} else {
					throw new AuthorizationException("You do not have permission to see that record");
				}
			}
		} catch(NullPointerException | AuthorizationException e) {
			throw e;
		}
	}

	public void addUser(User user){
		users.add(user);
		try {
			saveDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void putRecord(User user, String firstName, String surName, String comment,
			ArrayList<Integer> nurseIDs, String socialSecurityNumber) throws NullPointerException, AuthorizationException{
		if(!users.contains(user)){
			addUser(user);
		}  
		Patient patient = new Patient(socialSecurityNumber,firstName + " " + surName);
		if(!users.contains(patient)){
			addUser(patient);
		} else {
			throw new AuthorizationException("A patient with that socialSecurityNumber already exist.");
		}
		
		
		if(user instanceof Doctor){
			Doctor d = (Doctor) user;
			int doctorID =  new Integer(d.getDivisionID());
			Record record = new Record(socialSecurityNumber, firstName, surName,doctorID, comment);
			RecordEntry recordEntry = new RecordEntry(record, d.getDivisionID(), doctorID,
					nurseIDs, socialSecurityNumber);
			records.put(socialSecurityNumber, recordEntry);
			try {
				saveDataBase();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			throw new AuthorizationException("You do not have the athority to create a record");
		}
	}
	
	public void editDoctorACL(User user, String socialSecurityNumber, int doctorID) throws AuthorizationException{
		if(user instanceof Government){
			try{
				RecordEntry re = records.get(socialSecurityNumber);
				Record record = re.getRecord();
				RecordEntry newRe = new RecordEntry(record, re.getDivisionID(), doctorID, re.getNurseIDs(), socialSecurityNumber);
				records.remove(socialSecurityNumber);
				records.put(socialSecurityNumber, newRe);
				try {
					saveDataBase();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (NullPointerException e){
				throw new NullPointerException("No record for that social security number exists");
			}
		} else{
			throw new AuthorizationException("You do not have permission to edit doctor IDs");
		}
	}
	
	public void addNurseACL(User user, String socialSecurityNumber, ArrayList<Integer> nurseIDs) throws AuthorizationException{
		RecordEntry re = records.get(socialSecurityNumber);
		ArrayList<Integer> oldNurseIDs = re.getNurseIDs();
		Record record = re.getRecord();
		StringBuilder nurses = new StringBuilder();
		if(user instanceof Doctor){
			Doctor doctor = (Doctor) user;
			for(int nurseID : nurseIDs){
				nurses.append(" " + nurseID);
				if(!oldNurseIDs.contains(nurseID))
					oldNurseIDs.add(nurseID);
			}
			nurseIDs = oldNurseIDs;
			
			if(doctor.getDivisionID() == record.getDivisionID()){
				au.println(user, "Added nurses " + nurses.toString().trim());
				records.remove(socialSecurityNumber);
				RecordEntry newRe = new RecordEntry(record, record.getDivisionID(), re.getDoctorID(), nurseIDs, socialSecurityNumber);
				records.put(socialSecurityNumber, newRe);
				try {
					saveDataBase();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else {
				throw new AuthorizationException("You do not have permission to edit that ACL");
			}
		}
	}
	
	public void removeNurseACL(User user, String socialSecurityNumber, ArrayList<Integer> nurseIDs) throws AuthorizationException{
		RecordEntry re = records.get(socialSecurityNumber);
		Record record = re.getRecord();
		int oldDoctorID = re.getDoctorID();
		ArrayList<Integer> oldNurseIDs = re.getNurseIDs();
		StringBuilder nurses = new StringBuilder();
		if(user instanceof Doctor){
			Doctor doctor = (Doctor) user;
			Iterator<Integer> it = oldNurseIDs.iterator();
			for(Integer nurseID : nurseIDs){
				nurses.append(" " + nurseID);
				while(it.hasNext()){
					if(it.next().equals(nurseID)){
						it.remove();
						break;
					}
				}
			}
			nurseIDs = oldNurseIDs;
			if(doctor.getDivisionID() == record.getDivisionID()){
				au.println(user, "Removed nurses " + nurses.toString().trim());
				records.remove(socialSecurityNumber);
				RecordEntry newRe = new RecordEntry(record, record.getDivisionID(), oldDoctorID, nurseIDs, socialSecurityNumber);
				records.put(socialSecurityNumber, newRe);
				try {
					saveDataBase();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else {
				throw new AuthorizationException("You do not have permission to edit that ACL");
			}
		} else {
			throw new AuthorizationException("You do not have permission to edit ACLs for nurses");
		}
	}
	
	public ArrayList<Integer> getACL(User user, String socialSecurityNumber) throws AuthorizationException{
		ArrayList<Integer> acls = new ArrayList<Integer>();
		RecordEntry re = records.get(socialSecurityNumber);
		if(re == null){
			throw new NullPointerException("No record for that social security number exists");
		}
		
		ArrayList<Integer> nurseIDs = re.getNurseIDs();
		for(Integer nurseID : nurseIDs){
			acls.add(nurseID);
		}
		acls.add(re.getDoctorID());
		
		if(user instanceof Doctor){
			if(((Doctor) user).getDivisionID() == re.getRecord().getDivisionID()){
				au.println(user, "Returned ACL for " + socialSecurityNumber);
				return acls;
			}else {
				throw new AuthorizationException("You do not have permission to see that ACL");
			}
		} else if(user instanceof Government){
			au.println(user, "Returned ACL for " + socialSecurityNumber);
			return acls;
		} else {
			throw new AuthorizationException("You do not have permission to see ACLs");
		}
	}
	
	public ArrayList<Record> getAllAvailabe(User user) throws AuthorizationException{
		ArrayList<Record> tempRecords = new ArrayList<>();
		Iterator it = records.entrySet().iterator();

		Map.Entry pair = null;
		if(user instanceof Government){
			while (it.hasNext()) {
				pair = (Map.Entry)it.next();
		        tempRecords.add(((RecordEntry)pair.getValue()).getRecord());
		    }
		} else if (user instanceof Doctor){
			while (it.hasNext()) {
				pair = (Map.Entry)it.next();
				RecordEntry re = (RecordEntry)pair.getValue();
		        if(re.getDivisionID() == ((Doctor)user).getDivisionID()){
		        	tempRecords.add(re.getRecord());
		        }
		    }
		} else if(user instanceof Nurse){
			while (it.hasNext()) {
				pair = (Map.Entry)it.next();
				RecordEntry re = (RecordEntry)pair.getValue();
		        ArrayList<Integer> nurseIDs = re.getNurseIDs();
		        for(int nurseID : nurseIDs){
			        if(nurseID == new Integer(((Nurse)user).getID())){
			        	tempRecords.add(re.getRecord());
			        	break;
			        }
		        }
		    }
		} else {
			throw new AuthorizationException("You are not allowed to do that");
		}
		au.println(user, "Printed all availabele ACLs");
		return tempRecords;
	}
	
	public void removeRecord(User user, String socialSecurityNumber) throws AuthorizationException, NullPointerException{
		try {
			RecordEntry re = records.get(socialSecurityNumber);
			if(re == null){
				throw new NullPointerException();
			} else if(user instanceof Government){
				records.remove(socialSecurityNumber);
				Patient p = new Patient(socialSecurityNumber, "");
				users.remove(p);
				au.println(user, "Removed record " + socialSecurityNumber);
				try {
					saveDataBase();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				throw new AuthorizationException("You are not allowed to delete records");
			}
		} catch(AuthorizationException | NullPointerException e){
			throw e;
		}
	}

	public void editRecord(User user, String firstName, String surName, String comment, int divisionID, String socialSecurityNumber) throws AuthorizationException, NullPointerException  {
		try {
			RecordEntry oldRecordEntry = getRecordEntry(socialSecurityNumber, user);
			Record oldRecord = oldRecordEntry.getRecord();
			
			if(comment == null){
				comment = oldRecord.getComment();
			}
			if(divisionID < 0){
				divisionID = oldRecordEntry.getDivisionID();
			}
			if(firstName != null || surName != null){
				if(firstName == null){
					firstName = oldRecord.getFirstName();
				}
				if(surName == null){
					surName = oldRecord.getSurName();
				}
				Patient p = new Patient(socialSecurityNumber, firstName+surName);
				users.remove(p);
				users.add(p);
			}
			Record editedRecord = new Record(socialSecurityNumber, firstName, surName, divisionID, comment);
			RecordEntry editedRecordEntry = new RecordEntry(editedRecord, divisionID, oldRecordEntry.getDivisionID(), oldRecordEntry.getNurseIDs(), socialSecurityNumber);

			if (user instanceof Doctor) {
				if (new Integer(((Doctor) user).getDivisionID()) == oldRecordEntry.getDivisionID()) {
					records.remove(socialSecurityNumber);
					records.put(socialSecurityNumber, editedRecordEntry);
					au.println(user, "Edited record " + socialSecurityNumber);
				} else {
					throw new AuthorizationException("The patient does not belong to your division, Dr " + user.getName());
				}
			} else if (user instanceof Nurse) {
				int nurseID = new Integer(user.getID());
				boolean success = false;
				for (int someNurseID : oldRecordEntry.getNurseIDs()) {
					if (nurseID == someNurseID) {
						records.remove(socialSecurityNumber);
						records.put(socialSecurityNumber, editedRecordEntry);
						au.println(user, "Edited record " + socialSecurityNumber);
						success = true;
						break;
					}
				}
				if(!success){
					throw new AuthorizationException("You do not have access to that patient, nurse" + user.getName());
				}
			} else {
				throw new AuthorizationException("You are not allowed to edit records");
			}
			try {
				saveDataBase();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (NullPointerException | AuthorizationException e){
			throw e;
		}
	}

	private class RecordEntry {
		private Record record;
		private int doctorID, divisionID;
		private ArrayList<Integer> nurseIDs;
		private String socialSecurityNumber;

		private RecordEntry(Record record, int divisionID, int doctorID,
				ArrayList<Integer> nurseIDs, String socialSecurityNumber) {
			this.record = record;
			this.doctorID = doctorID;
			this.nurseIDs = nurseIDs;
			this.socialSecurityNumber = socialSecurityNumber;
			this.divisionID = divisionID;
		}

		private int getDoctorID() {
			return doctorID;
		}

		private ArrayList<Integer> getNurseIDs() {
			return nurseIDs;
		}

		private String getSocialSecurityNumber() {
			return socialSecurityNumber;
		}

		private Record getRecord() {
			return record;
		}

		private int getDivisionID() {
			return divisionID;
		}

	}
	
	public class AuthorizationException extends Exception {
		  public AuthorizationException() { super(); }
		  public AuthorizationException(String message) { super(message); }
		  public AuthorizationException(String message, Throwable cause) { super(message, cause); }
		  public AuthorizationException(Throwable cause) { super(cause); }
		}

}
