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
            //print na consola o que foi recebido(a bunch of 0??)
            //for (Byte b : inPacket.getData()) System.out.print(b);

            //deserialize info from other per
            ByteArrayInputStream bis = new ByteArrayInputStream(inPacket.getData());
            //List<FileInfo> fis = new ArrayList<>();
            //while (bis.read() == '+') {
                FileInfo fi = FileInfo.deserialize(bis);
                System.out.println(fi.toString());
              //  fis.add(fi);
            //}

            //:FIXME : O PACOTE QUE VEM DA SOCKET VEM COM LIXO O QUE NAO PERMITE FAZER BEM O PARSING
            //          Testa assim e se nao der, tenta ver onde os bytes mudam.
            //          O + sinaliza que ainda há ficheiros para serem transferidos

            //for (FileInfo f : fis)   System.out.println(f.toString());
            
            //print na consola para verificar se o que foi enviado está correto
            int port = outPacket.getPort();
            InetAddress ip = outPacket.getAddress();
            String resultado = "Obrigado";
            System.out.println("Agradecer ao IP " + ip.toString() + " na porta " + port);
            DatagramPacket outPacket2 = new DatagramPacket(resultado.getBytes(), resultado.length(),ip,port);
            socket.send(outPacket2);
            socket.close();
        }
        catch (IOException e) {
             e.printStackTrace();
        }
    }
}
