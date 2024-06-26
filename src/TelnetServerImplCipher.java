import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Base64;

//implementacja działania serwera w przypadku szyfrowania komunikacji
public class TelnetServerImplCipher extends UnicastRemoteObject implements TelnetServer {
    private SecretKey secretKey;//przechowuje klucz do szyfrowania komunikacji
    public TelnetServerImplCipher(SecretKey secretKey) throws RemoteException {
        super();
        this.secretKey = secretKey;//klucz przekazywany w konstruktorze
    }
    //funkcja szyfrująca tekst
    private String encrypt(String message) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    //funkcja deszyfrująca tekst
    private String decrypt(String encryptedMessage) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
        return new String(decryptedBytes);
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
    public String executeCommand(String command) throws Exception {
            result = new StringBuilder();//nowa zawartosc
            try {
                // deszyfrowanie otrzymanego polecenia
                String decryptedCommand = decrypt(command);
                //jesli windows uruchom cmd i komende, jesli linux uruchom basha i komende
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    process = Runtime.getRuntime().exec("cmd.exe /c "+decryptedCommand);
                } else if (os.contains("nix") || os.contains("nux")) {
                    process =  Runtime.getRuntime().exec("/bin/bash");
                    process =  Runtime.getRuntime().exec(decryptedCommand);
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
        return encrypt(result.toString());//zwraca odszyfrowane wyjście komendy
    }
    @Override
    public String getOSInfo() throws Exception {//pobiera informacje o serwerze
       return encrypt("\nSERVER INFORMATION:\n"+"Operating system: "+System.getProperty("os.name")+" version: "+
               System.getProperty("os.version")+"\nOS architecture: "+System.getProperty("os.arch")
               +"\nJava Version: "+System.getProperty("java.version")+"\nUsername: "+System.getProperty("user.name")+
               "\nHostname: "+getHostName());
    }

}
