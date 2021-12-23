import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class HTTPResponser implements Runnable{
    
    private Socket canal;
    BufferedReader httpV1;
    BufferedReader httpV2;


    public HTTPResponser(Socket canal) {
        this.canal = canal;
        try {
            this.httpV1 =  new BufferedReader(new FileReader(new File("http")));
        } catch (FileNotFoundException e) {
            try {
                BufferedWriter httpV1 = new BufferedWriter(new FileWriter("http",true));
                httpV1.write("Nenhum ficheiro foi enviado.");
                httpV1.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        
        try {
            this.httpV2 =  new BufferedReader(new FileReader(new File("httpV2")));
        } catch (FileNotFoundException e) {
            try {
                BufferedWriter httpV2 = new BufferedWriter(new FileWriter("httpV2",true));
                httpV2.write("Nenhum ficheiro foi recebido");
                httpV2.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public List<String> getFromLogs() {
        List<String> str_log = new ArrayList<>();
        String line = "";
        try {
            while ((line = this.httpV1.readLine()) != null) str_log.add(line);
            str_log.add("\n\n");
            while ((line = this.httpV2.readLine()) != null) str_log.add(line);
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
            bwStream.close();
            brStream.close();
        } catch (IOException e) {
       
            e.printStackTrace();
        }

    }


}
