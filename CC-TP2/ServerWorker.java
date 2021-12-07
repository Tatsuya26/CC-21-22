import java.io.ByteArrayOutputStream;
import java.io.File;
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

            byte opcode = this.received.getData()[0];
            byte[] data = new byte[1300];
            if (opcode == 1) data = getFileInfo();
            
            DatagramPacket sendPacket = new DatagramPacket(data,data.length,clientIP,port);
            
            byte[] indata = new byte[1300];
            this.received = new DatagramPacket(indata, 1300);
            socket.setSoTimeout(1000);
            int i = 0;
            while (i < 25){
                try {
                    socket.send(sendPacket);
                    socket.receive(this.received);
                    i = 25;
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


    public byte[] getFileInfo() throws IOException{
        //Criar array com todos os ficheiros da diretoria;
        File[] subFicheiros = folder.listFiles();
        //Abrir stream onde escrevemos os bytes;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        //Percorremos o array dos ficheiros, vemos a sua informação e escrevemos no stream;
        for (File f  : subFicheiros) {
            BasicFileAttributes fa = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
            FileInfo fi = new FileInfo(f.getName(),Long.toString(fa.lastModifiedTime().toMillis()),Long.toString(fa.size()));
            bos.write('+');
            bos.write(fi.serialize());
        }
        // O byte 0 indica que não temos mais dados;
        bos.write(0);
        return bos.toByteArray();
    }
}
