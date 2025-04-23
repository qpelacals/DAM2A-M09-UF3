import java.io.*;
import java.net.*;

public class ServidorXat {
    private static final int PORT = 9999;
    private static final String HOST = "localhost";
    public static final String MSG_SORTIR = "sortir";

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public void iniciarServidor() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciat a " + HOST + ":" + PORT);
        clientSocket = serverSocket.accept();
        System.out.println("Client connectat: " + clientSocket.getInetAddress());
    }

    public void pararServidor() throws IOException {
        if (in != null) in.close();
        if (out != null) out.close();
        if (clientSocket != null) clientSocket.close();
        if (serverSocket != null) serverSocket.close();
        System.out.println("Servidor aturat.");
    }

    public String getNom() throws IOException, ClassNotFoundException {
        in = new ObjectInputStream(clientSocket.getInputStream());
        return (String) in.readObject();
    }

    public static void main(String[] args) {
        try {
            ServidorXat servidor = new ServidorXat();
            servidor.iniciarServidor();

            servidor.out = new ObjectOutputStream(servidor.clientSocket.getOutputStream());

            String nom = servidor.getNom();
            System.out.println("Nom rebut: " + nom);
            System.out.println("Fil de xat creat.");
            System.out.println("Fil de " + nom + " iniciat");

            FilServidorXat fil = new FilServidorXat(servidor.in);
            fil.start();

            BufferedReader teclat = new BufferedReader(new InputStreamReader(System.in));
            String missatge;
            do {
                System.out.print("Missatge ('sortir' per tancar): ");
                missatge = teclat.readLine();
                servidor.out.writeObject(missatge);
            } while (!missatge.equals(MSG_SORTIR));

            fil.join();
            servidor.pararServidor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}