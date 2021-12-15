import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RQFileInfoPacket {
    private static final byte opcode = 1;
    private String ficheiroASincronizar;
    
    public RQFileInfoPacket(String file) {
        this.ficheiroASincronizar = file;
    }

    public String getFileToSync() {
        return this.ficheiroASincronizar;
    }

    public byte[] serialize() throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(RQFileInfoPacket.opcode);
        dos.writeBytes(this.ficheiroASincronizar);
        return bos.toByteArray();
    }

    public static RQFileInfoPacket deserialise(ByteArrayInputStream bis) throws IOException{
        DataInputStream dis = new DataInputStream(bis);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte b;
        while((b = dis.readByte()) != 0)
            bos.write(b);
        String filename = new String(bos.toByteArray());
        return new RQFileInfoPacket(filename);
    }
    
}
