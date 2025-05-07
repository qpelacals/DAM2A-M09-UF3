import java.io.*;
import java.net.*;

public class Servidor {
    private static final int PORT = 9999;
    private static final String HOST = "localhost";
    private ServerSocket serverSocket;

    public Socket connectar() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Acceptant connexions en -> " + HOST + ":" + PORT);
        System.out.println("Esperant connexio...");
        Socket socket = serverSocket.accept();
        System.out.println("Connexio acceptada: " + socket.getRemoteSocketAddress());
        return socket;
    }

    public void tancarConnexio(Socket socket) throws IOException {
        if (socket != null) socket.close();
        if (serverSocket != null) serverSocket.close();
        System.out.println("Connexio tancada.");
    }

    public void enviarFitxers(Socket socket) throws IOException, ClassNotFoundException {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        System.out.println("Esperant el nom del fitxer del client...");
        String nomFitxer = (String) in.readObject();
        System.out.println("NomFitxer rebut: " + nomFitxer);

        if (nomFitxer == null || nomFitxer.trim().equals("")) {
            System.out.println("Nom del fitxer buit o nul. Sortint...");
            return;
        }

        Fitxer fitxer = new Fitxer(nomFitxer);
        byte[] contingut;
        try {
            contingut = fitxer.getContingut();
            System.out.println("Contingut del fitxer a enviar: " + contingut.length + " bytes");
            out.writeObject(contingut);
            System.out.println("Fitxer enviat al client: " + nomFitxer);
        } catch (IOException e) {
            System.out.println("Error llegint el fitxer del client: " + e.getMessage());
            out.writeObject(null);
        }
    }

    public static void main(String[] args) {
        try {
            Servidor s = new Servidor();
            Socket socket = s.connectar();
            s.enviarFitxers(socket);
            s.tancarConnexio(socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}