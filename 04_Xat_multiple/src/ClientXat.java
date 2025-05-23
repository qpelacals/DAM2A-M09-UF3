import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Scanner;

public class ClientXat implements Serializable {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean sortir = false;

    public void connecta() throws IOException {
        socket = new Socket(ServidorXat.HOST, ServidorXat.PORT);
        out = new ObjectOutputStream(socket.getOutputStream());
        System.out.println("Client connectat a " + ServidorXat.HOST + ":" + ServidorXat.PORT);
        System.out.println("Flux d'entrada i sortida creat.");
    }

    public void tancarClient() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            System.out.println("Tancant client...");
        } catch (IOException e) {
            System.out.println("Error al tancar: " + e.getMessage());
        }
    }

    public void ajuda() {
        System.out.println("---------------------");
        System.out.println("Comandes disponibles:");
        System.out.println("  1.- Conectar al servidor (primer pass obligatori)");
        System.out.println("  2.- Enviar missatge personal");
        System.out.println("  3.- Enviar missatge al grup");
        System.out.println("  4.- (o línia en blanc)-> Sortir del client");
        System.out.println("  5.- Finalitzar tothom");
        System.out.println("---------------------");
    }

    public void executa() {
        new Thread(() -> {
            try {
                in = new ObjectInputStream(socket.getInputStream());
                while (!sortir) {
                    String missatgeRaw = (String) in.readObject();
                    String codi = Missatge.getCodiMissatge(missatgeRaw);
                    String[] parts = Missatge.getPartsMissatge(missatgeRaw);

                    if (codi == null) continue;

                    switch (codi) {
                        case Missatge.CODI_SORTIR_TOTS:
                            sortir = true;
                            System.out.println("Tancant tots els clients.");
                            break;
                        case Missatge.CODI_MSG_PERSONAL:
                            System.out.println("Missatge de (" + parts[1] + "): " + parts[2]);
                            break;
                        case Missatge.CODI_MSG_GRUP:
                            System.out.println("[GRUP] " + parts[1]);
                            break;
                        default:
                            System.out.println("Codi desconegut rebut: " + codi);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                if (!sortir) System.out.println("Error rebent missatge. Sortint...");
            } finally {
                tancarClient();
            }
        }).start();
    }

    public static void main(String[] args) {
        ClientXat client = new ClientXat();
        try {
            client.connecta();
            client.executa();
            client.ajuda();
            Scanner scanner = new Scanner(System.in);

            while (!client.sortir) {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    client.sortir = true;
                    client.out.writeObject(Missatge.getMissatgeSortirClient("Adéu"));
                    break;
                }

                switch (input) {
                    case "1":
                        System.out.print("Introdueix el nom: ");
                        String nom = scanner.nextLine();
                        client.out.writeObject(Missatge.getMissatgeConectar(nom));
                        break;
                    case "2":
                        System.out.print("Destinatari: ");
                        String dest = scanner.nextLine();
                        System.out.print("Missatge a enviar: ");
                        String msg = scanner.nextLine();
                        client.out.writeObject(Missatge.getMissatgePersonal(dest, msg));
                        break;
                    case "3":
                        System.out.print("Missatge al grup: ");
                        String msgGrup = scanner.nextLine();
                        client.out.writeObject(Missatge.getMissatgeGrup(msgGrup));
                        break;
                    case "5":
                        client.out.writeObject(Missatge.getMissatgeSortirTots("Adéu"));
                        client.sortir = true;
                        break;
                    default:
                        System.out.println("Opció no vàlida");
                }
                client.ajuda();
            }
        } catch (IOException e) {
            System.out.println("Error de connexió: " + e.getMessage());
        } finally {
            client.tancarClient();
        }
    }
}