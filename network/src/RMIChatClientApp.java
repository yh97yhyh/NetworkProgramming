
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.rmi.*;
import java.rmi.server.*;
import javax.swing.*;

public class RMIChatClientApp extends Frame implements RMIChatClient, ActionListener {
	TextArea display;
	TextField input, host, id, passwd;
	RMIChatServer server;
	CardLayout card;
	Button login, clear;
	String name, serverName;
	
	public RMIChatClientApp() {
		super("RMI Chat");
		setLayout(card = new CardLayout());
		
		Panel bottom = new Panel(new GridLayout(4,2)){
			public Insets getInsets() {
				return new Insets(10,10,10,10);
			}
		};
		
		bottom.add(new Label("ID", Label.RIGHT));
		bottom.add(id = new TextField());
		bottom.add(new Label("PASSWORD", Label.RIGHT));
		bottom.add(passwd = new TextField());
		bottom.add(new Label("Host", Label.RIGHT));
		bottom.add(host = new TextField());
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
						server.unregister(RMIChatClientApp.this, name);
					} catch(Exception ex) {}
				}
			}
		});
		
		setSize(200,200);
		setVisible(true);
		
	}
	
	private void connect() {
		try{
			UnicastRemoteObject.exportObject(this);
			server = (RMIChatServer)Naming.lookup("//" +serverName+ "/chat");
			server.register(this, name);
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
					display.append(msg+"n");
				}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void actionPerformed(ActionEvent e){
		Component c = (Component)e.getSource();
		if(c==login){
			name = id.getText().trim();
			String pv = passwd.getText().trim();
			serverName = host.getText().trim();
			if(name.length()>0 && serverName.length()>0){
				card.show(this, "chat");
				connect();
			}
		} else if(c==input){
			try{
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
		System.out.println("before");
		new RMIChatClientApp();
		System.out.println("after");
	}
}
