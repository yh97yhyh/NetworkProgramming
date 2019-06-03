
import java.rmi.server.*;
import java.rmi.*;
import java.util.*;
public class RMIChatServerImpl extends UnicastRemoteObject implements RMIChatServer {
	Vector<RMIChatClient> clientList;
	public RMIChatServerImpl() throws RemoteException {
		super();
		clientList = new Vector<RMIChatClient>();
	}
	public synchronized void register(RMIChatClient client, String name) throws RemoteException {
		clientList.add(client);
		broadcast(name+" entered");
	}
	public synchronized void unregister(RMIChatClient client, String name) throws RemoteException {
		clientList.remove(client);
		broadcast(name+" leaved");
	}
	public void broadcast(String msg) throws RemoteException{
		synchronized(clientList){
			for(RMIChatClient c : clientList)
			c.setMessage(msg);
		}
	}
	
	public static void main(String[] args){
		try{
			RMIChatServerImpl server = new RMIChatServerImpl();
			Naming.rebind("//localhost/chat", server);
			System.out.println("RMI Chat Server is ready");
		} catch(Exception e){
			System.err.println(e);
			e.printStackTrace();
		}
	}
}