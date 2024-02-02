import java.rmi.Remote;

public interface TelnetServer extends Remote {
    //interfejs dla implementacji serwera
    String executeCommand(String command) throws Exception;
    String getOSInfo() throws Exception;
}
