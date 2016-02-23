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

	public Database(Auditer au) {
		this.au = au;
		records = new HashMap<String, RecordEntry>();
		users = new ArrayList<>();
		
		URL tempLocation = server.class.getProtectionDomain().getCodeSource().getLocation();
		location = "" + tempLocation;
		location = location.substring(5, location.length());		
		
		file = getLatestDatabase();
		if(file == null){
			file = new File(location + "DataBase/DataBase-infoFile.txt");
		} else {
			au.println("Loading database " + file.getName());
			loadDataBase();
		}
	}


	synchronized private void loadDataBase() {
		Scanner scan = null;
		try {
			scan = new Scanner(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = scan.nextLine();
		while (!line.equals("-Users")) {
			line = scan.nextLine();
		}
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
	
	synchronized public void saveDataBase() throws IOException {
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
		} catch (NullPointerException | AuthorizationException e) {
			if(e instanceof AuthorizationException){
				throw (AuthorizationException) e;
			} else if(e instanceof NullPointerException){
				throw new NullPointerException("These is no record for social security number " + socialSecurityNumber);
			} else {
				e.printStackTrace();
			}
			
		}
		return re.getRecord();
	}
	
	public RecordEntry getRecordEntry(String socialSecurityNumber, User user) throws NullPointerException, AuthorizationException {
		try {
			RecordEntry re = null;
			if (user instanceof Government) {
				re = records.get(socialSecurityNumber);
			} else if (user instanceof Employee) {
				Employee employee = (Employee) user;
				re = records.get(socialSecurityNumber);
				if (employee.getDivisionID() != re.getDivisionID()) {
					throw new AuthorizationException("You do not have permission to see that record");
				}
			} else {
				re = records.get(socialSecurityNumber);
				if (!re.getSocialSecurityNumber().equals(user.getID())){
					throw new AuthorizationException("You do not have permission to see that record");
				}
			}
			return re;
		} catch(NullPointerException e) {
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
	
	public void putRecord(User user, Record record, int divisionID,
			int doctorID, ArrayList<Integer> nurseIDs,
			String socialSecurityNumber) throws NullPointerException, AuthorizationException{
		if(!users.contains(user)){
			addUser(user);
		}  
		Patient patient = new Patient(socialSecurityNumber, record.getFirstName() + " " + record.getSurName());
		if(!users.contains(patient)){
			addUser(patient);
		}
		
		RecordEntry recordEntry = new RecordEntry(record, divisionID, doctorID,
				nurseIDs, socialSecurityNumber);
		if(user instanceof Doctor || user instanceof Government){
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
		return tempRecords;
	}
	
	public void removeRecord(User user, String socialSecurityNumber) throws AuthorizationException, NullPointerException{
		RecordEntry re = null;
		try {
			re = records.get(socialSecurityNumber);
			if(re == null){
				throw new NullPointerException();
			} else if(user instanceof Government || user instanceof Doctor){
				records.remove(socialSecurityNumber);
				au.println("Removed record " + socialSecurityNumber);
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

	public void editRecord(User user, Record newRecord, String socialSecurityNumber) throws AuthorizationException, NullPointerException  {
		try {
			RecordEntry oldRecordEntry = getRecordEntry(socialSecurityNumber, user);
			Record oldRecord = oldRecordEntry.getRecord();
			
			String comment = oldRecord.getComment();
			int divisionID = oldRecord.getDivisionID();
			
			ArrayList<Integer> nurseIDs = oldRecordEntry.getNurseIDs();
			int doctorID = oldRecordEntry.getDoctorID();
			String firstName = oldRecord.getFirstName();
			String surName = oldRecord.getSurName();
			
			if(newRecord.getComment() != null){
				comment = newRecord.getComment();
			}
			if(newRecord.getDivisionID() > 0){
				divisionID = newRecord.getDivisionID();
			}
			if(newRecord.getFirstName() != null){
				firstName = newRecord.getFirstName();
			}
			if(newRecord.getSurName() != null){
				surName = newRecord.getSurName();
			}
			Record editedRecord = new Record(socialSecurityNumber, firstName, surName, divisionID, comment);
			RecordEntry editedRecordEntry = new RecordEntry(editedRecord, divisionID, doctorID, nurseIDs, socialSecurityNumber);
			
			boolean success = false;
			if(user instanceof Patient){
				throw new AuthorizationException("Patients are not allowed to edit records");
			} else {
				if (user instanceof Government) {
					success = true;
					records.remove(socialSecurityNumber);
					records.put(socialSecurityNumber, editedRecordEntry);
				} else if (user instanceof Doctor) {
					if (new Integer(((Doctor) user).getDivisionID()) == oldRecordEntry.getDivisionID()) {
						success = true;
						records.remove(socialSecurityNumber);
						records.put(socialSecurityNumber, editedRecordEntry);
					} else {
						throw new AuthorizationException("The patient does not belong to your division, Dr " + user.getName());
					}
				} else if (user instanceof Nurse) {
					int nurseID = new Integer(user.getID());
					for (int someNurseID : oldRecordEntry.getNurseIDs()) {
						if (nurseID == someNurseID) {
							success = true;
							records.remove(socialSecurityNumber);
							records.put(socialSecurityNumber, editedRecordEntry);
						} else {
							throw new AuthorizationException("This is not your patient, Nurse " + user.getName());
						}
					}
				}
				if(success){
					try {
						saveDataBase();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} catch (NullPointerException e){
			throw (NullPointerException) e;
		} catch (AuthorizationException e){
			throw (AuthorizationException) e;
		}
	}

	public class RecordEntry {
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
