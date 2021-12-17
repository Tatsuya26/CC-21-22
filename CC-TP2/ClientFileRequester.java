import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class ClientFileRequester implements Runnable{
    private InetAddress ip;
    private ArmazemFicheiro af;

    public ClientFileRequester(InetAddress ip,ArmazemFicheiro f) {
        this.ip = ip;
        this.af = f;
    }
    
    public void run() {
        try {
            Security s = new Security();
            DatagramSocket socket = new DatagramSocket();
            RQFileInfoPacket rqPacket = new RQFileInfoPacket();
            byte[] rqBytes = s.addSecurityToPacket(rqPacket.serialize());
            DatagramPacket outPacket = new DatagramPacket(rqBytes,rqBytes.length,ip,80);
            int i = 0;
            socket.setSoTimeout(1000);
            while (i < 5) {
                try {
                    socket.send(outPacket);
                    byte[] indata = new byte[1320];
                    DatagramPacket inPacket = new DatagramPacket(indata,1320);
                    socket.receive(inPacket);
                    int port = inPacket.getPort();

                    boolean authenticity = s.verifyPacketAuthenticity(inPacket.getData());
                    if (authenticity) {
                        byte[] packet = inPacket.getData();
                        ByteArrayInputStream bis = new ByteArrayInputStream(Arrays.copyOfRange(packet,21,packet.length));
                        int opcode = bis.read();
                        System.out.println(opcode);
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
