import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

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
                bos.write('+');
                bos.write(fi.serialize());
            }
            bos.write(0);
            byte[] data = bos.toByteArray();
            DatagramPacket sendPacket = new DatagramPacket(data,data.length,clientIP,port);
            System.out.println("Server a enviar pacote para o IP " + clientIP.toString() + " para a porta " + port);
            
            byte[] indata = new byte[1300];
            this.received = new DatagramPacket(indata, 1300);
            socket.setSoTimeout(1000);
            int i = 0;
            while (i < 25){
                try {
                    socket.send(sendPacket);
                    socket.receive(this.received);
                    i = 25;
                }
                catch (SocketTimeoutException e) {
                    i++;
                }
            }
            System.out.println("Server here!");
            System.out.println(new String(this.received.getData()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
