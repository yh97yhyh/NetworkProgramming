
import java.rmi.*;

public interface RMIChatClient extends Remote{
	public void setMessage(String msg) throws RemoteException;
}
