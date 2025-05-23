import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorXat {
    public static final int PORT = 9999;
    public static final String HOST = "localhost";
    public static final String MSG_SORTIR = "sortir";
    private Hashtable<String, GestorClients> gestorClients = new Hashtable<>();
    private boolean sortir = false;
    private ServerSocket serverSocket;

    public void servidorAEscoltar() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciat a " + HOST + ":" + PORT);
    }

    public void pararServidor() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Error al tancar el servidor: " + e.getMessage());
        }
    }

    public void finalitzarXat() {
        enviarMissatgeGrup(Missatge.getMissatgeSortirTots(MSG_SORTIR));
        gestorClients.clear();
        System.out.println("DEBUG: multicast sortir");
        System.exit(0);
    }

    public void afegirClient(GestorClients client) {
        gestorClients.put(client.getNom(), client);
        enviarMissatgeGrup(Missatge.getMissatgeGrup("Entra: " + client.getNom()));
        System.out.println("DEBUG: multicast Entra: " + client.getNom());
    }

    public void eliminarClient(String nom) {
        if (gestorClients.containsKey(nom)) {
            gestorClients.remove(nom);
            System.out.println(nom + " eliminat.");
        }
    }

    // ServidorXat.java (modificació del mètode enviarMissatgeGrup)
    public void enviarMissatgeGrup(String missatge) {
        String missatgeFormatat = Missatge.getMissatgeGrup(missatge); // Afegit
        gestorClients.forEach((nom, client) -> {
            try {
                client.out.writeObject(missatgeFormatat); // Enviem directament el missatge formatat
            } catch (IOException e) {
                System.out.println("Error enviant a " + nom + ": " + e.getMessage());
            }
        });
    }

    public void enviarMissatgePersonal(String destinatari, String remitent, String missatge) {
        GestorClients client = gestorClients.get(destinatari);
        if (client != null) {
            client.enviarMissatge(remitent, missatge);
            System.out.println("Missatge personal per (" + destinatari + ") de (" + remitent + "): " + missatge);
        }
    }

    public static void main(String[] args) {
        ServidorXat servidor = new ServidorXat();
        try {
            servidor.servidorAEscoltar();
            while (!servidor.sortir) {
                Socket clientSocket = servidor.serverSocket.accept();
                System.out.println("Client connectat: " + clientSocket.getRemoteSocketAddress());
                GestorClients gestor = new GestorClients(clientSocket, servidor);
                new Thread(gestor).start();
            }
        } catch (IOException e) {
            if (!servidor.sortir) System.out.println("Error al escoltar: " + e.getMessage());
        } finally {
            servidor.pararServidor();
        }
    }
}