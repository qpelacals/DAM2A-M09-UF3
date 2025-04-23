import java.io.*;

public class FilServidorXat extends Thread {
    private ObjectInputStream in;

    public FilServidorXat(ObjectInputStream in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            String missatge;
            while ((missatge = (String) in.readObject()) != null) {
                if (missatge.equals(ServidorXat.MSG_SORTIR)) break;
                System.out.println("Rebut: " + missatge);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error en la lectura del client.");
        } finally {
            System.out.println("Fil de xat finalitzat.");
        }
    }
}