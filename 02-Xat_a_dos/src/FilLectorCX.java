import java.io.*;

public class FilLectorCX extends Thread {
    private ObjectInputStream in;

    public FilLectorCX(ObjectInputStream in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            String missatge;
            while ((missatge = (String) in.readObject()) != null) {
                if (missatge.equals("sortir")) break;
                System.out.println("Rebut: " + missatge);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Lectura tancada.");
        }
    }
}