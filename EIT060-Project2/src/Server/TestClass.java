package Server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import Client.Doctor;

public class TestClass {

	public static void main(String[] args) {

		Database db = new Database();
		//Record record = new Record("9403034375", "Christoffer", "MacFie", "Han mår illa");
		Doctor user = new Doctor("1006", "Mantis Tobagan", 2);
		//ArrayList<Integer> nurseIDs = new ArrayList<Integer>();
		//nurseIDs.add(1001);
		//db.putRecord(doctor, record, doctor.getDivisionID(), new Integer(doctor.getID()), nurseIDs, record.getID());

		
		String command;
		Scanner scan = new Scanner(System.in);
		while(true){
			System.out.println("Put -p, Edit -e, Get -g, Help -h");
			command = "";
			command = scan.nextLine();
			if(command.contains("-g")){
				String[] infos = command.split(" ");
				String socialSecurityNumber = infos[1];
				if(socialSecurityNumber.length() == 10){
					socialSecurityNumber = "19" + socialSecurityNumber;
				}
				if(socialSecurityNumber.length() == 12){
					Record record = db.getRecord(socialSecurityNumber, user);
					System.out.println(record.toString());
				} else {
					System.out.println("Wrong format. Try again");
				}
			
			}else if(command.contains("-p")){
				String[] infos = command.split(" ");
				String firstName = infos[1];
				String surName = infos[2];
				int divisionID = new Integer(infos[3]);
				String socialSecurityNumber = infos[infos.length-1];
				ArrayList<Integer> nurseIDs = new ArrayList<Integer>();
				for(int i = 5; i < infos.length-1; i++){
					nurseIDs.add(new Integer(infos[i]));
				}
				System.out.println("Add comment to record: ");
				String comment = scan.nextLine();
				
				Record record = new Record(socialSecurityNumber, firstName, surName, divisionID, comment);
				int result = db.putRecord(user, record, divisionID, new Integer(user.getID()), nurseIDs, socialSecurityNumber);
				if(result == 0){
					System.out.println("Record added");
				}
			} else if(command.contains("-e")){
				String[] infos = command.split(" ");
				String socialSecurityNumber = infos[1];
				if(socialSecurityNumber.length() == 10){
					socialSecurityNumber = "19" + socialSecurityNumber;
				}
				if(socialSecurityNumber.length() == 12){
					Record record = db.getRecord(socialSecurityNumber, user);
					String firstName = record.getFirstName();
					String surName = record.getSurName();
					int divisionID = record.getDivisionID();
					ArrayList<Integer> nurseIDs = db.getRecordEntry(socialSecurityNumber, user).getNurseIDs();
					String comment = record.getComment();
					System.out.println(record.toString());
					System.out.println("What do you want to change? \n FirstName -fn \nSurname -sn \n Comment -co \n Divison -di \n SocialSecurityNumber -scc \n Quit -q");
					String edit = scan.nextLine();
					while(!edit.contains("q")){
						String[] edits = edit.split(" ");
						if(edits.length % 2 == 0){
							for(int i = 0; i < edits.length; i+=2){
								if(edits[i].contains("-co")){
									int index = i+1;
									comment = "";
									while(index < edits.length && !edits[index].contains("-")){
										comment += " " + edits[index];
										index++;
									}
									i = index-2;
								} else {
								String choice = edits[i];
								String change = edits[i+1];
								if(choice.equals("-fn")){
									firstName = change;
								} else if(choice.equals("-sn")){
									surName = change;
								}else if(choice.equals("-di")){
									divisionID = new Integer(change);
								} else if(choice.equals("-scc")){
									socialSecurityNumber = change;
								} else {
									System.out.println("Command " + choice + " not recognized");
								}
								}
							}
						} else {
							System.out.println("Wrong format. Try again:");
							edit = scan.nextLine();
							
						}
						record = new Record(socialSecurityNumber, firstName, surName, divisionID, comment);
						
						int result = db.putRecord(user, record, divisionID, new Integer(user.getID()), nurseIDs, socialSecurityNumber);
						if(result == 0){
							System.out.println("Record added");
						}
						System.out.println(record.toString());
						System.out.println("Nästa ändring: ");
						edit = scan.nextLine();
					}
				}
			} else {
				System.out.println("Wrong format. Try again");
			}
		}
		
	}
}
