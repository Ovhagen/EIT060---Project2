package Client;
import java.io.BufferedReader;
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
        String host = null;
        int port = -1;
        for (int i = 0; i < args.length; i++) {
            System.out.println("args[" + i + "] = " + args[i]);
        }
        if (args.length < 2) {
            System.out.println("USAGE: java client host port");
            System.exit(-1);
        }
        try { /* get input parameters */
            host = args[0];
            port = Integer.parseInt(args[1]);
        } catch (IllegalArgumentException e) {
            System.out.println("USAGE: java client host port");
            System.exit(-1);
        }
        
        /******Tillagd kod f�r test*****/
        /*
        boolean login = false;
        Scanner scan = new Scanner(System.in);
        String userName = null;
        String password = null;
        FileInputStream file = null;
        
        while(login){
        	try{
        	userName = scan.nextLine();
        	System.out.println("Username: ");
        	password = scan.nextLine();
        	System.out.println("Password: ");
        	
        	file = new FileInputStream("");
        	
        	}catch(FileNotFoundException e){
        		e.printStackTrace();
        		System.out.println("You entered the wrong username or password. Please try again.");
        	}
        }
        */
        /******Tillagd kod slut*****/

        try { /* set up a key manager for client authentication */
            SSLSocketFactory factory = null;
            try {
                //char[] pass = password.toCharArray();
            	char[] pass = "password".toCharArray();
                KeyStore ks = KeyStore.getInstance("JKS");
                KeyStore ts = KeyStore.getInstance("JKS");
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                SSLContext ctx = SSLContext.getInstance("TLS");
                
                /**Set and trim path to folders*/
                URL tempLocation = server.class.getProtectionDomain().getCodeSource().getLocation();
                String location = "" + tempLocation;
                location = location.substring(5, location.length()-5);
              
                ks.load(new FileInputStream(location + "/certificates/Client/clientkeystore"), pass);  // keystore password (storepass)
				ts.load(new FileInputStream(location + "/certificates/Client/clienttruststore"), pass); // truststore password (storepass);
				kmf.init(ks, pass); // user password (keypass)
				tmf.init(ts); // keystore can be used as truststore here
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
                factory = ctx.getSocketFactory();
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
            SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
            System.out.println("\nsocket before handshake:\n" + socket + "\n");

            /*
             * send http request
             *
             * See SSLSocketClient.java for more information about why
             * there is a forced handshake here when using PrintWriters.
             */
            socket.startHandshake();

            SSLSession session = socket.getSession();
            X509Certificate cert = (X509Certificate)session.getPeerCertificateChain()[0];
            String subject = cert.getSubjectDN().getName();
            
            String issuer = cert.getIssuerDN().getName();
            String serial = cert.getSerialNumber().toString();
            
            System.out.println("certificate name (subject DN field) on certificate received from server:\n" + subject + "\n" + "issuer name (issuer DN field) on certificate received from server:\n" + issuer);
            System.out.println("Serial number: " + serial);
            System.out.println("socket after handshake:\n" + socket + "\n");
            System.out.println("secure connection established\n\n");

            BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg;
			for (;;) {
                System.out.print(">");
                msg = read.readLine();
                if (msg.equalsIgnoreCase("quit")) {
				    break;
				}
                System.out.print("sending '" + msg + "' to server...");
                out.println(msg);
                out.flush();
                System.out.println("done");

                System.out.println("received '" + in.readLine() + "' from server\n");
            }
            in.close();
			out.close();
			read.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
