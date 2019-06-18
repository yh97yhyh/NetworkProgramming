
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.Scanner;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.ssl.*;

public class RMIChatClientApp extends Frame implements RMIChatClient, ActionListener {
	
	TextArea display;
	TextField input, host, id, port, passwd;
	RMIChatServer server;
	CardLayout card;
	Button login, clear;
	static String name, pv;
	static String serverName, portNum;
	static String sServer;
	static int sPort;
	static InetAddress inetaddr = null;
	static SSLSocket sslSocket = null;
	static SSLSocketFactory sslSocketFactory = null;
	static Socket chatSocket = null;
	
	public RMIChatClientApp() {
		super("RMI Chat");
		setLayout(card = new CardLayout());
		
		//GUI with JPanel
		Panel bottom = new Panel(new GridLayout(5,2)){
			public Insets getInsets() {
				return new Insets(10,10,10,10);
			}
		};
		
		
		
		bottom.add(new Label("ID", Label.RIGHT));
		bottom.add(id = new TextField());
		bottom.add(new Label("PASSWORD", Label.RIGHT));
		bottom.add(passwd = new TextField());
		bottom.add(new Label("Host", Label.RIGHT));		//serverName
		bottom.add(host = new TextField());
		bottom.add(new Label("Port", Label.RIGHT));		//portNumber
		bottom.add(port = new TextField());
	
		bottom.add(new Label(""));
		bottom.add(login = new Button("Login"));
		
		login.addActionListener(this);
		Panel loginPanel = new Panel(new BorderLayout());
		loginPanel.add(bottom, BorderLayout.SOUTH);
		loginPanel.add(new JLabel(new ImageIcon("river.jpg")), BorderLayout.CENTER);
		Panel chatBottom = new Panel(new BorderLayout());
		input = new TextField();
		input.addActionListener(this);
		clear = new Button("clear");
		clear.addActionListener(this);
		chatBottom.add(input, BorderLayout.CENTER);
		chatBottom.add(clear, BorderLayout.EAST);
		
		Panel chatPanel = new Panel(new BorderLayout());
		display = new TextArea();
		display.setEditable(false);
		chatPanel.add(chatBottom,BorderLayout.SOUTH);
		chatPanel.add(display ,BorderLayout.CENTER);	
		add("login", loginPanel);
		add("chat", chatPanel);
		card.show(this, "login");
		
		addWindowListener( new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
			setVisible(false);
				if(server != null) {
					try{
						server.unregister(RMIChatClientApp.this, name, portNum);
					} catch(Exception ex) {}
				}
			}
		}); 
		  
		setSize(1000,1000);
		setVisible(true);
		
	}
	
	@SuppressWarnings("deprecation")
	private void connect() {
		try{
			
			UnicastRemoteObject.exportObject(this);
			
			//RMI
			server = (RMIChatServer)Naming.lookup("//" +serverName+ "/chat");
			server.register(this, name, portNum);
			
		} catch(Exception e){
				e.printStackTrace();
		}
	}
	public synchronized void setMessage(String msg){
		try{
			int index = msg.indexOf(": !!");
			if(index > 0) {
				new MessageWindow(this, msg);
				}else{
					display.append(msg+"\n");
				}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void actionPerformed(ActionEvent e){
		Component c = (Component)e.getSource();
		if(c==login){
			
			name = id.getText().trim();
			pv = passwd.getText().trim();
			serverName = host.getText().trim();
			portNum = port.getText().trim();
			
			sServer = serverName;
			sPort = Integer.parseInt(portNum);

			
			if(name.length()>0 && serverName.length()>0){
				card.show(this, "chat");
				connect();
			}
		} else if(c==input){
			try{
				//broadcasting
				server.broadcast(name + ":" +input.getText());
				input.setText("");
				
			}catch(Exception ex){
				display.append(ex.toString());
			}
		} else if(c==clear){
			display.setText("");
		}
	}
	public static void main(String[] args){
	
		if (args.length != 2) {
			System.out.println("Usage: Classname ServerName ServerPort");
			System.exit(1);
		}

		sServer = args[0];
		sPort = Integer.parseInt(args[1]);
				
		System.out.println("before");
		new RMIChatClientApp();
		System.out.println("after");	
		
		try {
			
			System.setProperty("javax.net.ssl.trustStore", "trustedcerts");
			System.setProperty("javax.net.ssl.trustStorePassword", "20160318");
			
			inetaddr = InetAddress.getByName(sServer);
			sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			
			sslSocket = (SSLSocket) sslSocketFactory.createSocket(inetaddr, sPort);
			sslSocket.startHandshake();
			
		}
		catch (IOException io) {
		}
		
		new Thread(new ClientReceiver(sslSocket,sPort)).start();	
		new Thread(new ClientSender(sslSocket, sServer, sPort)).start();
	}

static class ClientSender implements Runnable {
	
	private SSLSocket chatSocket = null;
	String sServer;
	int port;
	
	ClientSender(SSLSocket socket, String sServer, int sPort){
		this.chatSocket = socket;
		this.sServer = sServer;
		this.port = sPort;
	}
		public void run() {
			Scanner KeyIn = null;
			PrintWriter out = null;
			try {
				KeyIn = new Scanner(System.in);
				out = new PrintWriter(chatSocket.getOutputStream(), true);
				
				String userInput = "";
				userInput = String.valueOf(port);
				while((userInput = KeyIn.nextLine()) != null) {
					out.println(userInput);
					out.flush();
					if(userInput.equalsIgnoreCase("Bye."))
						break;
				}
			
			}catch(IOException i) {
				try {
					if(out != null) out.close();
					if(KeyIn != null) KeyIn.close();
					if(chatSocket != null) chatSocket.close();
				} catch(IOException e) {
					
				}
				System.exit(1);
			}
			
		}
	
}


static class ClientReceiver implements Runnable {
	private SSLSocket chatSocket = null;
	int port;
	ClientReceiver(SSLSocket socket, int sPort){
		this.chatSocket = socket;
		this.port = sPort;
	}
	
	public void run() {
		while(chatSocket.isConnected()) {
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(chatSocket.getInputStream()));
				String readSome = null;
				readSome = String.valueOf(port);
				
				while((readSome = in.readLine()) != null) {
					System.out.println(readSome);
				}
			}catch(IOException i) {
				try {
					if(in != null) 
						in.close();
					if(chatSocket != null) 
						chatSocket.close();
				}catch(IOException e) {
					
				}
				System.out.println("leave.");
				System.exit(1);
			}
		}
	}
	
}
	
}
