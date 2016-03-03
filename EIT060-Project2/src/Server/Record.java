package Server;

import java.util.ArrayList;

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
		return ("ID: " + ID + "\n First Name: " + firstName + "\n Surname: " + surName + "\nDivision: " + divisionID +  "\n Comment: " + comment);
	}
	
	
	public String toFancyString(){
		StringBuilder s = new StringBuilder();
		StringBuilder stars = new StringBuilder();
		StringBuilder divider = new StringBuilder();
		stars.append(" ");
		divider.append(" ");
		for (int j = 0; j < 39; j++) {
			stars.append("*");
			divider.append("-");
		}
		s.append(stars);
		s.append(String.format("%s %s %20s %16s","\n", "*","Record", "*"));
		s.append("\n *" + divider.substring(3) + "*");
		s.append(String.format("%s %s %-12s %-22s %s", "\n", "*", "First name:",firstName, "*"));
		s.append(String.format("%s %s %-12s %-22s %s", "\n", "*", "Surname:", surName, "*"));
		s.append(String.format("%s %s %-12s %-22s %s", "\n", "*", "Division:", (""+divisionID), "*"));
		String tempComment = comment;
		ArrayList<String> dividedComment = new ArrayList<>();
		int i = 0;
		while(tempComment.length() > 22){
			if((tempComment.charAt(22) == ' ' || tempComment.charAt(20) == ' ')){
				dividedComment.add(tempComment.substring(0,21));
				tempComment = tempComment.substring(21,tempComment.length());
			} else if (tempComment.charAt(21) == ' ') {
				dividedComment.add(tempComment.substring(0,22));
				tempComment = tempComment.substring(22,tempComment.length());
			} else {
				dividedComment.add(tempComment.substring(0,21) + "-");
				tempComment = tempComment.substring(21,tempComment.length());
			}
			
		}
		if(tempComment.length() > 0){
			dividedComment.add(tempComment);
		}
			s.append(String.format("%s %s %-12s %-22s %s", "\n", "*", "Comment: ", dividedComment.get(0).trim(), "*"));
		for(i = 1; i < dividedComment.size(); i++){
			s.append(String.format("%s %s %-12s %-22s %s", "\n", "*", "", dividedComment.get(i).trim(), "*"));
		}
		s.append("\n" + stars);
		return s.toString();
		
		
		//return ("ID: " + ID + "\n First Name: " + firstName + "\n Surname: " + surName + "\nDivision: " + divisionID +  "\n Comment: " + comment);
	}
	
	

}
