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
import java.util.List;

public class FTRapidClient implements Runnable{
    public final static int length = 1300;
    public File folder;
    public InetAddress[] ips;
    public ArmazemFicheiro ficheirosSincronizar;

    public FTRapidClient(File folder,InetAddress[] ips) {
        this.folder = folder;
        this.ips = ips;
        this.ficheirosSincronizar = new ArmazemFicheiro(this.folder);
    }

    public void run() {
        try{
            Thread[] threads = new Thread[ips.length];
            int t = 0;
            for (InetAddress i : this.ips) {
                threads[t] = new Thread(new ClientFileRequester(i,this.ficheirosSincronizar,this.folder));
                threads[t].start();
                t++;
            }

            for (Thread th : threads) th.join();

            List<FileInfo> fis = this.ficheirosSincronizar.getList();

            threads = new Thread[fis.size()];
            t = 0;
            for (FileInfo fi : fis) {
                threads[t] = new Thread(new ClientFileGetter(fi.getIP(),fi,folder));
                if (fi.getIP() != null) 
                    threads[t].start();
                t++;
            }
            
            for (Thread th : threads) th.join();
        }
        catch (InterruptedException e) {
             e.printStackTrace();
        }
    }

    public List<FileInfo> readFileInfos (DataTransferPacket data) throws IOException{
        //Abrimos stream para leitura do array de bytes vindo do packet;
        ByteArrayInputStream bis = new ByteArrayInputStream(data.getData());
        // Lista onde armazenamos a informação dos ficheiros;
        List<FileInfo> fis = new ArrayList<>();
        //Lemos até ao byte 0;
        while (bis.read() != 0) {
            FileInfo fi = FileInfo.deserialize(bis);
            fis.add(fi);
        }
        return fis;
    }

}
