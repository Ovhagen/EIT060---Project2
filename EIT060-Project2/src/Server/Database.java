package Server;

import java.util.ArrayList;
import java.util.HashMap;

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

	public Database() {
		records = new HashMap<Integer, RecordEntry>();
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

	public void putRecord(User user, Record record, int divisionID, int doctorID, int[] nurseIDs,
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
		private int[] nurseIDs;

		public RecordEntry(Record record, int divisionID, int doctorID, int[] nurseIDs, int socialSecurityNumber) {
			this.record = record;
			this.doctorID = doctorID;
			this.nurseIDs = nurseIDs;
			this.socialSecurityNumber = socialSecurityNumber;
			this.divisionID = divisionID;
		}

		public int getDoctorID() {
			return doctorID;
		}

		public int[] getNurseID() {
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
