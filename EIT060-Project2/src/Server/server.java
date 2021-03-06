package Server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;

import Client.Doctor;
import Client.Employee;
import Client.Government;
import Client.Nurse;
import Client.Patient;
import Client.User;
import Client.Client;
import Exeptions.AuthorizationException;
import Exeptions.WrongFormatException;

public class server implements Runnable {
	private ServerSocket serverSocket = null;
	private static int numConnectedClients = 0;
	private Database db;
	private BufferedReader in;
	private HashMap<String, User> clients;
	private ArrayList<String> connectedClients;
	private PrintWriter out;
	private Auditer au;

	public server(ServerSocket ss) throws IOException {
		serverSocket = ss;
		newListener();
		clients = new HashMap<>();
		au = new Auditer();
		db = new Database(au);
		try {
			db.init();
		} catch (IOException e) {
			au.println(e.getMessage());
		}
		connectedClients = new ArrayList<>();
	}


	public void run(){
		try {
			SSLSocket socket = (SSLSocket) serverSocket.accept();
			newListener();
			SSLSession session = socket.getSession();
			X509Certificate cert = (X509Certificate) session.getPeerCertificateChain()[0];
			String subject = cert.getSubjectDN().getName();

			String issuer = cert.getIssuerDN().getName();
			String serial = cert.getSerialNumber().toString();

			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF8"));

			if (connectedClients.contains(serial)) {
				out.println("notAllowed");
				throw new AuthorizationException("Client with serial " + serial + " already connected");
			} else {
				out.println("allowed");
			}
			connectedClients.add(serial);

			numConnectedClients++;
			au.println("New client connected");
			au.println("client name (cert subject DN field): " + subject + "\n" + "issuer name(cert issuer DN field): "
					+ issuer);

			addUser(subject);

			au.println("Serial number: " + serial);
			au.println(numConnectedClients + " concurrent connection(s)\n");

			out.flush();
			String[] certifacateInfos = subject.split(",");
			String clientID = certifacateInfos[1].substring(4, certifacateInfos[1].length());
			String clientMsg = null;
			
			if(clientID.length() > 4){
				handleInput("-g " + clientID, clientID);
			} else {
				while ((clientMsg = in.readLine()) != null) {
					System.out.println(clientMsg);
					handleInput(clientMsg, clientID);
					out.println("listen");
					out.flush();
				}
			}

			connectedClients.remove(serial);
			in.close();
			out.close();
			socket.close();
			numConnectedClients--;
			au.println("client disconnected");
			au.println(numConnectedClients + " concurrent connection(s)\n");
		} catch (AuthorizationException e){
			au.println("ERROR:" + e.getMessage());
			out.println("User already logged in");
		} catch (Exception e) {
			Object o = e.getClass();
			if (o instanceof Client) {
				Client c = ((Client) o);
				au.println("Client " + c.getClientID() + " died: " + e.getMessage());
				connectedClients.remove(c.getSerial());
			} else {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Handles the input from client and executes code accordingly
	 * @param clientMsg - The input message sent from Client
	 * @param clientID - The ID for the client
	 * @throws IOException
	 */
	private void handleInput(String clientMsg, String clientID) throws IOException {
		User user = clients.get(clientID);
		au.println(user, clientMsg);
		String[] infos = clientMsg.split(" ");
		String command = infos[0];
		try {
			if (infos.length <= 1) {
				switch(command){
				case "-h":
					help(user);
					break;
				case "-pa":
					printAll(user);
					break;
				default: 
					throw new WrongFormatException("Command not recognized or no arguments");
				}
			} else if (infos.length > 1) {
				switch (command) {
				case "-rm":
					remove(user, infos);
					break;
				case "-an":
					addNurse(user, infos);
					break;
				case "-rn":
					removeNurse(user, infos);
					break;
				case "-ed":
					editDoctorID(user, infos);
					break;
				case "-pacl":
					printACL(user, infos);
					break;
				case "-p":
					put(user, infos);
					break;
				case "-e":
					edit(user, infos);
					break;
				case "-g":
					Record r = get(user, infos);
					out.println(r.toFancyString());
					break;
				default:
					throw new WrongFormatException("Command not recognized or no arguments");
				}
			}
		} catch (NullPointerException | AuthorizationException e) {
			out.println("No record for that social security number or unauthorized action!");
			au.println("Error " + e.getMessage());
		} catch (WrongFormatException e){
			out.println(e.getMessage());
			au.println("Error " + e.getMessage());
		}
	} 
	
	/**
	 * Creates and add user to an arraylist for authoriztion checks later
	 * @param subject - Contains info of the user
	 */
	private void addUser(String subject) {
		String[] certifacateInfos = subject.split(",");
		String userID = certifacateInfos[1].substring(4, certifacateInfos[1].length());
		String name = certifacateInfos[0].substring(3, certifacateInfos[0].length());
		int division = new Integer(certifacateInfos[2].substring(3, certifacateInfos[2].length()));

		if (userID.length() < 5) { // Not a Patient
			int userType = new Integer(userID);
			if (userType < 1000) {
				clients.put(userID, (new Government(userID, name)));
			} else {
				if (userType < 2000) {
					clients.put(userID, (new Doctor(userID, name, division)));
				} else {
					clients.put(userID, (new Nurse(userID, name, division)));
				}
			}
		} else {
			clients.put(userID, (new Patient(userID, name)));
		}
	}


	/**
	 * Adds a nurse to a specific record
	 * @param user - User trying to perform operation
	 * @param infos - Contains info for parameters
	 * @throws WrongFormatException - Thrown if the social security number is not length 12
	 * @throws AuthorizationException - Thrown if user is not doctor on the same division as record, or government
	 */
	private void addNurse(User user, String[] infos) throws WrongFormatException, AuthorizationException {
		if (infos.length > 0) {			String socialSecurityNumber = infos[1];
			if (socialSecurityNumber.length() == 12) {
				if (infos.length >= 3) {
					ArrayList<Integer> nurseIDs = new ArrayList<Integer>();
					for (int i = 2; i < infos.length; i++) {
						nurseIDs.add(new Integer(infos[i]));
					}
					try {
						db.addNurseACL(user, socialSecurityNumber, nurseIDs);
						out.println("Successfully added " + nurseIDs.toString());
					} catch (AuthorizationException e) {
						throw e;
					}
				} else {
					throw new WrongFormatException("No argument found");
				}
			} else {
				throw new WrongFormatException("Wrong format of social security number, should be yyyyMMddxxxx. Try again");
			}
		}
	}

	/**
	 * Adds a nurse to a specific record
	 * @param user - User trying to perform operation
	 * @param infos - Contains info for parameters
	 * @throws WrongFormatException - Thrown if the social security number is not length 12
	 * @throws AuthorizationException - Thrown if user is not doctor on the same division as record, or government
	 */
	private void removeNurse(User user, String[] infos) throws WrongFormatException, AuthorizationException {
		String socialSecurityNumber = infos[1];
		if (socialSecurityNumber.length() == 12) {
			if (infos.length >= 3) {
				ArrayList<Integer> nurseIDs = new ArrayList<Integer>();
				for (int i = 2; i < infos.length; i++) {
					nurseIDs.add(new Integer(infos[i]));
				}
				try {
					db.removeNurseACL(user, socialSecurityNumber, nurseIDs);
					out.println("Successfully removed " + nurseIDs.toString());
				} catch (AuthorizationException e) {
					throw e;
				}
			} else {
				throw new WrongFormatException("No argument found");
			}
		} else {
			throw new WrongFormatException("Wrong format of social security number, should be yyyyMMddxxxx. Try again");
		}
	}

	/**
	 * Edits a record's doctorID
	 * @param user - User trying to perform operation
	 * @param infos - Contains info for parameters
	 * @throws WrongFormatException - Thrown if the social security number is not length 12
	 * @throws AuthorizationException - Thrown if user is not government
	 */
	private void editDoctorID(User user, String[] infos) throws WrongFormatException, AuthorizationException{

		String socialSecurityNumber = infos[1];
		if (socialSecurityNumber.length() == 12) {
			if(infos.length == 3){
				int doctorID = new Integer(infos[2]);
				try{
					db.editDoctorACL(user, socialSecurityNumber, doctorID);
				} catch (AuthorizationException e){
					throw e;
				}
			} else {
				throw new WrongFormatException("Wrong amount of arguments.");
			}
		} else {
			throw new WrongFormatException("Wrong format, should be yyyyMMddxxxx. Try again");
		}
	}

	/**
	 * Removes a record from the database
	 * @param user - User trying to perform operation
	 * @param infos - Contains info for parameters
	 * @throws WrongFormatException - Thrown if the social security number is not length 12
	 * @throws AuthorizationException - Thrown if user is not allowed to perform operation
	 */
	private void remove(User user, String[] infos) throws WrongFormatException, AuthorizationException {

		String socialSecurityNumber = infos[1];
		if (socialSecurityNumber.length() == 12) {
			try {
				db.removeRecord(user, socialSecurityNumber);
				out.println("Successfully removed " + socialSecurityNumber);
			} catch (NullPointerException | AuthorizationException e) {
				throw e;
			}
		} else {
			throw new WrongFormatException("Wrong format of social security number, should be yyyyMMddxxxx. Try again");
		}
	}

	/**
	 * Prints an ACL for a specific record
	 * @param user - User trying to perform operation
	 * @param infos - Contains info for parameters
	 * @throws WrongFormatException - Thrown if the social security number is not length 12
	 * @throws AuthorizationException - Thrown if user is not doctor on the same division as record, or government
	 */
	private void printACL(User user, String[] infos) throws WrongFormatException, AuthorizationException {
		String socialSecurityNumber = infos[1];
		if (socialSecurityNumber.length() == 12) {
			try {
				ArrayList<Integer> acls = db.getACL(user, socialSecurityNumber);
				StringBuilder stars = new StringBuilder();
				StringBuilder divider = new StringBuilder();
				for (int j = 0; j < 24; j++) {
					stars.append("*");
					divider.append("-");
				}
				out.println(stars);
				out.println("* ACL for " + socialSecurityNumber + " *");
				out.println("*" + divider.substring(2) + "*");
				for (Integer i : acls) {
					String userType = "Doctor";
					if (i >= 2000) {
						userType = "Nurse";
					}
					out.println(String.format("%s %-12s %7s %s", "*", userType, i, "*"));
				}
				out.println(stars);
			} catch (NullPointerException | AuthorizationException e) {
				throw e;
			}
		} else {
			throw new WrongFormatException("Wrong format of social security number, should be yyyyMMddxxxx. Try again");
		}
	}

	/**
	 * Prints all authorized operations for the logged in client
	 * @param user - User asking for help
	 */
	private void help(User user) {
		out.println("Example Get: -g SocialSecurityNumber");
		if(user instanceof Government){
			out.println("Example Print All: -pa");
			out.println("Example Remove: -rm SocialSecurityNumber");
			out.println("Example: Print ACL: -pacl SocialSecurityNumber");
			out.println("Example: Edit DoctorID: -ed SocialSecurityNumber DoctorID");
		} else if (user instanceof Employee){
			out.println("Example Edit: -e SocialSecurityNumber");	
			out.println("Example Print All: -pa");
			if(user instanceof Doctor){
				out.println("Example Put: -p Firstname Surname NurseIDs SocialSecurityNumber");
				out.println("Example: Print ACL: -pacl SocialSecurityNumber");
				out.println("Example: Add Nurses: -an SocialSecurityNumber NurseID1 NurseID2 NurseID3....");
				out.println("Example: Remove Nurse: -rn SocialSecurityNumber NurseID1 NurseID2 NurseID3... ");
			}
		}		
	}

	/**
	 * Prints all records the user is allowed to read
	 * @param user - User trying to perform operation
	 */
	private void printAll(User user) throws AuthorizationException {
		try {
			StringBuilder stars = new StringBuilder();
			StringBuilder divider = new StringBuilder();
			ArrayList<Record> records = db.getAllAvailabe(user);
			for (int i = 0; i < 127; i++) {
				stars.append("*");
				divider.append("-");
			}
			out.print(stars + "\n");
			out.printf(String.format("%-1s %-15s %-15s %-10s %-25s %-54s %s %s", "*", "First Name", "Surname",
					"Division", "Social Security Number", "Comment", "*", "\n"));
			for (Record r : records) {
				out.print("*" + divider.substring(2) + "*\n");
				String comment = r.getComment();
				String tempComment = comment;
				String[] dividedComment = new String[comment.length()/54+1];
				int i = 0;
				/** Adds a - at the end of words that are split*/
				for(i = 0; i < (comment.length()/54); i++){
					if((Character.isSpaceChar(tempComment.charAt(55)))){
						dividedComment[i] = tempComment.substring(0,55);
						tempComment = tempComment.substring(55,tempComment.length());
					
					} else {
						dividedComment[i] = tempComment.substring(0,53);
						tempComment = tempComment.substring(53,tempComment.length());
					}
				}
				if(tempComment.length() > 0)
					dividedComment[i] = tempComment;
				
				out.printf(String.format("%-1s %-15s %-15s %-10s %-25s %-54s %s %s", "*", r.getFirstName(),
						r.getSurName(), r.getDivisionID(), r.getID(), dividedComment[0].trim(), "*", "\n"));
				for(i = 1; i < dividedComment.length; i++){
					out.printf(String.format("%-1s %-15s %-15s %-10s %-25s %-54s %s %s", "*", "",
							"", "", "", dividedComment[i].trim(), "*", "\n"));
				}
			}
			out.println(stars);
		} catch (AuthorizationException | NullPointerException e) {
			throw e;
		}
	}

	/**
	 * Adds a record to the database with the divisionID of the user
	 * @param user - User trying to perform operation
	 * @param infos - Contains info for parameters to create the record
	 * @throws WrongFormatException - Thrown if the social security number is not length 12
	 * @throws AuthorizationException - Thrown if user is not a doctor
	 * @throws IOException - Thrown if takeInput could not write to audit and/or client
	 */
	private void put(User user, String[] infos) throws AuthorizationException, IOException, WrongFormatException {
		if (infos.length > 4) {
			String firstName = infos[1];
			String surName = infos[2];
			String socialSecurityNumber = infos[infos.length - 1];
			if (socialSecurityNumber.length() == 12) {
				ArrayList<Integer> nurseIDs = new ArrayList<Integer>();
				for (int i = 3; i < infos.length - 1; i++) {
					nurseIDs.add(new Integer(infos[i]));
				}
				String comment = takeInput(user, "Add comment to record: ");
	
				try {
					db.putRecord(user, firstName, surName, comment, nurseIDs, socialSecurityNumber);
					out.println("Record for " + socialSecurityNumber + " added");
				} catch (NumberFormatException | NullPointerException | AuthorizationException e) {
					out.println(e.getMessage());
					au.errorprintln(user, e.getMessage());
				}
			} else {
				throw new WrongFormatException("Wrong format of social security number, should be yyyyMMddxxxx. Try again");
			}
		} else {
			throw new WrongFormatException("Too few arguments");
		}
	}

	/**
	 * Asks question to client, waits for input and prints it to the auditlog
	 * @param user - User trying to print 
	 * @param question - Question to ask client
	 * @return - Return 
	 * @throws IOException
	 */
	private String takeInput(User user, String question) throws IOException {
		out.println(question);
		out.println("listen");
		String input = in.readLine();
		au.println(user, input);
		out.flush();
		return input;
	}

	/**
	 * Edit the parameters of a specific record 
	 * @param user - User trying to perform operation
	 * @param infos - Contains info for parameters
	 * @throws WrongFormatException - Thrown if the social security number is not length 12
	 * @throws AuthorizationException - Thrown if user is not allowed to perform operation
	 * @throws IOException - Thrown if takeInput could not write to audit and/or client
	 */
	private void edit(User user, String[] infos) throws IOException, WrongFormatException, AuthorizationException {
		Record record = get(user, infos);
		String socialSecurityNumber = infos[1];
		if (record != null) {
			out.println(record.toString());
			String firstName = null;
			String surName = null;
			int divisionID = -1;
			String comment = null;

			String edit = takeInput(user,
					"What do you want to change? \n FirstName -fn, Surname -sn, Comment -co, Divison -di \nSocialSecurityNumber -scc, Quit -q");

			while(!edit.contains("-q")) {
				String[] edits = edit.split(" ");
				for (int i = 0; i < edits.length; i += 2) {
					if (edits[i].contains("-co")) {
						int index = i + 1;
						comment = "";
						while (index < edits.length && !edits[index].contains("-")) {
							comment += " " + edits[index];
							index++;
						}
						i = index - 2;
					} else {
						if (edits.length % 2 == 0) {
							String choice = edits[i];
							String change = edits[i + 1];
							if (choice.equals("-fn")) {
								firstName = change;
							} else if (choice.equals("-sn")) {
								surName = change;
							} else if (choice.equals("-di")) {
								divisionID = new Integer(change);
							} else if (choice.equals("-scc")) {
								socialSecurityNumber = change;
							} else {
								out.println("Command " + choice + " not recognized");
							}
						} else {
							throw new WrongFormatException("Wrong format. Try again");
						}
					}
				}
				try {
					db.editRecord(user, firstName, surName, comment, divisionID, socialSecurityNumber);
					out.println("Record " + socialSecurityNumber + " edited");
				} catch (AuthorizationException | NullPointerException e) {
					out.println(e.getMessage());
					au.errorprintln(user, e.getMessage());
				}
				out.println("-q to leave editor.");
				edit = takeInput(user, "Next edit: ");
			}
		}
		out.println("-------");
	}

	/**
	 * Retrieves a specific record from the database
	 * @param user - User trying to perform operation
	 * @param infos - Contains info for parameters
	 * @throws WrongFormatException - Thrown if the social security number is not length 12
	 * @throws AuthorizationException - Thrown if user is not allowed to perform operation
	 */
	private Record get(User user, String[] infos) throws WrongFormatException, AuthorizationException {
		String socialSecurityNumber = infos[1];
		if (socialSecurityNumber.length() == 12) {
			Record record = null;
			try {
				record = db.getRecord(socialSecurityNumber, user);
				return record;
			} catch (NullPointerException | AuthorizationException e) {
				throw e;
			}
		} else {
			throw new WrongFormatException("Wrong format of social security number, should be yyyyMMddxxxx. Try again");
		}
	}

	private void newListener() {
		(new Thread(this)).start();
	} // calls run()

	public static void main(String args[]) {

		System.out.println("\nServer Started\n");
		int port = -1;
		if (args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}
		String type = "TLS";
		try {
			ServerSocketFactory ssf = getServerSocketFactory(type);
			ServerSocket ss = ssf.createServerSocket(port);
			((SSLServerSocket) ss).setNeedClientAuth(true); // enables client
															// authentication
			new server(ss);
		} catch (IOException e) {
			System.out.println("Unable to start Server: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static ServerSocketFactory getServerSocketFactory(String type) {
		if (type.equals("TLS")) {
			SSLServerSocketFactory ssf = null;
			try { // set up key manager to perform server authentication
				SSLContext ctx = SSLContext.getInstance("TLS");
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
				KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
				char[] password = "password".toCharArray();

				/** Set and trim path to folders */
				URL tempLocation = server.class.getProtectionDomain().getCodeSource().getLocation();
				String location = "" + tempLocation;
				location = location.substring(5, location.length() - 5);

				ks.load(new FileInputStream(location + "/certificates/Server/serverkeystore"), password); // keystore
																											// password
																											// (storepass)
				ts.load(new FileInputStream(location + "/certificates/Server/servertruststore"), password); // truststore
																											// password
																											// (storepass)
				kmf.init(ks, password); // certificate password (keypass)
				tmf.init(ts); // possible to use keystore as truststore here
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
				ssf = ctx.getServerSocketFactory();
				return ssf;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return ServerSocketFactory.getDefault();
		}
		return null;
	}

}
