import java.rmi.Remote;

public interface TelnetServer extends Remote {
    String executeCommand(String command) throws Exception;
    String getOSInfo() throws Exception;
}
