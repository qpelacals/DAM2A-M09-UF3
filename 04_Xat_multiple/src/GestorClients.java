import java.io.*;
import java.net.Socket;

public class GestorClients extends Thread {
    private Socket client;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private ServidorXat servidor;
    private String nom;
    private boolean sortir = false;

    public GestorClients(Socket client, ServidorXat servidor) {
        this.client = client;
        this.servidor = servidor;
        try {
            oos = new ObjectOutputStream(client.getOutputStream());
        } catch (IOException e) {
            oos = null;
        }
    }

    public String getNom() {
        return nom;
    }

    public void enviarMissatge(String remitent, String missatge) {
        try {
            if (oos != null) oos.writeObject("Missatge de (" + remitent + "): " + missatge);
        } catch (IOException e) {
            sortir = true;
        }
    }

    public void run() {
        try {
            ois = new ObjectInputStream(client.getInputStream());
            while (!sortir) {
                String missatge = (String) ois.readObject();
                processaMissatge(missatge);
            }
        } catch (Exception e) {
            System.out.println("Error rebent missatge. Sortint...");
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void processaMissatge(String missatge) {
        String codi = Missatge.getCodiMissatge(missatge);
        String[] parts = Missatge.getPartsMissatge(missatge);

        switch (codi) {
            case Missatge.CODI_CONECTAR:
                nom = parts[1];
                servidor.afegirClient(this);
                break;
            case Missatge.CODI_SORTIR_CLIENT:
                sortir = true;
                servidor.eliminarClient(nom);
                break;
            case Missatge.CODI_SORTIR_TOTS:
                sortir = true;
                servidor.finalitzarXat();
                break;
            case Missatge.CODI_MSG_PERSONAL:
                servidor.enviarMissatgePersonal(parts[1], nom, parts[2]);
                break;
            default:
                System.out.println("ERROR: codi desconegut");
        }
    }
}