import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

public class FileInfo {
    private String name;
    private String time;
    private InetAddress ip;


    public FileInfo(String nome,String lastMod) {
        this.name = nome;
        this.time = lastMod;
    }

    public String getName () {
        return this.name;
    }

    public String getTime() {
        return this.time;
    }


    public InetAddress getIP() {
        return this.ip;
    }

    public void setFileName (String f) {
        this.name = f;
    }
    public void setIP(InetAddress inet) {
        this.ip = inet;
    }

    public byte[] serialize() throws IOException{
        byte[] info = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(this.name.getBytes().length);
        dos.writeBytes(this.name);
        dos.writeInt(time.length());
        dos.writeBytes(time);
        dos.flush();
        info = bos.toByteArray();
        return info;
    }

    public static FileInfo deserialize(ByteArrayInputStream bis) throws IOException{
        DataInputStream dis = new DataInputStream(bis);
        int length = dis.readInt();
        String name = new String(dis.readNBytes(length));
        length = dis.readInt();
        String millis = new String(dis.readNBytes(length));
        return new FileInfo(name, millis);
    }

    public String toString(){
        return "Nome do ficheiro: " + this.name +"; Ultima alteracao: " + this.time + "; IP : " + this.ip; 
    }

    public long compareTo(FileInfo f) {
        if (this.name.compareTo(f.getName()) == 0) 
            if (this.time.compareTo(f.getTime()) == 0) 
                return 0;
            else return Long.parseLong(this.time) - Long.parseLong(f.getTime());
        return -1;
    }
}
