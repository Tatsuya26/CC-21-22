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
            String line = "Pedido conexão para o endereço " + ips[0].toString();
            DatagramPacket outPacket = new DatagramPacket(line.getBytes(), line.length(),ips[0],80);
            socket.setSoTimeout(5000);
            int i = 0;
            while (i < 5){
                try {
                    socket.send(outPacket);
                    System.out.println("Pedido conexão para o endereço " + ips[0].toString());
                    socket.receive(outPacket);
                    int port = socket.getPort();
                    System.out.println("Mensagem recebida na porta " + port);
                    i = 5;
                }
                catch (SocketTimeoutException e) {
                    i++;
                }
            }
            String resultado = new String(outPacket.getData());
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
