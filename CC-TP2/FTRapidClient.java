import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

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
            byte[] data = new byte[1300];
            DatagramSocket socket = new DatagramSocket();
            String line = "Pedido dos ficheiros";
            DatagramPacket outPacket = new DatagramPacket(line.getBytes(), line.length(),ips[0],80);
            DatagramPacket inPacket = new DatagramPacket(data, 1300);
            socket.setSoTimeout(5000);
            int i = 0;
            while (i < 5){
                try {
                    socket.send(outPacket);
                    socket.receive(inPacket);
                    i = 5;
                }
                catch (SocketTimeoutException e) {
                    i++;
                }
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(inPacket.getData());
            List<FileInfo> fis = new ArrayList<>();
            while (bis.available() > 0) {
                FileInfo fi = FileInfo.deserialize(bis);
                fis.add(fi);
                System.out.println(bis.available());
            }

            for (FileInfo f : fis) 
                System.out.println(f.toString());
                
            int port = outPacket.getPort();
            InetAddress ip = outPacket.getAddress();
            String resultado = "Obrigado";
            System.out.println("Agradecer ao IP " + ip.toString() + " na porta " + port);
            outPacket = new DatagramPacket(resultado.getBytes(), resultado.length(),ip,port);
            socket.send(outPacket);
            socket.close();
        }
        catch (IOException e) {
             e.printStackTrace();
        }
    }
}
