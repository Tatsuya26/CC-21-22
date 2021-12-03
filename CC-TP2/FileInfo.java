import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FileInfo {
    public String name;
    public String time;
    public String size;

    public FileInfo(String nome,String lastMod, String tamanho) {
        this.name = nome;
        this.time = lastMod;
        this.size = tamanho;
    }

    public String getName () {
        return this.name;
    }

    public String getTime() {
        return this.time;
    }

    public String getSize() {
        return this.size;
    }

    public byte[] serialize() throws IOException{
        byte[] info = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(this.name.getBytes().length);
        dos.writeBytes(this.name);
        dos.writeInt(time.length());
        dos.writeBytes(time);
        dos.writeInt(size.length());
        dos.writeBytes(size);
        info = bos.toByteArray();
        return info;
    }

    public static FileInfo deserialize(ByteArrayInputStream bis) throws IOException{
        DataInputStream dis = new DataInputStream(bis);
        int length = dis.readInt();
        String name = new String(dis.readNBytes(length));
        length = dis.readInt();
        String millis = new String(dis.readNBytes(length));
        length = dis.readInt();
        String size = new String(dis.readNBytes(length));
        return new FileInfo(name, millis, size);
    }

    public int compareFiles(FileInfo f) {
        if (this.name.compareTo(f.getName()) == 0) 
            if (this.time.compareTo(f.getTime()) == 0)
                if (this.size.compareTo(f.getSize()) == 0)
                    return 0;
        return -1;
    }

    public String toString(){
        return "Nome do ficheiro: " + this.name +"; Ultima alteracao: " + this.time + "; Tamanho do ficheiro: " + this.size; 
    }
}
