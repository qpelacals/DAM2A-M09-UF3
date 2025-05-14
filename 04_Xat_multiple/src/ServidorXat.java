import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class ServidorXat {
    public static final int PORT = 9999;
    public static final String HOST = "localhost";
    public static final String MSG_SORTIR = "sortir";

    private Hashtable<String, GestorClients> clients = new Hashtable<>();
    private boolean sortir = false;
    private ServerSocket serverSocket;

    public void servidorAEscoltar() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciat a " + HOST + ":" + PORT);

        while (!sortir) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connectat: " + clientSocket.getInetAddress());
            GestorClients gestor = new GestorClients(clientSocket, this);
            gestor.start();
        }
        pararServidor();
    }

    public void pararServidor() throws IOException {
        if (serverSocket != null) serverSocket.close();
    }

    public synchronized void finalitzarXat() {
        enviarMissatgeGrup(MSG_SORTIR);
        clients.clear();
        sortir = true;
        System.out.println("DEBUG: multicast sortir");
    }

    public synchronized void afegirClient(GestorClients client) {
        clients.put(client.getNom(), client);
        enviarMissatgeGrup("Entra: " + client.getNom());
        System.out.println("DEBUG: multicast Entra: " + client.getNom());
    }

    public synchronized void eliminarClient(String nom) {
        if (clients.containsKey(nom)) clients.remove(nom);
    }

    public synchronized void enviarMissatgeGrup(String missatge) {
        for (GestorClients c : clients.values()) {
            c.enviarMissatge("Servidor", missatge);
        }
    }

    public synchronized void enviarMissatgePersonal(String destinatari, String remitent, String missatge) {
        GestorClients client = clients.get(destinatari);
        if (client != null) {
            client.enviarMissatge(remitent, missatge);
        }
    }

    public static void main(String[] args) {
        try {
            ServidorXat servidor = new ServidorXat();
            servidor.servidorAEscoltar();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
