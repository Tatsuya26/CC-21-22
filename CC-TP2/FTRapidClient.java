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
            byte[] indata = new byte[1300];
            byte[] outdata = new byte[1300];
            DatagramSocket socket = new DatagramSocket();
            outdata = new String("Pedido de ficheiros").getBytes();
            DatagramPacket outPacket = new DatagramPacket(outdata, outdata.length,ips[0],80);
            DatagramPacket inPacket = new DatagramPacket(indata, 1300);
            
            //timeout até haver conexão
            socket.setSoTimeout(1000);
            int i = 0;
            while (i < 25){
                try {
                    socket.send(outPacket);
                    socket.receive(inPacket);
                    i = 25;
                }
                catch (SocketTimeoutException e) {
                    i++;
                }
            }

            ByteArrayInputStream bis = new ByteArrayInputStream(inPacket.getData());
            List<FileInfo> fis = new ArrayList<>();
            while (bis.read() != 0) {
                FileInfo fi = FileInfo.deserialize(bis);
                fis.add(fi);
            }

            //print na consola para verificar se o que foi enviado está correto
            for (FileInfo f : fis)   System.out.println(f.toString());
            
            int port = inPacket.getPort();
            InetAddress ip = inPacket.getAddress();
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
