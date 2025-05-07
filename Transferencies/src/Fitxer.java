import java.io.*;

public class Fitxer {
    private String nom;
    private byte[] contingut;

    public Fitxer(String nom) {
        this.nom = nom;
    }

    public byte[] getContingut() throws IOException {
        File file = new File(nom);
        if (!file.exists()) throw new FileNotFoundException("Fitxer no trobat: " + nom);

        FileInputStream fis = new FileInputStream(file);
        contingut = fis.readAllBytes();
        fis.close();
        return contingut;
    }
}