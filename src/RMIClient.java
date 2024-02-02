import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Base64;
import java.util.Scanner;

public class RMIClient {
    static String IP;
    static String PORT;
    static TelnetServer telnetServer;
    static Scanner scanner  = new Scanner(System.in);
    private static SecretKey secretKey;
    static boolean isFirst=true;
    static boolean cipherOption=false;
    static String result;

    private static String encrypt(String message, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private static String decrypt(String encryptedMessage, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
        return new String(decryptedBytes);
    }
    public static SecretKey readKeyFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("serverKey"))) {
            Object obj = ois.readObject();
            if (obj instanceof SecretKey) {
                System.out.println("Key read from file..." );
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
    public static void IPInput() {
        do {
            System.out.println("Enter server IP address!");
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
    public static void writeHelp() throws RemoteException {
        System.out.println("\"q\" to exit program.\n" +
                "\"help\"   to get information about program commands.\n" +
                "\"changeip\"   to change telnet server.\n" +
                "\"serverinfo\" to write information of server you are connected to.\n" +
                "\"iscipher\"   to write if encryption and decryption is enabled. \n");
    }
    public static void main(String[] args) {
        System.out.println("Telnet Client program. Michal Pasieka 2024. ");
        IPInput();
        if(cipherOption) secretKey=readKeyFromFile();
        while(true) {
            try {
                System.setProperty("java.rmi.server.hostname", IP);
                Registry registry = LocateRegistry.getRegistry(IP, Integer.parseInt(PORT));
                telnetServer = (TelnetServer) registry.lookup("TelnetServer");
                if(isFirst) {
                    if (cipherOption) System.out.println("Server information:\n"+decrypt(telnetServer.getOSInfo(),secretKey));
                    else System.out.println("Connected with: "+IP+telnetServer.getOSInfo());
                    isFirst=false;
                    System.out.println("Type \"q\" to exit, \"help\" to get information about program commands. ");
                }
                System.out.println("Enter command: ");
                String userInput = scanner.nextLine();
                if(userInput.equalsIgnoreCase("q")) {
                    System.out.println("Exiting...");
                    break;
                } else if (userInput.equalsIgnoreCase("changeip")) {
                    IPInput();
                    isFirst=true;
                    continue;
                }else if (userInput.equalsIgnoreCase("help") || userInput.equalsIgnoreCase("?")) {
                    writeHelp();
                    continue;
                }else if (userInput.equalsIgnoreCase("serverinfo")) {
                    if (cipherOption) System.out.println("Server information:\n"+decrypt(telnetServer.getOSInfo(),secretKey));
                    else System.out.println("Server information:\n"+telnetServer.getOSInfo());
                    continue;
                }else if (userInput.equalsIgnoreCase("iscipher")) {
                    System.out.println("Cipher enabled:"+cipherOption+"\n");
                    continue;
                }
                if (cipherOption)  {
                    String encryptedMessage = encrypt(userInput, secretKey);
                    result = telnetServer.executeCommand(encryptedMessage);
                    String decryptedResult = decrypt(result, secretKey);
                    System.out.println("Server's response: \n" + decryptedResult);
                }
                if (!cipherOption) {
                    result = telnetServer.executeCommand(userInput);
                    System.out.println("Server's response: \n" + result);
                }
            } catch (ConnectException e) {
                System.err.println("Can't connect: " + e.toString());
                System.out.println("Exiting...");
                System.exit(0);
            } catch (AccessException e) {
                System.err.println("Can't access: " + e.toString());
                System.out.println("Exiting...");
                System.exit(0);
            } catch (NotBoundException e) {
                System.err.println("NotBound: " + e.toString());
                System.out.println("Exiting...");
                System.exit(0);
            } catch (RemoteException e) {
                System.err.println("RemoteException: " + e.toString());
                System.out.println("Exiting...");
                System.exit(0);
        } catch (Exception e) {
                System.err.println("Exception: " + e.toString());
                System.out.println("Exiting...");
                System.exit(0);
            }
        }
    }
}
