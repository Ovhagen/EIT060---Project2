package Client;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.security.KeyStore;
import java.util.Scanner;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.X509Certificate;


import Server.server;

/*
 * This example shows how to set up a key manager to perform client
 * authentication.
 *
 * This program assumes that the client is not inside a firewall.
 * The application can be modified to connect to a server outside
 * the firewall by following SSLSocketClientWithTunneling.java.
 */
public class client {


	public static void main(String[] args) throws Exception {
		new client().init();
	}
	
	public void init() throws IOException{
		String host = "localhost";
		int port = 9876;
		String passwd = "";
		String userPath = "";
		boolean login = true;
		Scanner scan = new Scanner(System.in);
		FileInputStream keyfile = null;
		FileInputStream trustfile = null;
		URL tempLoc = server.class.getProtectionDomain().getCodeSource().getLocation();
		String loc = "" + tempLoc;
		loc = loc.substring(5, loc.length() - 5);
		SSLSocketFactory factory = null;
		Console console = System.console();
		boolean terminate = false;
		
		while (login) {
			System.out.println("Type \"quit\" to exit program");
			try {
				System.out.println("UserID: ");
				userPath = scan.nextLine();
				if(userPath.equalsIgnoreCase("quit")){
					login = false;
					scan.close();
					System.out.println("Quit program");
					terminate = true;
					break;
				}
				/*Users keystore password is entered*/
				char[] pass = console.readPassword("Password: ");
				
				
				keyfile = new FileInputStream(loc + "/certificates/Users/" + userPath + "/" + userPath + "_keystore");
				trustfile = new FileInputStream(loc + "/certificates/Users/" + userPath + "/" + userPath + "_truststore");
				
				//SSLSocketFactory factory = null;
				try {
					
					KeyStore ks = KeyStore.getInstance("JKS");
					KeyStore ts = KeyStore.getInstance("JKS");
					
					KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
					TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
					
					SSLContext ctx = SSLContext.getInstance("TLS");

					/** Set and trim path to folders */
					URL tempLocation = server.class.getProtectionDomain().getCodeSource().getLocation();
					String location = "" + tempLocation;
					location = location.substring(5, location.length() - 5);

					ks.load(keyfile, pass); // keystore
											// password
											// (storepass)
					ts.load(trustfile, pass); // truststore
												// password
												// (storepass);
					kmf.init(ks, pass); // user password (keypass)
					tmf.init(ts); // keystore can be used as truststore here
					ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
					factory = ctx.getSocketFactory();
					login = false;
				} catch (Exception e) {
					System.out.println("Incorrect password");
				}
			} catch (FileNotFoundException e) {
				System.out.println("Username does not exist. Please try again.");
			}
		}
		if(!terminate){
			try { /* set up a key manager for client authentication */
				
				
				SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
				System.out.println("\nsocket before handshake:\n" + socket + "\n");
	
				/*
				 * send http request
				 * 
				 * See SSLSocketClient.java for more information about why there is
				 * a forced handshake here when using PrintWriters.
				 */
				socket.startHandshake();
	
				SSLSession session = socket.getSession();
				X509Certificate cert = (X509Certificate) session.getPeerCertificateChain()[0];
				String subject = cert.getSubjectDN().getName();
	
				String issuer = cert.getIssuerDN().getName();
				String serial = cert.getSerialNumber().toString();
				
				
	
	
	
	
				BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String msg;
				String allowed = in.readLine();
				if(allowed.equalsIgnoreCase("notAllowed")){
					throw new LoginException("User already logged in");
				} 
				
				System.out.println("certificate name (subject DN field) on certificate received from server:\n" + subject
						+ "\n" + "issuer name (issuer DN field) on certificate received from server:\n" + issuer);
				System.out.println("Serial number: " + serial);
				System.out.println("socket after handshake:\n" + socket + "\n");
				System.out.println("secure connection established\n\n");
				
				System.out.println("Succesful login! Type a command in the prompt and press enter.");
				System.out.println("Get -g, Put -p, Edit -e, Help -h, -r Remove, -pa Print All, Logout logout, Quit quit");
				
				for (;;) {
					System.out.print(">");
					msg = read.readLine();
					if (msg.equalsIgnoreCase("quit") || msg.equalsIgnoreCase("logout")) {
						break;
					}
					out.println(msg);
					out.flush();
					String fromServer;
					while ((fromServer = in.readLine()) != null) {
					    if (fromServer.equals("listen")){
					        break;
					    } else {
						    System.out.println(fromServer);
					    }
					}
					
					
				}
				if(msg.equals("logout")){
					System.out.println("Logged out");
					socket.close();
					in.close();
					out.close();
					init();
				} else {
					System.out.println("Exit client");
					socket.close();
					in.close();
					out.close();
					read.close();
				}
			} catch (LoginException e) {
				System.out.println(e.getMessage());
				init();
			}
		}
	}
	public class LoginException extends Exception {
		  public LoginException() { super(); }
		  public LoginException(String message) { super(message); }
		  public LoginException(String message, Throwable cause) { super(message, cause); }
		  public LoginException(Throwable cause) { super(cause); }
		}
	
	

}
