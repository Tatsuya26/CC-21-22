import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class ClientFileRequester implements Runnable{
    private InetAddress ip;
    private ArmazemFicheiro af;
    private String fileToSync;

    public ClientFileRequester(InetAddress ip,ArmazemFicheiro f,String folder) {
        this.ip = ip;
        this.af = f;
        this.fileToSync = folder;
    }

    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            RQFileInfoPacket rqPacket = new RQFileInfoPacket(this.fileToSync);
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

                    Security s = new Security();
                    boolean authenticity = s.verifyPacketAuthenticity(inPacket.getData());

                    byte[] packet = inPacket.getData();
                    ByteArrayInputStream bis = new ByteArrayInputStream(Arrays.copyOfRange(packet,20,packet.length));
                    int opcode = bis.read();
                    if (opcode == 3) {
                        DataTransferPacket data = DataTransferPacket.deserialize(bis);
                        readFileInfos(data);
                        ACKPacket ack = new ACKPacket(data.getNumBloco());
                        byte[] packetToSend = s.addSecurityToPacket(ack.serialize());
                        outPacket = new DatagramPacket(packetToSend,packetToSend.length,ip,port);
                    }
                    if (opcode == 5) {
                        i = 5;
                        FINPacket finPacket = new FINPacket();
                        byte[] packetToSend = s.addSecurityToPacket(finPacket.serialize());
                        outPacket = new DatagramPacket(packetToSend,packetToSend.length,ip,port);
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
            fi.setIP(ip);
            this.af.adicionaFileInfo(fi);
        }
    }
}
