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
import java.util.Arrays;

public class ClientFileGetter implements Runnable{
    public InetAddress ip;
    public FileInfo fi;
    public File folder;
    public Security s;
    public BufferedWriter myWriter;

    public void whenWriteStringUsingBufferedWritter_thenCorrect() throws IOException {
        this.myWriter = new BufferedWriter(new FileWriter("Logs",true));
    }
    
    public ClientFileGetter(InetAddress ip,FileInfo fi,File f) {
        this.ip = ip;
        this.fi = fi;
        this.folder = f;
        this.s = new Security();
        try {
            whenWriteStringUsingBufferedWritter_thenCorrect();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
            this.myWriter.append("A pedir o ficheiro " + filename + "\n");
            while (i < 5) {
                try {
                    socket.send(outPacket);
                    byte[] indata = new byte[1320];
                    DatagramPacket inPacket = new DatagramPacket(indata, 1320);
                    socket.receive(inPacket);
                    int port = inPacket.getPort();

                    Security s = new Security();
                    boolean authenticity = s.verifyPacketAuthenticity(inPacket.getData());

                    if (authenticity) {
                        byte[] packet = inPacket.getData();
                        ByteArrayInputStream bis = new ByteArrayInputStream(Arrays.copyOfRange(packet,20,packet.length));

                        //Ler o byte que indica o opcode
                        int opcode = bis.read();
                        System.out.println(opcode);
                        // Se opcode == 3 temos um DataTransferPacket logo vamos escrever os dados no ficheiro e enviar o ACK.
                        if (opcode == 3) {
                            DataTransferPacket data = DataTransferPacket.deserialize(bis);
                            ACKPacket ack = new ACKPacket(numB);
                            if (numB == data.getNumBloco()) {
                                fos.write(data.getData(),0,data.getLengthData());
                                numB++;
                            }
                            byte[] packetToSend = s.addSecurityToPacket(ack.serialize());
                            outPacket = new DatagramPacket(packetToSend,packetToSend.length,ip,port);
                        }
                        // Se opcode == 5 temos um FINPacket. Enviamos um FINPacket de volta e dá mos exit.
                        if (opcode == 5) {
                            FINPacket fin = new FINPacket();
                            byte[] packetToSend = s.addSecurityToPacket(fin.serialize());
                            socket.send(new DatagramPacket(packetToSend, packetToSend.length,ip,port));
                            i = 25;
                        }
                    }
                }   
                catch (SocketTimeoutException e) {
                    i++;
                }
            }
            fos.close();
            ficheiro.setLastModified(Long.parseLong(fi.getTime()));
            this.myWriter.close();
            socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
