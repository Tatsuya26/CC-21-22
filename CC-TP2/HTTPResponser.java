import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;

public class HTTPResponser implements Runnable{
    
    private Socket canal;

    public HTTPResponser(Socket canal){
        this.canal = canal;
    }

    public void run() {
        try {
            BufferedReader brStream = new BufferedReader(new InputStreamReader(canal.getInputStream()));
            BufferedWriter bwStream = new BufferedWriter(new OutputStreamWriter(canal.getOutputStream()));

            String pedido;
            while ((pedido = brStream.readLine()) != null && pedido.isEmpty()) {
                System.out.println(pedido);
            }

            // TODO: Definir a mensagem que vamos responder no html
            // Resposta muito básica ainda
            bwStream.write("HTTP/1.1 200 OK\r\n");
            bwStream.write("Date: Fri, 13 Dec 23:59:59 GMT\r\n");
            bwStream.write("Content-Type: text/html\r\n");
            bwStream.write("Content-Length: 59\r\n");
            bwStream.write("Expires: Sat, 01 Jan 2000 00:59:59 GMT\r\n");
            bwStream.write("Last-modified: Fri, 09 Aug 1996 14:21:40 GMT\r\n");
            bwStream.write("\r\n");
            bwStream.write("<TITLE>Exemplo</TITLE>");
            bwStream.write("<h1>Estado</h1>");
            bwStream.write("<P>Aqui estará o estado do programa.</P>");
            bwStream.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
