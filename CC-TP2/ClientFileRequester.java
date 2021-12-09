import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.file.Path;

public class ClientFileRequester implements Runnable{
    private InetAddress ip;
    private ArmazemFicheiro af;
    private File folder;

    public ClientFileRequester(InetAddress ip,ArmazemFicheiro f,File ficheiro) {
        this.ip = ip;
        this.af = f;
        this.folder = ficheiro;
    }

    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            RQFileInfoPacket rqPacket = new RQFileInfoPacket();
            DatagramPacket outPacket = new DatagramPacket(rqPacket.serialize(),rqPacket.serialize().length,ip,80);
            int i = 0;
            socket.setSoTimeout(1000);
            while (i < 5) {
                try {
                    socket.send(outPacket);
                    byte[] indata = new byte[1300];
                    DatagramPacket inPacket = new DatagramPacket(indata,1300);
                    socket.receive(inPacket);
                    int port = inPacket.getPort();
                    ByteArrayInputStream bis = new ByteArrayInputStream(inPacket.getData());
                    int opcode = bis.read();
                    if (opcode == 3) {
                        DataTransferPacket data = DataTransferPacket.deserialize(bis);
                        readFileInfos(data);
                        ACKPacket ack = new ACKPacket(data.getNumBloco());
                        outPacket = new DatagramPacket(ack.serialize(),ack.serialize().length,ip,port);
                    }
                    if (opcode == 5) {
                        i = 5;
                        FINPacket finPacket = new FINPacket();
                        outPacket = new DatagramPacket(finPacket.serialize(),finPacket.serialize().length,ip,port);
                        socket.send(outPacket);
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

    public void readFileInfos (DataTransferPacket data) throws IOException{
        ByteArrayInputStream bis = new ByteArrayInputStream(data.getData());
        while (bis.read() != 0) {
            FileInfo fi = FileInfo.deserialize(bis);
            String filename = fi.getName();
            Path file = Path.of(filename);
            Path parent = file.getParent().getParent();
            file = parent.relativize(file);
            Path path = folder.toPath().getParent();
            System.out.println("File "+ file.toString());
            System.out.println("Path "+ path.toString());
            file = path.resolve(file);
            fi.setFileName(file.toString());
            fi.setIP(ip);
            this.af.adicionaFileInfo(fi);
        }
    }
}
