import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
            byte[] buffer = new byte[length];
            DatagramSocket serverSocket = new DatagramSocket(80);
            while (true) {
                DatagramPacket receiver = new DatagramPacket(buffer, length);
                serverSocket.receive(receiver);
                Thread executante = new Thread(new ServerWorker(receiver,folder,ips));
                executante.start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
