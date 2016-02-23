package Server;

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;

import Server.Database.AuthorizationException;
import Client.Doctor;
import Client.Employee;
import Client.Government;
import Client.Nurse;
import Client.Patient;
import Client.User;

public class server implements Runnable {
	private ServerSocket serverSocket = null;
	private static int numConnectedClients = 0;
	private Database db;
	private BufferedReader in;
	private HashMap<String, User> clients;
	private PrintWriter out;
	private Auditer au;

	public server(ServerSocket ss) throws IOException {
		serverSocket = ss;
		newListener();
		clients = new HashMap<>();
		au = new Auditer();;
		db = new Database(au);
	}
	
	private void addUser(String subject){
		String[] certifacateInfos = subject.split(",");
		String userID = certifacateInfos[1].substring(4,certifacateInfos[1].length());
		String name = certifacateInfos[0].substring(3,certifacateInfos[0].length());
		int division = new Integer(certifacateInfos[2].substring(3,certifacateInfos[2].length()));
		
		if(userID.length() < 5){ 		//Not a Patient
			int userType = new Integer(userID);
			if(userType < 1000){
				clients.put(userID, (new Government(userID, name)));
			} else {
				if(userType < 2000){
					clients.put(userID, (new Doctor(userID, name, division)));
				} else {
					clients.put(userID, (new Nurse(userID, name, division)));
				}
			}
		} else{
			clients.put(userID, (new Patient(userID, name)));
		}
		
	}

	public void run() {
		try {
			SSLSocket socket = (SSLSocket) serverSocket.accept();
			newListener();
			SSLSession session = socket.getSession();
			X509Certificate cert = (X509Certificate) session
					.getPeerCertificateChain()[0];
			String subject = cert.getSubjectDN().getName();

			String issuer = cert.getIssuerDN().getName();
			String serial = cert.getSerialNumber().toString();

			numConnectedClients++;
			au.println("New client connected");
			au.println("client name (cert subject DN field): "
					+ subject + "\n" + "issuer name(cert issuer DN field): "
					+ issuer);
			
			addUser(subject);
			

			au.println("Serial number: " + serial);

			au.println(numConnectedClients
					+ " concurrent connection(s)\n");

			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			
			String clientMsg = null;
			String clientID = null;
			while ((clientMsg = in.readLine()) != null) {
				String[] certifacateInfos = subject.split(",");
				clientID = certifacateInfos[1].substring(4,certifacateInfos[1].length());
				takeInput(clientMsg, clientID);
				out.println("listen");
				out.flush();
			}

			in.close();
			out.close();
			socket.close();
			numConnectedClients--;
			au.println("client " + clientID + " disconnected");
			au.println(numConnectedClients
					+ " concurrent connection(s)\n");
		} catch (IOException e) {
			au.println("Client died: " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}
	

	synchronized private void takeInput(String clientMsg, String clientID) throws IOException {
		String command = clientMsg;
		User user = clients.get(clientID);
		au.println(user, clientMsg);
		if (command.contains("-h")) {
			out.println("Example Get: -g SocialSecurityNumber");
			out.println("Example Put: -p Firstname Surname DivisionID NurseIDs SocialSecurityNumber");
			out.println("Example Edit: -e SocialSecurityNumber");
		} 
		if (command.contains("-p")) {
			String[] infos = command.split(" ");
			String firstName = infos[1];
			String surName = infos[2];
			int divisionID = new Integer(infos[3]);
			String socialSecurityNumber = infos[infos.length - 1];
			ArrayList<Integer> nurseIDs = new ArrayList<Integer>();
			for (int i = 5; i < infos.length - 1; i++) {
				nurseIDs.add(new Integer(infos[i]));
			}
			out.println("Add comment to record: ");
			out.println("listen");
			String comment = in.readLine();
			au.println(user,comment);
			out.flush();

			Record record = new Record(socialSecurityNumber, firstName,
					surName, divisionID, comment);
			try {
				db.putRecord(user, record, divisionID, new Integer(
						user.getID()), nurseIDs, socialSecurityNumber);
				out.println("Record for " + socialSecurityNumber + " added");
				au.println("Record for " + socialSecurityNumber + " added");
			} catch (NumberFormatException | NullPointerException
					| AuthorizationException e) {
				out.println(e.getMessage());
				au.errorprintln(user, e.getMessage());
			}
		}
		if (command.contains("-e") || command.contains("-g")) {
			String[] infos = command.split(" ");
			String socialSecurityNumber = infos[1];
			if (socialSecurityNumber.length() == 12) {
				Record record = null;
				try {
					record = db.getRecord(socialSecurityNumber, user);
				} catch (NullPointerException | AuthorizationException e) {
					out.println(e.getMessage());
					au.errorprintln(user, e.getMessage());
				}
				if (record != null) {
					out.println(record.toString());
					if(command.contains("-e")){
						
						String firstName = null;
						String surName = null;
						int divisionID = -1;
						String comment = null;
						
						out.println("What do you want to change? \n FirstName -fn, Surname -sn, Comment -co, Divison -di \nSocialSecurityNumber -scc, Quit -q");
						out.println("listen");
						String edit = in.readLine();
						out.flush();
						au.println(user,edit);
						if(!edit.contains("-q")) {
							String[] edits = edit.split(" ");
							for (int i = 0; i < edits.length; i += 2) {
								if (edits[i].contains("-co")) {
									int index = i + 1;
									comment = "";
									while (index < edits.length
											&& !edits[index].contains("-")) {
										comment += " " + edits[index];
										index++;
									}
									i = index - 2;
								} else {
									if (edits.length % 2 == 0){
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
											out.println("Command " + choice
													+ " not recognized");
										}
									}
								}
							} 
							record = new Record(socialSecurityNumber, firstName,
									surName, divisionID, comment);
							try {
								db.editRecord(user, record,	socialSecurityNumber);
								out.println("Record " + socialSecurityNumber + " edited");
								au.println("Record " + socialSecurityNumber + " edited");
							} catch (AuthorizationException | NullPointerException e){
								out.println(e.getMessage());
								au.errorprintln(user, e.getMessage());
							}
						}
					}
				}
			} else {
				out.println("Wrong format, should be yyyyMMdd-xxxx. Try again");
			} 
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
				KeyManagerFactory kmf = KeyManagerFactory
						.getInstance("SunX509");
				TrustManagerFactory tmf = TrustManagerFactory
						.getInstance("SunX509");
				KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
				char[] password = "password".toCharArray();

				/** Set and trim path to folders */
				URL tempLocation = server.class.getProtectionDomain()
						.getCodeSource().getLocation();
				String location = "" + tempLocation;
				location = location.substring(5, location.length() - 5);

				ks.load(new FileInputStream(location
						+ "/certificates/Server/serverkeystore"), password); // keystore
																				// password
																				// (storepass)
				ts.load(new FileInputStream(location
						+ "/certificates/Server/servertruststore"), password); // truststore
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
