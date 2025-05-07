import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String DIR_ARRIBADA = "/tmp"; // Equivalent on Linux/Mac
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void connectar() throws IOException {
        System.out.println("Connectant a -> localhost:9999");
        socket = new Socket("localhost", 9999);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        System.out.println("Connexio acceptada: " + socket.getRemoteSocketAddress());
    }

    public void rebreFitxers() throws IOException, ClassNotFoundException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Nom del fitxer a rebre ('sortir' per sortir): ");
        String nomFitxer = sc.nextLine();

        if ("sortir".equalsIgnoreCase(nomFitxer)) {
            System.out.println("Sortint...");
            return;
        }

        out.writeObject(nomFitxer);
        out.flush();

        String nomSortida = DIR_ARRIBADA + "/" + new File(nomFitxer).getName();
        System.out.println("Nom del fitxer a guardar: " + nomSortida);

        byte[] contingut = (byte[]) in.readObject();
        if (contingut == null) {
            System.out.println("No s'ha pogut rebre el fitxer.");
            return;
        }

        FileOutputStream fos = new FileOutputStream(nomSortida);
        fos.write(contingut);
        fos.close();
        System.out.println("Fitxer rebut i guardat com: " + nomSortida);
    }

    public void tancarConnexio() throws IOException {
        if (socket != null) socket.close();
        System.out.println("Connexio tancada.");
    }

    public static void main(String[] args) {
        try {
            Client c = new Client();
            c.connectar();
            c.rebreFitxers();
            c.tancarConnexio();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
