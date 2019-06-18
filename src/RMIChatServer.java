
import java.rmi.*;

public interface RMIChatServer extends Remote{
	public void register(RMIChatClient client, String name, String portNum) throws RemoteException;
	public void unregister(RMIChatClient client, String name, String portNum) throws RemoteException;
	public void broadcast(String msg) throws RemoteException;
}