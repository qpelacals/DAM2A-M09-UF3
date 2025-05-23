import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GestorClients implements Runnable {
    private Socket client;
    ObjectOutputStream out;
    private ObjectInputStream in;
    private ServidorXat servidor;
    private String nom;
    private boolean sortir = false;

    public GestorClients(Socket socket, ServidorXat servidor) throws IOException {
        this.client = socket;
        this.servidor = servidor;
        out = new ObjectOutputStream(client.getOutputStream());
        in = new ObjectInputStream(client.getInputStream());
    }

    public String getNom() { return nom; }

    public void run() {
        try {
            while (!sortir) {
                String missatgeRaw = (String) in.readObject();
                processaMissatge(missatgeRaw);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (!sortir) System.out.println("Error de connexió amb " + nom + ": " + e.getMessage());
        } finally {
            try { client.close(); } catch (IOException e) { /* Ignored */ }
            servidor.eliminarClient(nom);
        }
    }

    public void enviarMissatge(String remitent, String missatge) {
        try {
            out.writeObject(Missatge.getMissatgePersonal(remitent, missatge));
        } catch (IOException e) {
            System.out.println("Error enviant a " + nom + ": " + e.getMessage());
        }
    }

    private void processaMissatge(String missatgeRaw) {
        String codi = Missatge.getCodiMissatge(missatgeRaw);
        String[] parts = Missatge.getPartsMissatge(missatgeRaw);

        if (codi == null || parts == null) return;

        switch (codi) {
            case Missatge.CODI_CONECTAR:
                nom = parts[1];
                servidor.afegirClient(this);
                break;
            case Missatge.CODI_SORTIR_CLIENT:
                sortir = true;
                servidor.eliminarClient(nom);
                break;
            case Missatge.CODI_MSG_GRUP:
                servidor.enviarMissatgeGrup(parts[1]);
                break;
            case Missatge.CODI_SORTIR_TOTS:
                servidor.finalitzarXat();
                break;
            case Missatge.CODI_MSG_PERSONAL:
                servidor.enviarMissatgePersonal(parts[1], nom, parts[2]);
                break;
            default:
                System.out.println("Codi desconegut: " + codi);
        }
    }
}