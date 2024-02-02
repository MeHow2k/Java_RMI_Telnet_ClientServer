import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class CheckRMIServer {
    static Scanner scanner  = new Scanner(System.in);
    static String PORT;
    public static void main(String[] args) {

        try {
            // Domyślny port RMI to 1099
            System.out.println("Enter server's IP address:");
            String IP = scanner.nextLine();
            System.out.println("Enter server's PORT:");
            String PORT = scanner.nextLine();
            Registry registry = LocateRegistry.getRegistry(IP, Integer.parseInt(PORT));

            try {
                // Pobierz obiekt InetAddress reprezentujący lokalny adres IP
                InetAddress localHost = InetAddress.getLocalHost();

                // Pobierz nazwę hosta
                String hostname = localHost.getHostName();

                System.out.println("Hostname of the local machine: " + hostname);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            // Pobierz tablicę nazw obiektów z rejestru
            String[] boundObjects = registry.list();

            // Wypisz nazwy i adresy IP obiektów
            for (String boundObject : boundObjects) {
                String objectHost = registry.lookup(boundObject).toString();
                System.out.println("Object: " + boundObject + ", IP Address: " + objectHost);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
