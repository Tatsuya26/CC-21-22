import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class HTTPResponser implements Runnable{
    
    private Socket canal;
    BufferedReader readLog;

    public HTTPResponser(Socket canal){
        this.canal = canal;
        try {
            this.readLog =  new BufferedReader(new FileReader(new File("Logs")));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public List<String> getFromLogs() {
        List<String> str_log = new ArrayList<>();
        String line = "";
        try {
            while ((line = this.readLog.readLine()) != null) str_log.add(line);
            for(int i = 0; i< str_log.size(); i++) System.out.println(str_log.get(i));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str_log;
    }

    public void run() {
        try {
            BufferedReader brStream = new BufferedReader(new InputStreamReader(canal.getInputStream()));
            BufferedWriter bwStream = new BufferedWriter(new OutputStreamWriter(canal.getOutputStream()));
            
            String pedido;
            List<String> log = getFromLogs(); 

            while ((pedido = brStream.readLine()) != null && !pedido.isEmpty()) {
                System.out.println(pedido);
            }   
        
            int size_log = 0;
            for(int i = 0; i< log.size(); i++) size_log += log.get(i).length();

            
            
            // TODO: Definir a mensagem que vamos responder no html
            // Resposta muito básica ainda
            bwStream.write("HTTP/1.1 200 OK\r\n");
            bwStream.write("Date: Fri, 13 Dec 23:59:59 GMT\r\n");
            bwStream.write("Content-Type: text/html\r\n");
            bwStream.write("Content-Length: \r\n" + (133 + size_log));
            bwStream.write("Expires: Sat, 01 Jan 2000 00:59:59 GMT\r\n");
            bwStream.write("Last-modified: Fri, 09 Aug 1996 14:21:40 GMT\r\n");
            bwStream.write("\r\n");
            bwStream.write("<!DOCTYPE html>");
            bwStream.write("<html>");
            bwStream.write("<head>");
            bwStream.write("<title>CC-TP2</title>");
            bwStream.write("</head>");
            bwStream.write("<h1> CC- TP2 </h1>");
            bwStream.write("<body>");
            bwStream.write("<P>Aqui estara o estado do programa.</P>");
            for(int i = 0; i < log.size(); i++) bwStream.write("<P>" + log.get(i) + "<P>");
            bwStream.write("</body>");
            bwStream.write("</html>");
            bwStream.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


}
