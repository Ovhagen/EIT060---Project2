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
	private HashMap<Integer, RecordEntry> records;
	private File file;

	public Database(File file) {
		records = new HashMap<Integer, RecordEntry>();
		this.file = file;
		//loadDataBase();
	}
	
	/**-Users
		UserType;UserID;Division;
		-Records
		UserID;Division;firstName;lastName;comment;userIDs...
	 */

	private void loadDataBase(){
		HashMap<Integer, User> users = new HashMap<>();
		Scanner scan = null;
		try {
			scan = new Scanner(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = scan.nextLine();
		while(!line.equals("START")){
			line = scan.nextLine();
		}

		if(line != null){
			while(line.equals("-Records")){
				String[] infos = line.split(";");
				String userType = infos[0];
				int userID = new Integer(infos[1]);
				Record record;
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
			while(line != null){
				String[] infos = line.split(";");
				int userID = new Integer(infos[0]);
				int divisionID = new Integer(infos[1]);
				Record record = new Record(userID, infos[1], infos[2], infos[3]);
				int i = 4;
				int doctorID = -1;
				ArrayList<Integer> nurseIDs = new ArrayList<>();
				int socialSecurityNumber = -1;
				while(i < infos.length){
					user = users.get(infos[i]);
					if(user.getClass() == Doctor.class){
						doctorID = user.getID();
					} else if (user.getClass() == Nurse.class){
						nurseIDs.add(user.getID());
					} else if (user.getClass() == Patient.class){
						socialSecurityNumber = user.getID();
					}
				}
				RecordEntry recordEntry = new RecordEntry(record,divisionID,doctorID,nurseIDs,socialSecurityNumber);
				records.put(socialSecurityNumber, recordEntry);
				line = scan.nextLine();
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
			int socialSecurityNumber) {
		RecordEntry recordEntry = new RecordEntry(record, divisionID, doctorID, nurseIDs, socialSecurityNumber);
		if (user instanceof Government || user instanceof Doctor) {
			records.put(socialSecurityNumber, recordEntry);
		} else {
			System.out.println("Du har ej tillåtelse att skapa en ny journal");
		}
	}

	public void editRecord(User user, int socialSecurityNumber, RecordEntry record) {
		RecordEntry oldRecord = records.get(socialSecurityNumber);
		if (oldRecord != null) {
			if (user instanceof Government) {
				records.remove(socialSecurityNumber);
				records.put(socialSecurityNumber, record);
			} else if (user instanceof Doctor) {
				if (((Doctor) user).getID() == oldRecord.getDoctorID()) {
					records.remove(socialSecurityNumber);
					records.put(socialSecurityNumber, record);
				} else {
					System.out.println("Patienten tillhör ej din division");
				}
			} else if (user instanceof Nurse) {
				int nurseID = ((Nurse) user).getID();
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
		private int doctorID, socialSecurityNumber, divisionID;
		private ArrayList<Integer> nurseIDs;

		public RecordEntry(Record record, int divisionID, int doctorID, ArrayList<Integer> nurseIDs, int socialSecurityNumber) {
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

		public int getSocialSecurityNumber() {
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
