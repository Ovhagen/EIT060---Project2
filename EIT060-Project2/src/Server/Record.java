package Server;

public class Record {
	

	private String ID, firstName, surName, comment;
	private int divisionID;


	public Record(String ID, String firstName, String surName, int divisionID, String comment){
		this.ID = ID;
		this.firstName = firstName;
		this.surName = surName;
		this.comment = comment;
		this.divisionID = divisionID;
	}
	
	public String getID() {
		return ID;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getSurName() {
		return surName;
	}

	public String getComment() {
		return comment;
	}
	
	public int getDivisionID(){
		return divisionID;
	}
	
	
	public String toString(){
		return ("ID: " + ID + "\n F�rnamn: " + firstName + "\n Efternamn: " + surName + "\nDivision: " + divisionID +  "\n Comment: " + comment);
	}
	
	

}
