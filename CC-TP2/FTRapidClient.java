import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FTRapidClient implements Runnable{
    public final static int length = 1300;
    public File folder;
    public InetAddress[] ips;

    public FTRapidClient(File folder,InetAddress[] ips) {
        this.folder = folder;
        this.ips = ips;
    }

    public void run() {
        try{
            DatagramSocket socket = new DatagramSocket();
            String line = "Tiagos David";
            DatagramPacket outPacket = new DatagramPacket(line.getBytes(), line.length(),InetAddress.getLocalHost(),80);
            socket.send(outPacket);

            socket.receive(outPacket);
            String resultado = new String(outPacket.getData());
            System.out.println(resultado);
            int port = outPacket.getPort();
            InetAddress ip = outPacket.getAddress();
            outPacket = new DatagramPacket(resultado.getBytes(), resultado.length(),ip,port);
            socket.send(outPacket);
            socket.close();
        }
        catch (IOException e) {
             e.printStackTrace();
        }
    }
}
