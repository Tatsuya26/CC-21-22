import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.file.Path;

public class ClientFileGetter implements Runnable{
    public InetAddress ip;
    public FileInfo fi;
    public File folder;

    public ClientFileGetter(InetAddress ip,FileInfo fi,File f) {
        this.ip = ip;
        this.fi = fi;
        this.folder = f;
    }

    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            String filename = fi.getName();
            System.out.println("A pedir o ficheiro " + filename);
            ReadFilePacket readFile = new ReadFilePacket(filename);
            DatagramPacket outPacket = new DatagramPacket(readFile.serialize(), readFile.serialize().length,ip,80);
            int i = 0;
            Path file = Path.of(filename);
            Path parent = file.getParent().getParent();
            file = parent.relativize(file);
            Path path = Path.of(folder.getAbsolutePath()).getParent();
            file = path.resolve(file);
            File parentFile = parent.toFile();
            if (!parentFile.exists()) parentFile.mkdir();
            File ficheiro = file.toFile();
            if (!ficheiro.exists()) ficheiro.createNewFile();
            FileOutputStream fos = new FileOutputStream(ficheiro,false);
            socket.setSoTimeout(1000);
            while (i < 25) {
                try {
                    socket.send(outPacket);
                    byte[] indata = new byte[1300];
                    DatagramPacket inPacket = new DatagramPacket(indata, 1300);
                    socket.receive(inPacket);
                    int port = inPacket.getPort();
                    ByteArrayInputStream bis = new ByteArrayInputStream(inPacket.getData());
                    int opcode = bis.read();
                    if (opcode == 3) {
                        DataTransferPacket data = DataTransferPacket.deserialize(bis);
                        ACKPacket ack = new ACKPacket(data.getNumBloco());
                        fos.write(data.getData(),0,data.getLengthData());
                        System.out.println("Enviar ACK ao bloco " + ack.getNumBloco());
                        outPacket = new DatagramPacket(ack.serialize(),ack.serialize().length,ip,port);
                    }
                    if (opcode == 5) {
                        FINPacket fin = new FINPacket();
                        socket.send(new DatagramPacket(fin.serialize(), fin.serialize().length,ip,port));
                        i = 25;
                    }
                }
                catch (SocketTimeoutException e) {
                    i++;
                }
            fos.close();
            socket.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
