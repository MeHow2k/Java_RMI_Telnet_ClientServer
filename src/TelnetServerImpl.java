import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
//implementacja działania serwera w przypadku braku szyfrowania
public class TelnetServerImpl extends UnicastRemoteObject implements TelnetServer {
    public TelnetServerImpl() throws RemoteException {
        super();
    }
    //funkcja pobierająca hostname serwera
    public static String getHostName() {
        String hostname = "";
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            hostname = localHost.getHostName();
        } catch (UnknownHostException e) {
            System.out.println("Can't get your hostname: "+e.toString());
            return "Unknown";
        }
        return hostname;
    }
    Process process;
    StringBuilder result;//przechowuje rezultat komendy
    //funkcja realizująca uruchomienie zdalnie komendy na serwerze
    @Override
    public String executeCommand(String command) throws RemoteException {
            result = new StringBuilder();//nowa zawartość
        //jesli windows uruchom cmd i komende, jesli linux uruchom basha i komende
            try {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    process = Runtime.getRuntime().exec("cmd.exe /c "+command);
                } else if (os.contains("nix") || os.contains("nux")) {
                    process = Runtime.getRuntime().exec("/bin/bash");
                    process = Runtime.getRuntime().exec(command);
                } else {
                    System.out.println("YOUR OS IS UNSUPPORTED!");
                }

                // Pobranie wyniku z procesu
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }

                // Oczekiwanie na zakończenie procesu
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                result.append("Błąd podczas wykonania polecenia: ").append(e.getMessage());
            } catch (Exception e) {
                System.out.println("Exception: "+e.toString());
            }
        return result.toString();//zwraca wyjście komendy
    }
    @Override
    public String getOSInfo() throws RemoteException {//wypisuje info o serwerze
       return "\nSERVER INFORMATION:\n"+"Operating system: "+System.getProperty("os.name")+" version: "+
               System.getProperty("os.version")+"\nOS architecture: "+System.getProperty("os.arch")
               +"\nJava Version: "+System.getProperty("java.version")+"\nUsername: "+System.getProperty("user.name")+
               "\nHostname: "+getHostName();
    }

}
