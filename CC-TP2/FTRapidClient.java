import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class FTRapidClient  extends TimerTask{
    public final static int length = 1320;
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
            System.out.println("A iniciar processo de sincronizacao");
            Thread[] threads = new Thread[ips.length];
            int t = 0;
            for (InetAddress i : this.ips) {
                threads[t] = new Thread(new ClientFileRequester(i,this.ficheirosSincronizar));
                threads[t].start();
                t++;
            }

            for (Thread th : threads) th.join();

            List<FileInfo> fis = this.ficheirosSincronizar.getList();

            for (FileInfo f : fis) System.out.println(f.toString());

            boolean sincronizado = this.ficheirosSincronizar.isSincronizado();
            if (!sincronizado) {

                threads = new Thread[fis.size()];
                t = 0;
                for (FileInfo fi : fis) {
                    threads[t] = new Thread(new ClientFileGetter(fi.getIP(),fi,folder));
                    if (fi.getIP() != null) 
                        threads[t].start();
                    t++;
                    fi.setIP(null);
                }

                for (Thread th : threads) th.join();
            }
            System.out.println("Fim do processo de sincronizacao");
            
            
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
