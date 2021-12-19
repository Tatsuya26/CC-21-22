import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientFileRequester implements Runnable{
    private InetAddress ip;
    private ArmazemFicheiro af;
    private int window;

    public ClientFileRequester(InetAddress ip,ArmazemFicheiro f) {
        this.ip = ip;
        this.af = f;
        this.window = 1;
    }
    
    public void run() {
        try {
            Security s = new Security();
            DatagramSocket socket = new DatagramSocket();
            RQFileInfoPacket rqPacket = new RQFileInfoPacket();
            byte[] rqBytes = s.addSecurityToPacket(rqPacket.serialize());
            DatagramPacket outPacket = new DatagramPacket(rqBytes,rqBytes.length,ip,80);
            int i = 0;
            int numB = 1;
            socket.setSoTimeout(1000);
            while (i < 5) {
                try {
                    socket.send(outPacket);
                    byte[] indata = new byte[1320];
                    DatagramPacket inPacket = new DatagramPacket(indata, 1320);
                    int atual = 0;
                    int port = 0;
                    List<DataTransferPacket> dtFiles = new ArrayList<>();
                    while (window > atual) {
                        int numBinicial = numB;
                        socket.receive(inPacket);
                        port = inPacket.getPort();
                        boolean authenticity = s.verifyPacketAuthenticity(inPacket.getData());
                        
                        if (authenticity) {
                            byte[] packet = inPacket.getData();
                            ByteArrayInputStream bis = new ByteArrayInputStream(Arrays.copyOfRange(packet,20,packet.length));
                            
                            //Ler o byte que indica o opcode
                            int opcode = bis.read();
                            System.out.println(opcode);
                            // Se opcode == 3 temos um DataTransferPacket logo vamos escrever os dados no ficheiro e enviar o ACK.
                            if (opcode == 3) {
                                atual++;
                                DataTransferPacket data = DataTransferPacket.deserialize(bis);
                                if (numBinicial + window > data.getNumBloco() && numBinicial <= data.getNumBloco()) {
                                    if (dtFiles.get(data.getNumBloco() - numBinicial) == null) { 
                                        dtFiles.add(data.getNumBloco() - numBinicial, data);
                                    }
                                }
                            }
                            // Se opcode == 5 temos um FINPacket. Enviamos um FINPacket de volta e dá mos exit.
                            if (opcode == 5) {
                                FINPacket fin = new FINPacket();
                                window = atual;
                                byte[] packetToSend = s.addSecurityToPacket(fin.serialize());
                                socket.send(new DatagramPacket(packetToSend, packetToSend.length,ip,port));
                                i = 5;
                            }
                        }
                    }
                    if (dtFiles.size() == window) {
                        for (DataTransferPacket dtp : dtFiles) {
                            readFileInfos(dtp);
                            numB++;
                        }
                        ACKPacket ack = new ACKPacket(numB);
                        byte[] outData = s.addSecurityToPacket(ack.serialize());
                        outPacket = new DatagramPacket(outData, outData.length,ip,port);
                        window++;
                    }
                    else {
                        for (int index = 0; index < dtFiles.size();index++) {
                            if (dtFiles.get(index) == null) {
                                numB = numB + index;
                                index = dtFiles.size();
                            }
                            else {
                                readFileInfos(dtFiles.get(index));
                            }
                        }
                        window = 1;
                        ACKPacket ack = new ACKPacket(numB);
                        byte[] outData = s.addSecurityToPacket(ack.serialize());
                        outPacket = new DatagramPacket(outData, outData.length,ip,port);
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
