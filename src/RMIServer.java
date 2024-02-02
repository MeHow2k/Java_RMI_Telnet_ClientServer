import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class RMIServer {
    private static String IP;
    private static String PORT;
    private static TelnetServer telnetServer;
    private static SecretKey secretKey;
    private static boolean cipherOption=false;
    private static Scanner scanner  = new Scanner(System.in);

    public static void IPInput() {
        do {
            System.out.println("Enter server IP address:");
            IP = scanner.nextLine();
        } while (!isValidIP(IP));

        do {
            System.out.println("Enter server PORT:");
            PORT = scanner.nextLine();
        } while (!isValidPort(PORT));

        String input;
        boolean validInput = false;
        do {
            System.out.println("Do you want cipher communication?(y/n):");
            input = scanner.nextLine();
            if (input.equalsIgnoreCase("y")) {
                cipherOption = true;
                System.out.println("Cipher enabled");
                validInput = true;
            } else if (input.equalsIgnoreCase("n")) {
                cipherOption = false;
                System.out.println("Cipher disabled");
                validInput = true;
            } else {
                System.out.println("Invalid input! Please enter 'y' or 'n'!");
            }
        } while (!validInput);
    }
    public static void generateKey() {
        SecretKey key;
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            key = keyGenerator.generateKey();
            System.out.println("New key generated.");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return ;
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("serverKey"))) {
            oos.writeObject(key);
            System.out.println("Key saved to file: serverKey");
            secretKey = key;
            System.out.println("WARNING! In order to apply changes you need to restart server application!");
            System.out.println("Without that, incoming client won't be able to communicate!");
            System.out.println("Type \"q\" to exit, then rerun server application!");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error saving key to file: " + e.getMessage());
        }
    }
    public static SecretKey readKeyFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("serverKey"))) {
            Object obj = ois.readObject();
            if (obj instanceof SecretKey) {
                System.out.println("Key read from file.");
                return (SecretKey) obj;
            } else {
                System.err.println("Invalid key file content.");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error reading key from file: " + e.getMessage());
            return null;
        }
    }
    private static boolean isValidIP(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            return true;
        } catch (java.net.UnknownHostException e) {
            return false;
        }
    }
    public static boolean isValidPort(String port) {
        try {
            int portNumber = Integer.parseInt(port);
            return portNumber >= 1025 && portNumber <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public static void main(String[] args) {
        System.out.println("Telnet Server program. Michal Pasieka 2024. ");
        IPInput();
        secretKey = readKeyFromFile();
        try {
            System.setProperty("java.rmi.server.hostname", IP);
            if (cipherOption) telnetServer = new TelnetServerImplCipher(secretKey);
            else telnetServer = new TelnetServerImpl();
            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(PORT));
            registry.rebind("TelnetServer", telnetServer);
            System.out.println("Telnet server is up.");
            System.out.println("If you want exit type \"q\", to generate new key type \"newkey\".");
            while (true){
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("q")) System.exit(1);
                else if (input.equalsIgnoreCase("newkey")) {
                    generateKey();
                }
            }
        } catch (Exception e) {
            System.err.println("Server error: " + e.toString());
            System.out.println("Exiting...");
            System.exit(0);
        }
    }
}
