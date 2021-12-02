import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
            DatagramSocket socket = new DatagramSocket();
            String line = "Pedido dos ficheiros";
            DatagramPacket outPacket = new DatagramPacket(line.getBytes(), line.length(),ips[0],80);
            socket.setSoTimeout(5000);
            int i = 0;
            while (i < 5){
                try {
                    socket.send(outPacket);
                    socket.receive(outPacket);
                    i = 5;
                }
                catch (SocketTimeoutException e) {
                    i++;
                }
            }
            byte[] data = outPacket.getData();
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            List<FileInfo> fis = new ArrayList<>();
            while (bis.available() > 0) {
                FileInfo fi = FileInfo.deserialize(bis);
                fis.add(fi);
            }

            for (FileInfo f : fis) 
                System.out.println(f.toString());
                
            int port = outPacket.getPort();
            InetAddress ip = outPacket.getAddress();
            String resultado = "Obrigado";
            outPacket = new DatagramPacket(resultado.getBytes(), resultado.length(),ip,port);
            socket.send(outPacket);
            socket.close();
        }
        catch (IOException e) {
             e.printStackTrace();
        }
    }
}
