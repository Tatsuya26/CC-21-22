import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FFSync {
    public static void main(String[] args) throws InterruptedException {
        if (args.length == 0) {
            System.out.println("Falta argumentos");
            return ;
        }
        String pasta = args[0];

        try {
            InetAddress[] ips = new InetAddress[args.length - 1];
            for (int i = 1 ; i < args.length; i++) 
            ips[i-1] = InetAddress.getByName(args[i]);
            File diretoria = new File(pasta);
            //ScheduledExecutorService sec = Executors.newScheduledThreadPool(1);
            TimerTask task = new FTRapidClient(diretoria, ips);
            Timer timer = new Timer(true);
            Thread serverUDP = new Thread(new FTRapidServer(diretoria,ips));
            Thread http = new Thread(new HTTPServer());
            serverUDP.start();
            http.start();
            timer.scheduleAtFixedRate(task, 0, 60000);
            serverUDP.join();
            http.join();
        }
        catch (IOException e) {
             e.printStackTrace();   
             
        }
        
            
    }
}
