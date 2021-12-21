import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;

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
            Thread cliente = new Thread(new FTRapidClient(diretoria, ips));
            Thread serverUDP = new Thread(new FTRapidServer(diretoria,ips));
            cliente.start();
            serverUDP.start();
            cliente.join();
            serverUDP.join();

            ServerSocket socket = new ServerSocket(80);

            while (true) {
                Thread http = new Thread(new HTTPResponser(socket.accept()));
                http.start();
            }
            
        }
        catch (IOException e) {
             e.printStackTrace();   
             
        }
        
            
    }
}
