import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

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
            TimerTask task = new FTRapidClient(diretoria, ips);
            Timer timer = new Timer(true);
            Thread serverUDP = new Thread(new FTRapidServer(diretoria,ips));
            Thread http = new Thread(new HTTPServer());
            serverUDP.start();
            http.start();
<<<<<<< HEAD
            timer.scheduleAtFixedRate(task, 0, 60000);
=======
            // Atualizar de 90 em 90 segundos
            timer.scheduleAtFixedRate(task, 0, 90000);
>>>>>>> 05b41582f6de46b52a755145d315154db941b606
            serverUDP.join();
            http.join();
        }
       catch (IOException e) {
             e.printStackTrace();   
             
        }          
    }

}
