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
            //Criar Pacote para pedir o ficheiro fi ao servidor.
            ReadFilePacket readFile = new ReadFilePacket(filename);
            DatagramPacket outPacket = new DatagramPacket(readFile.serialize(), readFile.serialize().length,ip,80);
            int i = 0;
            // Resolver o nome do ficheiro para ficar na diretoria onde estamos a sincronizar. Neste caso a diretoria pai da dada nos parametros
            Path file = Path.of(filename);
            Path path = Path.of(folder.getAbsolutePath()).getParent();
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
                    byte[] indata = new byte[1300];
                    DatagramPacket inPacket = new DatagramPacket(indata, 1300);
                    socket.receive(inPacket);
                    int port = inPacket.getPort();
                    ByteArrayInputStream bis = new ByteArrayInputStream(inPacket.getData());
                    //Ler o byte que indica o opcode
                    int opcode = bis.read();
                    // Se opcode == 3 temos um DataTransferPacket logo vamos escrever os dados no ficheiro e enviar o ACK.
                    if (opcode == 3) {
                        DataTransferPacket data = DataTransferPacket.deserialize(bis);
                        ACKPacket ack = new ACKPacket(numB);
                        if (numB == data.getNumBloco()) {
                            fos.write(data.getData(),0,data.getLengthData());
                            numB++;
                        }
                        outPacket = new DatagramPacket(ack.serialize(),ack.serialize().length,ip,port);
                    }
                    // Se opcode == 5 temos um FINPacket. Enviamos um FINPacket de volta e dá mos exit.
                    if (opcode == 5) {
                        FINPacket fin = new FINPacket();
                        socket.send(new DatagramPacket(fin.serialize(), fin.serialize().length,ip,port));
                        i = 25;
                    }
                }
                catch (SocketTimeoutException e) {
                    i++;
                }
            }
            fos.close();
            socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
