import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FTRapidServer implements Runnable{
    public final static int length = 1300;
    public File folder;
    public InetAddress[] ips;

    public FTRapidServer(File folder,InetAddress[] ips) {
        this.folder = folder;
        this.ips = ips;
    }
    
    public void run() {
        byte[] buffer = new byte[length];
        try {
            DatagramSocket serverSocket = new DatagramSocket(80);
            DatagramPacket receiver = new DatagramPacket(buffer, length);
            serverSocket.receive(receiver);
            Thread executante = new Thread(new ServerWorker(receiver,folder,ips));
            executante.start();
            serverSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
