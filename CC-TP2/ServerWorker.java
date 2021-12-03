import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerWorker implements Runnable{
    public DatagramPacket received;
    public DatagramSocket socket;
    public File folder;
    public InetAddress[] ips;

    public ServerWorker(DatagramPacket received,File folder,InetAddress[] ips) {
        this.received = received;
        this.folder = folder;
        this.ips = ips;
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
            File[] subFicheiros = folder.listFiles();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            for (File f  : subFicheiros) {
                BasicFileAttributes fa = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
                FileInfo fi = new FileInfo(f.getName(),Long.toString(fa.lastModifiedTime().toMillis()),Long.toString(fa.size()));
                bos.write(fi.serialize());
            }
            byte[] data = bos.toByteArray();
            DatagramPacket sendPacket = new DatagramPacket(data,data.length,clientIP,port);
            System.out.println("Server a enviar pacote para o IP " + clientIP.toString() + " para a porta " + port);
            for (byte b : data) {
                System.out.print(b);
            }
            socket.send(sendPacket);
            socket.receive(this.received);
            System.out.println(new String(this.received.getData()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
