import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FTRapidServer implements Runnable{
    public final static int length = 1320;
    public File folder;
    public InetAddress[] ips;

    public FTRapidServer(File folder,InetAddress[] ips) {
        this.folder = folder;
        this.ips = ips;
    }
    
    public void run() {
        try {
            DatagramSocket serverSocket = new DatagramSocket(8080);
            while (true) {
                byte[] buffer = new byte[length];
                DatagramPacket receiver = new DatagramPacket(buffer, length);
                serverSocket.receive(receiver);
                Thread executante = new Thread(new ServerWorker(receiver,folder));
                executante.start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
