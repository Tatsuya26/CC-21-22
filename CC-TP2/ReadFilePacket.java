import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ReadFilePacket {
    public static final byte opcode = 2;
    public String filename;

    public String getFileName() {
        return this.filename;
    }

    public ReadFilePacket(String nome) {
        this.filename = nome;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(ReadFilePacket.opcode);
        dos.writeBytes(this.filename);
        dos.writeByte(0);
        return bos.toByteArray();
    }

    public static ReadFilePacket deserialize(ByteArrayInputStream bis) throws IOException{
        DataInputStream dis = new DataInputStream(bis);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte b;
        while((b = dis.readByte()) != 0)
            bos.write(b);
        String filename = new String(bos.toByteArray());
        return new ReadFilePacket(filename);
    }
}
