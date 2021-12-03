import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

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
            DatagramPacket outPacket = new DatagramPacket(line.getBytes(), line.length(),this.ips[0],80);
            socket.setSoTimeout(5000);
            int i  = 0;
            while(i < 5) {
                try {
                    socket.send(outPacket);
                    socket.receive(outPacket);
                    i = 5;
                } catch(SocketTimeoutException e) {
                    i++;
                }
            }            
            socket.receive(outPacket);
            String resultado = new String(outPacket.getData());
            int port = outPacket.getPort();
            InetAddress ip = outPacket.getAddress();
            System.out.println("port" + port);
            System.out.println("ip" + ip);
            outPacket = new DatagramPacket(resultado.getBytes(), resultado.length(),ip,port);
            socket.send(outPacket);
            socket.close();
        }
        catch (IOException e) {
             e.printStackTrace();
        }
    }
}
