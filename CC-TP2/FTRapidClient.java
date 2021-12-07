import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.crypto.Data;

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
            byte[] outdata;
            DatagramSocket socket = new DatagramSocket();
            RQFileInfoPacket request = new RQFileInfoPacket();
            outdata = request.serialize();
            DatagramPacket outPacket = new DatagramPacket(outdata, outdata.length,ips[0],80);
            DatagramPacket inPacket = new DatagramPacket(indata, 1300);
            
            //timeout até haver conexão
            socket.setSoTimeout(1000);
            int i = 0;
            // Esperamos 25 segundos até obter resposta. Devemos encontrar um tempo ótimo.
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
            if (bis.read() == 3) {
                DataTransferPacket data = DataTransferPacket.deserialize(bis);
                List<FileInfo> fis = readFileInfos(data);
                for (FileInfo f : fis) System.out.println(f.toString());
            }
            
            int port = inPacket.getPort();
            InetAddress ip = inPacket.getAddress();
            String resultado = "Ficheiros recebidos com sucesso";
            outPacket = new DatagramPacket(resultado.getBytes(), resultado.length(),ip,port);
            socket.send(outPacket);
            socket.close();
        }
        catch (IOException e) {
             e.printStackTrace();
        }
    }

    public List<FileInfo> readFileInfos (DataTransferPacket data) throws IOException{
        //Abrimos stream para leitura do array de bytes vindo do packet;
        ByteArrayInputStream bis = new ByteArrayInputStream(data.getData());
        // Lista onde armazenamos a informação dos ficheiros;
        List<FileInfo> fis = new ArrayList<>();
        //Lemos até ao byte 0;
        while (bis.read() != 0) {
            FileInfo fi = FileInfo.deserialize(bis);
            fis.add(fi);
        }
        return fis;
    }
}
