import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class FTRapidServer {
    public final static int length = 1300;
    public static void main(String[] args) {
        byte[] buffer = new byte[length];
        try {
            DatagramSocket serverSocket = new DatagramSocket(80);
            DatagramPacket receiver = new DatagramPacket(buffer, length);
            serverSocket.receive(receiver);
            Thread executante = new Thread(new ServerWorker(receiver));
            executante.start();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
