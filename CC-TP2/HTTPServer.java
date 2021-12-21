import java.io.IOException;
import java.net.ServerSocket;

public class HTTPServer implements Runnable{

    private ServerSocket socket;
    public HTTPServer() {
        try {
            socket = new ServerSocket(80);
        } catch (IOException e) {
        }
    }
    
    public void run() {
        try {
            socket = new ServerSocket(80);
            while (true) {
                Thread http = new Thread(new HTTPResponser(socket.accept()));
                http.start();
            }
        } catch (IOException e) {
        }
    }
}
