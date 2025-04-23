import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientXat {
    private static final String HOST = "localhost";
    private static final int PORT = 9999;
    private static final String MSG_SORTIR = "sortir";

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public void connecta() throws IOException {
        socket = new Socket(HOST, PORT);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        System.out.println("Client connectat a " + HOST + ":" + PORT);
        System.out.println("Flux d'entrada i sortida creat.");
    }

    public void enviarMissatge(String missatge) throws IOException {
        out.writeObject(missatge);
        System.out.println("Enviant missatge: " + missatge);
    }

    public void tancarClient() throws IOException {
        if (in != null) in.close();
        if (out != null) out.close();
        if (socket != null) socket.close();
        System.out.println("Client tancat.");
    }

    public static void main(String[] args) {
        try {
            ClientXat client = new ClientXat();
            client.connecta();

            Scanner teclat = new Scanner(System.in);
            System.out.print("Missatge ('sortir' per tancar): Fil de lectura iniciat\n");
            System.out.print("Escriu el teu nom: ");
            String nom = teclat.nextLine();
            client.enviarMissatge(nom);

            FilLectorCX fil = new FilLectorCX(client.in);
            fil.start();

            String missatge;
            do {
                System.out.print("Missatge ('sortir' per tancar): ");
                missatge = teclat.nextLine();
                client.enviarMissatge(missatge);
            } while (!missatge.equals(MSG_SORTIR));

            teclat.close();
            fil.join();
            System.out.println("Tancant client...");
            client.tancarClient();
            System.out.println("El servidor ha tancat la connexió.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}