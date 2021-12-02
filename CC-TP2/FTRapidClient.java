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
            int port = socket.getPort();
            System.out.println(port);
            String line = "Tiagos David";
            DatagramPacket outPacket = new DatagramPacket(line.getBytes(), line.length(),ips[0],80);
            socket.setSoTimeout(5000);
            int i = 0;
            while (i < 5){
                try {
                    socket.send(outPacket);
                    System.out.println("Mensagem enviada para o endereço "+ ips[0].toString());
                    socket.receive(outPacket);
                    System.out.println("Mensagem recebida em " + InetAddress.getLocalHost().toString());
                    i = 5;
                }
                catch (SocketTimeoutException e) {
                    i++;
                }
            }
            String resultado = new String(outPacket.getData());
            port = outPacket.getPort();
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
