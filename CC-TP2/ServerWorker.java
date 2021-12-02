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
            String resposta = "Conexão estabelecida ao endereço " + clientIP.toString();
            DatagramPacket sendPacket = new DatagramPacket(resposta.getBytes(),resposta.getBytes().length,clientIP,port);
            socket.send(sendPacket);
            socket.receive(sendPacket);
            System.out.println(new String(sendPacket.getData()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
