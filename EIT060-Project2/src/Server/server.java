package Server;

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.util.ArrayList;

import javax.net.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;

import Client.Doctor;
import Client.User;

public class server implements Runnable {
	private ServerSocket serverSocket = null;
	private static int numConnectedClients = 0;
	private Database db;
	private User user;
	private BufferedReader in;
	PrintWriter out;

	public server(ServerSocket ss) throws IOException {
		serverSocket = ss;
		newListener();
		db = new Database();
		user = new Doctor("1006", "Mantis Tobagan", 2);
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
			System.out.println("client connected");
			System.out.println("client name (cert subject DN field): "
					+ subject + "\n" + "issuer name(cert issuer DN field): "
					+ issuer);

			System.out.println("Serial number: " + serial);

			System.out.println(numConnectedClients
					+ " concurrent connection(s)\n");

			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			String clientMsg = null;
			while ((clientMsg = in.readLine()) != null) {
				System.out.println("received '" + clientMsg + "' from client");

				takeInput(clientMsg);
				out.println("listen");

				out.flush();
				System.out.println("done\n");
			}

			in.close();
			out.close();
			socket.close();
			numConnectedClients--;
			System.out.println("client disconnected");
			System.out.println(numConnectedClients
					+ " concurrent connection(s)\n");
		} catch (IOException e) {
			System.out.println("Client died: " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}

	private void takeInput(String clientMsg) throws IOException {
		String command = clientMsg;
		if (command.contains("-g")) {
			String[] infos = command.split(" ");
			String socialSecurityNumber = infos[1];
			if (socialSecurityNumber.length() == 10) {
				socialSecurityNumber = "19" + socialSecurityNumber;
			}
			if (socialSecurityNumber.length() == 12) {
				Record record = db.getRecord(socialSecurityNumber, user);
				if (record != null) {
					out.println(record.toString());
				} else {
					out.println("No record for that social security number exists");
				}
			} else {
				out.println("Wrong format. Try again");
			}
		} else if (command.contains("-h")) {
			System.out.println("Example: -g SocialSecurityNumber");
			System.out
					.println("Example: -p Firstname Surname DivisionID NurseIDs SocialSecurityNumber");
			System.out.println("Example: -e SocialSecurityNumber");
		} else if (command.contains("-p")) {
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
			//System.out.println("Recieved " + comment + " from client");
			out.flush();
			//System.out.println("done\n");

			Record record = new Record(socialSecurityNumber, firstName,
					surName, divisionID, comment);
			int result = db.putRecord(user, record, divisionID, new Integer(
					user.getID()), nurseIDs, socialSecurityNumber);
			if (result == 0) {
				out.println("Record added");
			}
		} else if (command.contains("-e")) {
			String[] infos = command.split(" ");
			String socialSecurityNumber = infos[1];
			if (socialSecurityNumber.length() == 10) {
				socialSecurityNumber = "19" + socialSecurityNumber;
			}
			if (socialSecurityNumber.length() == 12) {
				Record record = db.getRecord(socialSecurityNumber, user);
				String firstName = record.getFirstName();
				String surName = record.getSurName();
				int divisionID = record.getDivisionID();
				ArrayList<Integer> nurseIDs = db.getRecordEntry(
						socialSecurityNumber, user).getNurseIDs();
				String comment = record.getComment();
				out.println(record.toString());
				out.println("What do you want to change? \n FirstName -fn \nSurname -sn \n Comment -co \n Divison -di \n SocialSecurityNumber -scc \n Quit -q");
				out.println("listen");
				String edit = in.readLine();
				out.flush();
				while (!edit.contains("-q")) {
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

					int result = db.putRecord(user, record, divisionID,
							new Integer(user.getID()), nurseIDs,
							socialSecurityNumber);
					if (result == 0) {
						out.println("Record added");
					}
					out.println(record.toString());
					out.println("Nästa ändring: ");
					out.println("listen");
					edit = in.readLine();
				}
			}
		} else {
			out.println("Wrong format. Try again");
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
