import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientFileGetter implements Runnable{
    private InetAddress ip;
    private FileInfo fi;
    private File folder;
    private Security s;
    private int window;
    private int port;

    public ClientFileGetter(InetAddress ip,FileInfo fi,File f) {
        this.ip = ip;
        this.fi = fi;
        this.folder = f;
        this.s = new Security();
        this.window = 1;
        this.port = 0;
    }
    
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            String filename = fi.getName();
            //Criar Pacote para pedir o ficheiro fi ao servidor.
            ReadFilePacket readFile = new ReadFilePacket(filename);
            byte[] rfBytes = s.addSecurityToPacket(readFile.serialize());
            DatagramPacket outPacket = new DatagramPacket(rfBytes, rfBytes.length,ip,80);
            int i = 0;
            // Resolver o nome do ficheiro para ficar na diretoria onde estamos a sincronizar. Neste caso a diretoria pai da dada nos parametros
            Path file = Path.of(filename);
            Path path = Path.of(folder.getAbsolutePath());
            file = path.resolve(file);
            Path parent = file.getParent();
            //Verificar se existe a diretoria pai. Se não existir criamos a pasta.
            File parentFile = parent.toFile();
            if (!parentFile.exists()) parentFile.mkdirs();
            //Verificar se o ficheiro já existe. Se não existir criamos o ficheiro.
            File ficheiro = file.toFile();
            if (!ficheiro.exists()) ficheiro.createNewFile();
            //Abrir stream para escrever no ficheiro.
            FileOutputStream fos = new FileOutputStream(ficheiro,false);
            socket.setSoTimeout(1000);
            int numB = 1;
            System.out.println("A pedir o ficheiro " + filename);
            while (i < 5) {
                try {
                    socket.send(outPacket);
                    System.out.println("Enviado ACK com o número " + numB);
                    byte[] indata = new byte[1320];
                    DatagramPacket inPacket = new DatagramPacket(indata, 1320);
                    int atual = 0;
                    List<DataTransferPacket> dtFiles = new ArrayList<>();
                    while (window > atual) {
                        int numBinicial = numB;
                        socket.receive(inPacket);
                        this.port = inPacket.getPort();
                        
                        boolean authenticity = s.verifyPacketAuthenticity(inPacket.getData());
                        
                        if (authenticity) {
                            byte[] packet = inPacket.getData();
                            ByteArrayInputStream bis = new ByteArrayInputStream(Arrays.copyOfRange(packet,20,packet.length));
                            
                            //Ler o byte que indica o opcode
                            int opcode = bis.read();
                            // Se opcode == 3 temos um DataTransferPacket logo vamos escrever os dados no ficheiro e enviar o ACK.
                            if (opcode == 3) {
                                atual++;
                                DataTransferPacket data = DataTransferPacket.deserialize(bis);
                                if (numBinicial + window > data.getNumBloco() && numBinicial <= data.getNumBloco()) {
                                        dtFiles.add(data.getNumBloco() - numBinicial, data);
                                }
                            }
                            // Se opcode == 5 temos um FINPacket. Enviamos um FINPacket de volta e dá mos exit.
                            if (opcode == 5) {
                                FINPacket fin = FINPacket.deserialise(bis);
                                byte fincode = fin.getFincode();
                                if (fincode == 1) {
                                    window = atual;
                                }
                                else {
                                    FINPacket finPacket = new FINPacket();
                                    byte[] packetToSend = s.addSecurityToPacket(finPacket.serialize());
                                    socket.send(new DatagramPacket(packetToSend, packetToSend.length,ip,port));
                                    i = 5;
                                }
                            }
                        }
                    }
                    if (i < 5) {
                        if (dtFiles.size() == window) {
                            for (DataTransferPacket dtp : dtFiles) {
                                fos.write(dtp.getData());
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
                                    fos.write(dtFiles.get(index).getData());
                                }
                            }
                            window = 1;
                            ACKPacket ack = new ACKPacket(numB);
                            byte[] outData = s.addSecurityToPacket(ack.serialize());
                            outPacket = new DatagramPacket(outData, outData.length,ip,port);
                        }
                    }
                }   
                catch (SocketTimeoutException e) {
                    i++;
                }
            }
            fos.close();
            ficheiro.setLastModified(Long.parseLong(fi.getTime()));
            socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
