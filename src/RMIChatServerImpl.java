
import java.rmi.server.*;
import java.rmi.*;
import java.util.*;
import java.io.*;
import java.net.BindException;
import java.net.SocketTimeoutException;
import java.security.*;
import java.security.cert.CertificateException;

import javax.net.ssl.*;


public class RMIChatServerImpl extends UnicastRemoteObject implements RMIChatServer, Runnable{
	static Vector<RMIChatClient> clientList;
	static int portNumber;
	static int sPort;
	static String sName;
	private RMIChatServerRunnable clients[] = new RMIChatServerRunnable[5];
	public int clientCount = 0;
	private KeyStore ks;
    private KeyManagerFactory kmf;
    private SSLContext sc;
    SSLServerSocketFactory sslServerSocketFactory = null;
    SSLServerSocket sslServerSocket = null;
    SSLSocket sslSocket = null;
      
    final String runRoot = "C:\\Users\\do0ob\\Desktop\\»õ Æú´õ\\project\\src\\";  // root change : your system root

	public RMIChatServerImpl(int port) throws RemoteException {
		super();
		this.sPort = port;
	}
	

	public RMIChatServerImpl() throws RemoteException {
		//client list with vector
		clientList = new Vector<RMIChatClient>();
	}


	public synchronized void register(RMIChatClient client, String name, String portNum) throws RemoteException {
		
		portNumber = Integer.parseInt(portNum);
		
		//if portNumber from server and sPort from Client is same
		if(sPort == portNumber) {		
			clientList.add(client);
			broadcast(name+" entered\n");
		}	
		
		//else, print this message
		else {
			System.out.println("server port : "+ sPort +" and client port : " + portNumber+" is different.\n");
			System.out.println("please re-execute clientApp.");
		}		
		
	}
	
	
	public synchronized void unregister(RMIChatClient client, String name, String portNum) throws RemoteException {
		
		clientList.remove(client);
		
		broadcast(name+" leaved\n");
	}
	
	public void broadcast(String msg) throws RemoteException{
		synchronized(clientList){
			for(RMIChatClient c : clientList)
			c.setMessage(msg);
		}
	}
	
	
	
	
	public void run() {
		SSLServerSocket serverSocket = null;
		String ksName = runRoot + ".keystore\\myKey"; 
	     
	    char keyStorePass[] = "20160318".toCharArray();
	      
	    char keyPass[] = "20160318".toCharArray();

		try {
		
			System.out.println ("Server started: socket created on " + sPort);

	         ks = KeyStore.getInstance("JKS");	   
	         ks.load(new FileInputStream(ksName),keyStorePass);
	         
	         kmf = KeyManagerFactory.getInstance("SunX509");
	         kmf.init(ks, keyPass);
	    	
	    	 sc = SSLContext.getInstance("TLS");
	         sc.init(kmf.getKeyManagers(), null, null);
	         
	         sslServerSocketFactory = sc.getServerSocketFactory();
	         sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(sPort);
	         //RMI server
	    	 RMIChatServerImpl server = new RMIChatServerImpl();
	    	 Naming.rebind("//" + sName + "/chat", server);
	    	 System.out.println("RMI Chat Server is ready");
			
			
	
			
			while (true) {
				addClient(serverSocket);
				}
	    	 }catch (BindException b) {
	    		 System.out.println("Can't bind on: "+sPort);
	    	 } catch (IOException i) {
	    		 System.out.println(i);
	    	 } catch (KeyStoreException e) {
	    		 // TODO Auto-generated catch block
	    		 e.printStackTrace();
	    	 } catch (NoSuchAlgorithmException e) {
	    		 // TODO Auto-generated catch block
	    		 e.printStackTrace();
	    	 } catch (CertificateException e) {
				// TODO Auto-generated catch block
	    		 e.printStackTrace();
	    	 } catch (UnrecoverableKeyException e) {
	    		 // TODO Auto-generated catch block
	    		 e.printStackTrace();
	    	 } catch (KeyManagementException e) {
	    		 // TODO Auto-generated catch block
	    		 e.printStackTrace();
	    	 } finally {
	    		 try {
	    			 if (serverSocket != null) serverSocket.close();
	    		 } catch (IOException i) {
	    			 System.out.println(i);
	    		 }
	    	 }
	}
	
	public int whoClient(int clientID) {
		for (int i = 0; i < clientCount; i++)
			if (clients[i].getClientID() == clientID)
				return i;
		return -1;
	}
	
	public void putClient(int clientID, String inputLine) {
		for (int i = 0; i < clientCount; i++)
			if (clients[i].getClientID() == clientID) {
				System.out.println("writer: "+clientID);
			} else {
				System.out.println("write: "+clients[i].getClientID());
				clients[i].out.println(inputLine);
			}
	}
	
	public void addClient(SSLServerSocket serverSocket) {
		SSLSocket clientSocket = null;
		
		if (clientCount < clients.length) { 
			try {
				clientSocket = (SSLSocket) sslServerSocket.accept();
				//clientSocket.setSoTimeout(1000000); // 1000/sec
			} catch (IOException i) {
				System.out.println ("Accept() fail: "+i);
			}
			clients[clientCount] = new RMIChatServerRunnable(this, clientSocket);
			new Thread(clients[clientCount]).start();
			clientCount++;
			System.out.println ("Client connected: " + clientSocket.getPort()
					+", CurrentClient: " + clientCount);
		} else {
			try {
				SSLSocket dummySocket = (SSLSocket) serverSocket.accept();
				RMIChatServerRunnable dummyRunnable = new RMIChatServerRunnable(this, dummySocket);
				new Thread(dummyRunnable);
				dummyRunnable.out.println(dummySocket.getPort()
						+ " < Sorry maximum user connected now");
				System.out.println("Client refused: maximum connection "
						+ clients.length + " reached.");
				dummyRunnable.close();
			} catch (IOException i) {
				System.out.println(i);
			}	
		}
	}
	
	public synchronized void delClient(int clientID) {
		int pos = whoClient(clientID);
		RMIChatServerRunnable endClient = null;
	      if (pos >= 0) {
	    	   endClient = clients[pos];
	    	  if (pos < clientCount-1)
	    		  for (int i = pos+1; i < clientCount; i++)
	    			  clients[i-1] = clients[i];
	    	  clientCount--;
	    	  System.out.println("Client removed: " + clientID
	    			  + " at clients[" + pos +"], CurrentClient: " + clientCount);
	    	  endClient.close();
	      }
	}
	
	public static void main(String[] args) throws RemoteException{
		
	
		
	    if (args.length != 2) {
			System.out.println("Usage: Classname ServerName Port");
			System.exit(1);
		}
	    //from argument
		sPort = Integer.parseInt(args[1]);
	    sName = args[0];
	    
         
	   new Thread(new RMIChatServerImpl(sPort)).start();    
		
	}

}

class RMIChatServerRunnable implements Runnable {
	static Vector<RMIChatClient> clientList;
	protected RMIChatServerImpl chatServer = null;
	protected SSLSocket clientSocket = null;
	protected PrintWriter out = null;
	protected BufferedReader in = null;
	public int clientID = -1;
	
	String sName = null;
	
	public RMIChatServerRunnable(RMIChatServerImpl rmiChatServerImpl, SSLSocket socket) {
		this.chatServer = rmiChatServerImpl;
		this.clientSocket = socket;
		clientID = clientSocket.getPort();
		try {
			out= new PrintWriter(clientSocket.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		}catch(IOException i) {
			
		}
		
	}

	public void run() {
		try {
			String inputLine;
			while((inputLine = in.readLine())!=null) {
				chatServer.putClient(getClientID(),getClientID()+": "+inputLine);
			
				if(inputLine.equalsIgnoreCase("Bye."))
			
					break;
			}
			chatServer.delClient(getClientID());
			
		}catch(SocketTimeoutException ste) {
			System.out.println("Socket timeout Occurred, force close() :"+getClientID());
			chatServer.delClient(getClientID());
		}catch(IOException e) {
			chatServer.delClient(getClientID());
		}
	}
	
	public int getClientID() {
		return clientID;
	}
	
	public void close() {
		try {
			if(in != null) in.close();
			if(out != null) out.close();
			if(clientSocket != null) clientSocket.close();
		}catch(IOException i) {}
	}
}