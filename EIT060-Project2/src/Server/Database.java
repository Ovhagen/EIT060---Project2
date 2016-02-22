package Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Files;

import javafx.scene.shape.Path;

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

	public Database() {
		records = new HashMap<String, RecordEntry>();
		users = new ArrayList<>();
		
		URL tempLocation = server.class.getProtectionDomain().getCodeSource().getLocation();
		location = "" + tempLocation;
		location = location.substring(5, location.length());		
		
		file = getLatestDatabase();
		if(file == null){
			file = new File(location + "DataBase/DataBase-infoFile.txt");
		} else {
			System.out.println("Loading database " + file.getName());
			loadDataBase();
		}
	}



	private void loadDataBase() {
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
					if (employeeNumber >= 1000) {
						doctorID = employeeNumber;
					} else if (employeeNumber >= 2000) {
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

	private void removeOldestFile(){
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
				System.out.println("DataBase" + fileToRemove + " was deleted");
			}
		}
		
	}
	
	private File getLatestDatabase(){
		File folder = new File(location + "/Database");
		File[] listOfFiles = folder.listFiles();
		long newestFile = 0;
		ArrayList<String[]> dates = new ArrayList<>();
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
	
	public void saveDataBase() throws IOException {
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
		System.out.println("Database" + todaysDate + " was created.");
		removeOldestFile();
	}

	public Record getRecord(String socialSecurityNumber, User user) {
		RecordEntry re = getRecordEntry(socialSecurityNumber, user);
		if(re != null){
			return re.getRecord();
		}
		return null;
	}
	
	public RecordEntry getRecordEntry(String socialSecurityNumber, User user){
		RecordEntry re = records.get(socialSecurityNumber);
		if(re == null){
			System.out.println("Finns ingen journal med det personnumret");
			return null;
		}
		if (user instanceof Government) {
			return re;
		} else if (user instanceof Employee) {
			Employee employee = (Employee) user;
			if (employee.getDivisionID() == re.getDivisionID()) {
				return re;
			}
		} else if (re.getSocialSecurityNumber() == user.getID()) {
			return re;
		}
		System.out.println("Du fÂr ej h‰mta ut den journalen");
		return null;
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
	
	public int putRecord(User user, Record record, int divisionID,
			int doctorID, ArrayList<Integer> nurseIDs,
			String socialSecurityNumber) {
		

		

		if(!users.contains(user)){
			addUser(user);
		}  
		Patient patient = new Patient(socialSecurityNumber, record.getFirstName() + " " + record.getSurName());
		if(!users.contains(patient)){
			addUser(patient);
		}
		
		RecordEntry recordEntry = new RecordEntry(record, divisionID, doctorID,
				nurseIDs, socialSecurityNumber);
		if (user instanceof Government || user instanceof Doctor) {
			records.put(socialSecurityNumber, recordEntry);
		} else {
			System.out.println("Du har ej till√•telse att skapa en ny journal");
			return -1;
		}
		try {
			saveDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public void editRecord(User user, String socialSecurityNumber,
			RecordEntry record) {
		RecordEntry oldRecord = records.get(socialSecurityNumber);
		if (oldRecord != null) {
			if (user instanceof Government) {
				records.remove(socialSecurityNumber);
				records.put(socialSecurityNumber, record);
			} else if (user instanceof Doctor) {
				if (new Integer(user.getID()) == oldRecord.getDoctorID()) {
					records.remove(socialSecurityNumber);
					records.put(socialSecurityNumber, record);
				} else {
					System.out.println("Patienten tillh√∂r ej din division");
				}
			} else if (user instanceof Nurse) {
				int nurseID = new Integer(user.getID());
				for (int someNurseID : oldRecord.getNurseIDs()) {
					if (nurseID == someNurseID) {
						records.remove(socialSecurityNumber);
						records.put(socialSecurityNumber, record);
					}
				}
			}
		}
		try {
			saveDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

		protected int getDoctorID() {
			return doctorID;
		}

		protected ArrayList<Integer> getNurseIDs() {
			return nurseIDs;
		}

		protected String getSocialSecurityNumber() {
			return socialSecurityNumber;
		}

		protected Record getRecord() {
			return record;
		}

		protected int getDivisionID() {
			return divisionID;
		}

	}

}
