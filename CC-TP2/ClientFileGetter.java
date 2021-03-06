import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
    public BufferedWriter myWriter;
    public BufferedWriter http_info;

    public void whenWriteStringUsingBufferedWritter_thenCorrect() throws IOException {
        this.myWriter = new BufferedWriter(new FileWriter("Logs",false));
        this.http_info = new BufferedWriter(new FileWriter("httpV2",false));
        this.myWriter.write("ClientFileGetter:\n");
        this.http_info.write("");
    }
    
    public ClientFileGetter(InetAddress ip,FileInfo fi,File f) {
        this.ip = ip;
        this.fi = fi;
        this.folder = f;
        this.s = new Security();
        try {
            whenWriteStringUsingBufferedWritter_thenCorrect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.window = 1;
        this.port = 0;
    }
    
    public void run() {
        try {
            long size = 0;
            long start = System.nanoTime();
            DatagramSocket socket = new DatagramSocket();
            String filename = fi.getName();
            //Criar Pacote para pedir o ficheiro fi ao servidor.
            ReadFilePacket readFile = new ReadFilePacket(filename);
            byte[] rfBytes = s.addSecurityToPacket(readFile.serialize());
            DatagramPacket outPacket = new DatagramPacket(rfBytes, rfBytes.length,ip,8080);
            int i = 0;
            // Resolver o nome do ficheiro para ficar na diretoria onde estamos a sincronizar. Neste caso a diretoria pai da dada nos parametros
            Path file = Path.of(filename);
            Path path = Path.of(folder.getAbsolutePath());
            file = path.resolve(file);
            Path parent = file.getParent();
            //Verificar se existe a diretoria pai. Se n??o existir criamos a pasta.
            File parentFile = parent.toFile();
            if (!parentFile.exists()) parentFile.mkdirs();
            //Verificar se o ficheiro j?? existe. Se n??o existir criamos o ficheiro.
            File ficheiro = file.toFile();
            if (!ficheiro.exists()) ficheiro.createNewFile();
            //Abrir stream para escrever no ficheiro.
            FileOutputStream fos = new FileOutputStream(ficheiro,false);
            socket.setSoTimeout(1000);
            int numB = 1;
            System.out.println("A pedir o ficheiro " + filename);
            this.myWriter.append("A pedir o ficheiro " + filename + "\n");
            this.http_info.append("A pedir ficheiro " +  filename + " do endereco" + ip.toString() + "\n");
            while (i < 5) {
                try {
                    socket.send(outPacket);
                    this.myWriter.append("Enviado ACK com o n??mero " + numB + "\n");
                    byte[] indata = new byte[1320];
                    DatagramPacket inPacket = new DatagramPacket(indata, 1320);
                    int atual = 0;
                    List<DataTransferPacket> dtFiles = new ArrayList<>();
                    for (int index = 0; index < window;index++) dtFiles.add(index,null);
                    int numBinicial = numB;
                    while (window > atual) {
                        socket.receive(inPacket);
                        i = 0;
                        this.port = inPacket.getPort();
                        
                        boolean authenticity = s.verifyPacketAuthenticity(inPacket.getData());
                        
                        if (authenticity) {
                            byte[] packet = inPacket.getData();
                            ByteArrayInputStream bis = new ByteArrayInputStream(Arrays.copyOfRange(packet,20,packet.length));
                            
                            //Ler o byte que indica o opcode
                            int opcode = bis.read();
                            // Se opcode == 3 temos um DataTransferPacket logo vamos escrever os dados no ficheiro e enviar o ACK.
                            if (opcode == 3) {
                                DataTransferPacket data = DataTransferPacket.deserialize(bis);
                                if (this.window != data.getWindow()) {
                                    dtFiles = new ArrayList<>();
                                    this.window = data.getWindow();
                                    for (int index = 0; index < window;index++) dtFiles.add(index,null);
                                    atual = 0;
                                }
                                if (numBinicial + window > data.getNumBloco() && numBinicial <= data.getNumBloco()) {
                                    if (dtFiles.get(data.getNumBloco() - numBinicial) == null) atual++;
                                    dtFiles.set(data.getNumBloco() - numBinicial, data);
                                }
                            }

                            if (opcode == 4) {
                                fos.close();
                                window = 0;
                                ficheiro.delete();
                                FINPacket finPacket = new FINPacket();
                                byte[] packetToSend = s.addSecurityToPacket(finPacket.serialize());
                                outPacket = new DatagramPacket(packetToSend, packetToSend.length,ip,port);
                            }
                            // Se opcode == 5 temos um FINPacket. Enviamos um FINPacket de volta e d?? mos exit.
                            if (opcode == 5) {
                                FINPacket finPacket = new FINPacket();
                                byte[] packetToSend = s.addSecurityToPacket(finPacket.serialize());
                                socket.send(new DatagramPacket(packetToSend, packetToSend.length,ip,port));
                                i = 5;
                                window = atual;
                            }
                        }
                    }
                    if (i < 5 && window != 0) {
                        List<DataTransferPacket> filesWindow = new ArrayList<>();
                        for (int index = 0; index < window;index++) filesWindow.add(index,null);
                        for (DataTransferPacket d : dtFiles) filesWindow.set(d.getNumBloco() - numBinicial, d);
                        for (int index = 0; index < filesWindow.size();index++) {
                            if (filesWindow.get(index) == null) {
                                index = filesWindow.size();
                            }
                            else {
                                fos.write(filesWindow.get(index).getData());
                                numB++;
                                size += filesWindow.get(index).getLengthData();
                            }
                        }
                        ACKPacket ack = new ACKPacket(numB);
                        byte[] outData = s.addSecurityToPacket(ack.serialize());
                        outPacket = new DatagramPacket(outData, outData.length,ip,port);
                    }
                }
                catch (SocketTimeoutException e) {
                    i++;
                }
            }
            fos.close();
            ficheiro.setLastModified(Long.parseLong(fi.getTime()));
            socket.close();
            long end = System.nanoTime();
            double time = (end - start) / (double) 1000000000;
            long bits = size*8;
            double debito = bits / time;
            System.out.println("Ficheiro "+ filename +" acabado de receber");
            System.out.println("Recebidos " + size + " bytes com um debito de "+ debito + " bps demorando " + time + " segundos");
            this.http_info.append("Recebidos " + size + " bytes com um debito de "+ debito + " bps demorando " + time + " segundos\n");
            this.myWriter.append("Ficheiro "+ filename +" acabado de receber\n");
            this.http_info.close();
            this.myWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
