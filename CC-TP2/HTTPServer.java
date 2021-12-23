import java.io.IOException;
import java.net.ServerSocket;

public class HTTPServer implements Runnable{

    private ServerSocket socket;
    public HTTPServer() {
        try {
            socket = new ServerSocket(8080);
        } catch (IOException e) {
        }
    }
    
    public void run() {
        try {
            while (true) {
                Thread http = new Thread(new HTTPResponser(socket.accept()));
                http.start();
            }
        } catch (IOException e) {
        }
    }
}
