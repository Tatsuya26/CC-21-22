import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
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
                    int port = inPacket.getPort();
                    InetAddress ip = inPacket.getAddress();
                    ByteArrayInputStream bis = new ByteArrayInputStream(inPacket.getData());
                    if (bis.read() == 3) {
                        DataTransferPacket data = DataTransferPacket.deserialize(bis);
                        List<FileInfo> fis = readFileInfos(data);
                        getFiles(fis,ip,port,socket);
                        FINPacket fin = new FINPacket();
                        outPacket = new DatagramPacket(fin.serialize(),fin.serialize().length,ip,port);
                        socket.send(outPacket);
                    }
                    if (bis.read() == 5) {
                        System.out.println("Recebido pedido de fim de conexão");
                        FINPacket fin = new FINPacket();
                        outPacket = new DatagramPacket(fin.serialize(), 1,ip,port);
                        socket.send(outPacket);
                        i = 25;
                    }
                }
                catch (SocketTimeoutException e) {
                    i++;
                }
            }
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

    public void getFiles(List<FileInfo> fis, InetAddress ip,int port,DatagramSocket socket) {
        try {
            for (FileInfo f: fis) {
                String filename = f.getName();
                ReadFilePacket readFile = new ReadFilePacket(filename);
                DatagramPacket outPacket = new DatagramPacket(readFile.serialize(), readFile.serialize().length,ip,port);
                byte[] indata = new byte[1300];
                DatagramPacket inPacket = new DatagramPacket(indata, 1300);
                socket.setSoTimeout(1000);
                int i = 0;
                File ficheiro = new File(filename);
                if (!ficheiro.exists()) ficheiro.createNewFile();
                FileOutputStream fos = new FileOutputStream(ficheiro,false);
                while (i < 25) {
                    socket.send(outPacket);
                    socket.receive(inPacket);
                    ByteArrayInputStream bis = new ByteArrayInputStream(inPacket.getData());
                    if (bis.read() == 3) {
                        DataTransferPacket data = DataTransferPacket.deserialize(bis);
                        ACKPacket ack = new ACKPacket(data.getNumBloco());
                        fos.write(data.getData(),0,data.getLengthData());
                        outPacket = new DatagramPacket(ack.serialize(),ack.serialize().length,ip,port);
                    }
                    if (bis.read() == 5) {
                        i = 25;
                        FINPacket fin = new FINPacket();
                        outPacket = new DatagramPacket(fin.serialize(),fin.serialize().length,ip,port);
                        //socket.send(outPacket);
                    }
                }
                fos.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
