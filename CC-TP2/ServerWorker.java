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
import java.nio.file.Path;
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
            socket.setSoTimeout(1000);
            int i = 0;
            int port = this.received.getPort();
            InetAddress clientIP = this.received.getAddress();
            System.out.println("Porta "+ port);
            while (i < 25){
                try {
                    ByteArrayInputStream bis = new ByteArrayInputStream(this.received.getData());
                    int opcode = bis.read();
                    if (opcode == 1) {
                        sendFileInfo(clientIP,port);
                        FINPacket fin = new FINPacket();
                        DatagramPacket outPacket = (new DatagramPacket(fin.serialize(),fin.serialize().length,clientIP,port));
                        socket.send(outPacket);
                    }
                    if (opcode == 2) {
                        ReadFilePacket readFile = ReadFilePacket.deserialize(bis);
                        sendFile(readFile,clientIP,port);
                        FINPacket fin = new FINPacket();
                        DatagramPacket outPacket = (new DatagramPacket(fin.serialize(),fin.serialize().length,clientIP,port));
                        socket.send(outPacket);
                    }
                    
                    //TODO: Meter os FIN a terminar o programar. Criar uma flag para ver se já recebemos ou enviamos um FIN.
                    if (opcode == 5) {
                        i = 25;
                    }
                    byte[] indata = new byte[1300];
                    this.received = new DatagramPacket(indata,1300);
                    socket.receive(this.received);
                }
                catch (SocketTimeoutException e) {
                    i++;
                }
                catch (IOException e) {
                    i = 25;
                }
            }
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void sendFileInfo(InetAddress ip,int port) throws IOException{
        //Criar array com todos os ficheiros da diretoria;
        File[] subFicheiros = folder.listFiles();
        //Abrir stream onde escrevemos os bytes;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int numB = 1;
        //Percorremos o array dos ficheiros, vemos a sua informação e escrevemos no stream;
        for (File f  : subFicheiros) {
            BasicFileAttributes fa = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
            String filename = f.getAbsolutePath();
            Path file = Path.of(filename);
            Path parent = file.getParent().getParent();
            file = parent.relativize(file);
            FileInfo fi = new FileInfo(file.toString(),Long.toString(fa.lastModifiedTime().toMillis()));
            if ((bos.size() + fi.serialize().length + 1) > 1292) {
                bos.write(0);
                byte[] data = bos.toByteArray();
                DataTransferPacket fileInfos = new DataTransferPacket(numB++, data.length, data);
                sendDataPacket(fileInfos, ip, port);
                bos.close();
                bos = new ByteArrayOutputStream();
            }
            bos.write('+');
            bos.write(fi.serialize());
        }
        // O byte 0 indica que não temos mais dados;
        bos.write(0);
        byte[] data = bos.toByteArray();
        DataTransferPacket fileInfos = new DataTransferPacket(numB++, data.length, data);
        sendDataPacket(fileInfos, ip, port);
    }

    public void sendFile(ReadFilePacket readFile,InetAddress clientIP,int port) throws IOException{
        String f = readFile.getFileName();
        Path file = Path.of(folder.getAbsolutePath()).getParent().getParent().resolve(f);
        System.out.println("A enviar o ficheiro " + file.toString());
        FileInputStream fis = new FileInputStream(file.toFile());
        int numB = 1;
        System.out.println("Enviar bloco numero "+ numB);
        while(fis.available() > 0) {
            byte[] fileData = fis.readNBytes(1293);
            DataTransferPacket dtFile = new DataTransferPacket(numB++, fileData.length, fileData);
            sendDataPacket(dtFile, clientIP, port);
        }
        System.out.println("Ficheiro acabado de enviar");
        fis.close();
    }

    public void sendDataPacket (DataTransferPacket data,InetAddress ip, int port) throws IOException{
        boolean verificado = false;
        int i = 0;
        socket.setSoTimeout(1000);
        while (i < 5) {
            try {
                while (!verificado) {
                    socket.send(new DatagramPacket(data.serialize(), data.serialize().length,ip,port));
                    byte[] indata = new byte[1300];
                    DatagramPacket inPacket = new DatagramPacket(indata,1300);
                    socket.receive(inPacket);
                    ByteArrayInputStream bis = new ByteArrayInputStream(inPacket.getData());
                    int opcode = bis.read();
                    if (opcode == 6) {
                        ACKPacket ack = ACKPacket.deserialize(bis);
                        if (ack.getNumBloco() == data.getNumBloco()) {
                            verificado = true;
                        }
                    }
                }
            }
            catch (SocketTimeoutException e) {
                i++;
            }
        }
    }

    public void interpretadorPacket () throws IOException{
        ByteArrayInputStream bis = new ByteArrayInputStream(this.received.getData());
        int port = this.received.getPort();
        InetAddress clientIP = this.received.getAddress();
        int opcode = bis.read();
        if (opcode == 1) {
            sendFileInfo(clientIP,port);
            FINPacket fin = new FINPacket();
            socket.send(new DatagramPacket(fin.serialize(),fin.serialize().length,clientIP,port));
        }
        if (opcode == 2) {
            ReadFilePacket readFile = ReadFilePacket.deserialize(bis);
            sendFile(readFile,clientIP,port);
            FINPacket fin = new FINPacket();
            socket.send(new DatagramPacket(fin.serialize(),fin.serialize().length,clientIP,port));
        }
        
        //TODO: Meter os FIN a terminar o programar. Criar uma flag para ver se já recebemos ou enviamos um FIN.
        if (opcode == 5) {
            throw new IOException();
        }
    }
}
