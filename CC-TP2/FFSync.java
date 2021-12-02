import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

public class FFSync {
    public static void main(String[] args) {
        String pasta = args[0];
        try{
            InetAddress[] ips = new InetAddress[args.length - 1];
            for (int i = 1 ; i < args.length; i++) 
                ips[i-1] = InetAddress.getByName(args[i]);
        }
        catch (IOException e) {
             e.printStackTrace();   
        }
            
        File diretoria = new File(pasta);
        if (diretoria.exists()) {
        File[] ficheiros = diretoria.listFiles();

        System.out.println("Ficheiros a sincronizar");
        for (File f:ficheiros) {
            System.out.println(f.getName());
        }
        }
        else System.out.println("Pasta não existe");
    }
}
