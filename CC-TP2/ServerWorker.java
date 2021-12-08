import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

public class ServerWorker implements Runnable{
    public DatagramPacket received;
    public DatagramSocket socket;
    public File folder;
    public InetAddress[] ips;

    public ServerWorker(DatagramPacket received,File folder,InetAddress[] ips) {
        this.received = received;
        this.folder = folder;
        this.ips = ips;
        try {
            this.socket = new DatagramSocket();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            int port = this.received.getPort();
            InetAddress clientIP = this.received.getAddress();

            byte opcode1 = this.received.getData()[0];
            byte[] data = new byte[1300];
            if (opcode1 == 1) {
                DataTransferPacket dataPacket = getFileInfo();
                data = dataPacket.serialize();
                System.out.println("Recebido pedido de ficheiros");
            }
            
            DatagramPacket sendPacket = new DatagramPacket(data,data.length,clientIP,port);
            
            byte[] indata = new byte[1300];
            this.received = new DatagramPacket(indata, 1300);
            socket.setSoTimeout(1000);
            int i = 0;
            while (i < 25){
                try {
                    socket.send(sendPacket);
                    socket.receive(this.received);
                    ByteArrayInputStream bis = new ByteArrayInputStream(this.received.getData());
                    int opcode = bis.read();
                    if (opcode == 2) {
                        ReadFilePacket readFile = ReadFilePacket.deserialize(bis);
                        sendFile(readFile,clientIP,port);
                        System.out.println("Ficheiro " +readFile.getFileName() + " enviado com sucesso.");
                        FINPacket fin = new FINPacket();
                        sendPacket = new DatagramPacket(fin.serialize(), fin.serialize().length);
                    }

                    if (opcode == 5 || opcode == 6) {
                        System.out.println("Recebido pedido de fim de conexão");
                        FINPacket fin = new FINPacket();
                        sendPacket = new DatagramPacket(fin.serialize(), fin.serialize().length,clientIP,port);
                        socket.send(sendPacket);
                        i = 25;
                    }
                }
                catch (SocketTimeoutException e) {
                    i++;
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public DataTransferPacket getFileInfo() throws IOException{
        //Criar array com todos os ficheiros da diretoria;
        File[] subFicheiros = folder.listFiles();
        //Abrir stream onde escrevemos os bytes;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        //Percorremos o array dos ficheiros, vemos a sua informação e escrevemos no stream;
        for (File f  : subFicheiros) {
            BasicFileAttributes fa = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
            String filename = new String(folder.getName()).concat("/").concat(f.getName());
            FileInfo fi = new FileInfo(filename,Long.toString(fa.lastModifiedTime().toMillis()),Long.toString(fa.size()));
            bos.write('+');
            bos.write(fi.serialize());
        }
        // O byte 0 indica que não temos mais dados;
        bos.write(0);
        byte[] data = bos.toByteArray();
        return new DataTransferPacket(1,data.length, data);
    }

    public void sendFile(ReadFilePacket readFile,InetAddress clientIP,int port) throws IOException{
        File f = new File(readFile.getFileName());
        System.out.println("Recebido pedido de leitura para o ficheiro " + readFile.getFileName());
        FileInputStream fis = new FileInputStream(f);
        int numB = 1;
        while(fis.available() > 0) {
            byte[] fileData = fis.readNBytes(1293);
            DataTransferPacket dtFile = new DataTransferPacket(numB, fileData.length, fileData);
            DatagramPacket sendPacket = new DatagramPacket(dtFile.serialize(),dtFile.serialize().length,clientIP,port);
            boolean verificado = false;
            while (!verificado) {
                socket.send(sendPacket);
                byte[] indata = new byte[1300];
                DatagramPacket inPacket = new DatagramPacket(indata,1300);
                socket.receive(inPacket);
                ByteArrayInputStream bis = new ByteArrayInputStream(inPacket.getData());
                int opcode = bis.read();
                if (opcode == 6) {
                    ACKPacket ack = ACKPacket.deserialize(bis);
                    if (ack.getNumBloco() == numB) {
                        verificado = true;
                        numB++;
                    }
                }
            }
        }
        fis.close();
    }
}
