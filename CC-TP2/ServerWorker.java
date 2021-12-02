import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ServerWorker implements Runnable{
    public DatagramPacket received;
    public DatagramSocket socket;

    public ServerWorker(DatagramPacket received) {
        this.received = received;
        try {
            this.socket = new DatagramSocket();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            int port = this.received.getPort();
            InetAddress clientIP = this.received.getAddress();
            String dados = new String(this.received.getData());
            System.out.println("IP : " + clientIP.toString());
            System.out.println("Port : " + port);
            System.out.println(dados);
            dados = dados.toUpperCase();
            DatagramPacket sendPacket = new DatagramPacket(dados.getBytes(),dados.getBytes().length,clientIP,port);
            socket.send(sendPacket);
            socket.receive(sendPacket);
            String resultado = new String(sendPacket.getData());
            System.out.println(resultado);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
