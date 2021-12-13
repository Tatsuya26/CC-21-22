import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class FFSync {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Falta argumentos");
            return ;
        }
        String pasta = args[0];
        try{
            InetAddress[] ips = new InetAddress[args.length - 1];
            for (int i = 1 ; i < args.length; i++) 
                ips[i-1] = InetAddress.getByName(args[i]);
            File diretoria = new File(pasta);
            Thread cliente = new Thread(new FTRapidClient(diretoria, ips));
            Thread serverUDP = new Thread(new FTRapidServer(diretoria,ips));
            cliente.start();
            serverUDP.start();
            ServerSocket socket = new ServerSocket(80);
            while (true) {
                Thread http = new Thread(new HTTPResponser(socket.accept()));
                http.start();
            }
            //cliente.join();
            //serverUDP.join();
        }
        catch (IOException e) {
             e.printStackTrace();   
             
        }
            
    }
}
