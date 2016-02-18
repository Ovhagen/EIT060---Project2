package Server;

public class Record {
	

	private String ID, firstName, surName, comment;


	public Record(String ID, String firstName, String surName, String comment){
		this.ID = ID;
		this.firstName = firstName;
		this.surName = surName;
		this.comment = comment;
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
	
	
	public String toString(){
		return ("ID: " + ID + "\n Förnamn: " + firstName + "\n Efternamn: " + surName + "\n Comment: " + comment);
	}
	
	

}
