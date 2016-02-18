package Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import Client.Doctor;
import Client.Employee;
import Client.Government;
import Client.Nurse;
import Client.User;
import Client.Patient;

public class Database {

	public final static int GOVERNMENT = 3, DOCTOR = 2, NURSE = 1, USER = 0;
	private User user;
	private HashMap<String, RecordEntry> records;
	private File file;

	public Database(File file) {
		records = new HashMap<String, RecordEntry>();
		this.file = file;
		loadDataBase();
		System.out.println(records.get("5301231531").getRecord().toString());
	}
	
	/**-Users
		UserType;UserID;Division;
		-Records
		UserID;Division;firstName;lastName;comment;userIDs...
	 */

	private void loadDataBase(){
		HashMap<String, User> users = new HashMap<>();
		Scanner scan = null;
		try {
			scan = new Scanner(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = scan.nextLine();
		while(!line.equals("-Users")){
			line = scan.nextLine();
		}
		line = scan.nextLine();
		if(line != null){
			while(!line.equals("-Records")){
				String[] infos = line.split(";");
				String userType = infos[0];
				String userID = infos[1];
				switch(userType){
				case "G":
					Government government = new Government(userID);
					users.put(userID, government);
				case "D":
					Doctor doctor = new Doctor(userID, new Integer(infos[2]));
					users.put(userID, doctor);
					break;
				case "N":
					Nurse nurse = new Nurse(userID, new Integer(infos[2]));
					users.put(userID, nurse);
					break;
				case "P":
					Patient patient = new Patient(userID);
					users.put(userID, patient);
					break;
				}
				line = scan.nextLine();
			}
			line = scan.nextLine();
			while(line != null){
				String[] infos = line.split(";");
				String socialSecurityNumber = infos[0];
				int divisionID = new Integer(infos[1]);
				Record record = new Record(socialSecurityNumber, infos[2], infos[3], infos[4]);
				int i = 5;
				int doctorID = -1;
				ArrayList<Integer> nurseIDs = new ArrayList<>();
				while(i < infos.length){
					user = users.get(infos[i]);
					if(user.getClass() == Doctor.class){
						doctorID = new Integer(user.getID());
					} else if (user.getClass() == Nurse.class){
						nurseIDs.add(new Integer(user.getID()));
					}
					i++;
				}
				RecordEntry recordEntry = new RecordEntry(record,divisionID,doctorID,nurseIDs,socialSecurityNumber);
				records.put(socialSecurityNumber, recordEntry);
				if(scan.hasNextLine()){
					line = scan.nextLine();
				} else {
					line = null;
				}
			}
		}
		
	}
	
	private void saveDataBase(){
		
	}

	public Record getRecord(int socialSecurityNumber, User user) {
		RecordEntry re = records.get(socialSecurityNumber);
		if (user instanceof Government) {
			return re.getRecord();
		} else if (user instanceof Employee) {
			Employee employee = (Employee) user;
			if (employee.getDivisionID() == re.getDivisionID()) {
				return re.getRecord();
			}
		} else {
			this.user = (Patient) user;
			if (re.getSocialSecurityNumber() == user.getID()) {
				return re.getRecord();
			}
		}
		return null;
	}

	public void putRecord(User user, Record record, int divisionID, int doctorID, ArrayList<Integer> nurseIDs,
			String socialSecurityNumber) {
		RecordEntry recordEntry = new RecordEntry(record, divisionID, doctorID, nurseIDs, socialSecurityNumber);
		if (user instanceof Government || user instanceof Doctor) {
			records.put(socialSecurityNumber, recordEntry);
		} else {
			System.out.println("Du har ej tillåtelse att skapa en ny journal");
		}
	}

	public void editRecord(User user, String socialSecurityNumber, RecordEntry record) {
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
					System.out.println("Patienten tillhör ej din division");
				}
			} else if (user instanceof Nurse) {
				int nurseID = new Integer(user.getID());
				for (int someNurseID : oldRecord.getNurseID()) {
					if (nurseID == someNurseID) {
						records.remove(socialSecurityNumber);
						records.put(socialSecurityNumber, record);
					}
				}
			}
		}
	}

	private class RecordEntry {
		private Record record;
		private int doctorID, divisionID;
		private ArrayList<Integer> nurseIDs;
		private String socialSecurityNumber;

		public RecordEntry(Record record, int divisionID, int doctorID, ArrayList<Integer> nurseIDs, String socialSecurityNumber) {
			this.record = record;
			this.doctorID = doctorID;
			this.nurseIDs = nurseIDs;
			this.socialSecurityNumber = socialSecurityNumber;
			this.divisionID = divisionID;
		}

		public int getDoctorID() {
			return doctorID;
		}

		public ArrayList<Integer> getNurseID() {
			return nurseIDs;
		}

		public String getSocialSecurityNumber() {
			return socialSecurityNumber;
		}

		public Record getRecord() {
			return record;
		}

		public int getDivisionID() {
			return divisionID;
		}


	}

}
