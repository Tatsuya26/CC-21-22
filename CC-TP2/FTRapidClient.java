import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FTRapidClient {
    public final static int length = 1300;
    public static void main(String[] args) {
        try{
            DatagramSocket socket = new DatagramSocket();

            DatagramPacket outPacket = new DatagramPacket(line.getBytes(), line.length(),InetAddress.getLocalHost(),80);
            socket.send(outPacket);

            socket.receive(outPacket);
            String resultado = new String(outPacket.getData());
            System.out.println(resultado);
            int port = outPacket.getPort();
            InetAddress ip = outPacket.getAddress();
            outPacket = new DatagramPacket(resultado.getBytes(), resultado.length(),ip,port);
            socket.send(outPacket);
        }
        catch (IOException e) {
             e.printStackTrace();
        }
    }
}
