package Server;

public class Record {
	
	private int ID;
	private String firstName, surName, comment;


	public Record(int ID, String firstName, String surName, String comment){
		this.ID = ID;
		this.firstName = firstName;
		this.surName = surName;
		this.comment = comment;
	}
	
	public int getID() {
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
	
	

}
