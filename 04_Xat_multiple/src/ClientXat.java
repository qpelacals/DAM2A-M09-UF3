import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientXat {

    private static final String HOST = "localhost";
    private static final int PORT = 9999;

    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean sortir = false;

    public void connecta() throws IOException {
        try {
            socket = new Socket(ServidorXat.HOST, ServidorXat.PORT);
            //ois = new ObjectInputStream(socket.getInputStream()); // PRIMER input
            oos = new ObjectOutputStream(socket.getOutputStream()); // DESPRÉS output
            System.out.println("Client connectat a " + ServidorXat.HOST + ":" + ServidorXat.PORT);
            System.out.println("Flux d'entrada i sortida creat.");
        } catch (IOException e) {
            System.err.println("Error connectant al servidor: " + e.getMessage());
        }
    }

    public void enviarMissatge(String missatge) throws IOException {
        try {
            if (oos != null) {
                oos.writeObject(missatge);
                oos.flush();
                System.out.println("Enviant missatge: " + missatge);
            }
        } catch (IOException e) {
            System.err.println("Error enviant missatge: " + e.getMessage());
        }
    }

    public void tancarClient() {
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (socket != null) socket.close();

            System.out.println("Tancant client...");
        } catch (IOException e) {
            System.err.println("Error tancant el client: " + e.getMessage());
        }
    }

    public void llegir() {
        //new Thread(() -> {
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            System.out.println("DEBUG: Iniciant rebuda de missatges...");
            while (!sortir) {
                String missatge = (String) ois.readObject();
                String codi = Missatge.getCodiMissatge(missatge);
                String[] parts = Missatge.getPartsMissatge(missatge);

                if (codi == null) {
                    System.out.println("ERROR: Missatge null.");
                    continue;
                }

                switch (codi) {
                    case Missatge.CODI_SORTIR_TOTS:
                        sortir = true;
                        System.out.println("DEBUG: Tancant client...");
                        break;
                    case Missatge.CODI_MSG_PERSONAL:
                        if (parts.length >= 3) {
                            System.out.println("Missatge de (" + parts[1] + "): " + parts[2]);
                        } else {
                            System.out.println("ERROR: Missatge personal mal format.");
                        }
                        break;
                    case Missatge.CODI_MSG_GRUP:
                        if (parts.length >= 2) {
                            System.out.println("Missatge de grup: " + parts[1]);
                            break;
                        } else {
                            System.out.println("ERROR: Missatge de grup mal format.");
                        }
                    default:
                        System.out.println("ERROR: codi desconegut");
                }
            }
        } catch (Exception e) {
            System.out.println("Error rebent missatge. Sortint...");
            e.printStackTrace();
        } finally {
            tancarClient();
        }
        //}).start();
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

    public String getLinea(Scanner sc, String missatge, boolean obligatori) {
        System.out.print(missatge);
        String linia = sc.nextLine();
        while (obligatori && linia.trim().isEmpty()) {
            System.out.print(missatge);
            linia = sc.nextLine();
        }
        return linia.trim();
    }

    public static void main(String[] args) {
        ClientXat client = new ClientXat();
        Scanner sc = new Scanner(System.in);

        try {
            client.connecta();

            Thread filLlegir = new Thread(() -> {
                try {
                    client.llegir();
                } catch (Exception e) {
                    System.err.println("Error en el fil de lectura: " + e.getMessage());
                }
            });
            filLlegir.start();

            //client.llegir();
            boolean sortir = false;
            client.ajuda();

            while (!sortir) {
                String opcio = sc.nextLine().trim();
                switch (opcio) {
                    case "1":
                        String nom = client.getLinea(sc, "Introdueix el nom: ", true);
                        String missatgeConectar = Missatge.getMissatgeConectar(nom);
                        client.enviarMissatge(missatgeConectar);
                        break;
                    case "2":
                        String dest = client.getLinea(sc, "Destinatari:: ", true);
                        String msg = client.getLinea(sc, "Missatge a enviar: ", true);
                        client.enviarMissatge(Missatge.getMissatgePersonal(dest, msg));
                        break;
                    case "3":
                        String grup = client.getLinea(sc, "Missatge per tothom: ", true);
                        client.enviarMissatge(Missatge.getMissatgeGrup(grup));
                        break;
                    case "4":
                        sortir = true;
                        client.enviarMissatge(Missatge.getMissatgeSortirClient("Adéu"));
                        break;
                    case "5":
                        sortir = true;
                        client.enviarMissatge(Missatge.getMissatgeSortirTots("Adéu"));
                        break;
                    default:
                        if (opcio.trim().isEmpty()) {
                            sortir = true;
                            client.enviarMissatge(Missatge.getMissatgeSortirClient("Adéu"));
                        } else {
                            client.ajuda();
                        }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            client.tancarClient();
        }
    }
}