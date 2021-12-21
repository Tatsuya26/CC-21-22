import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RQFileInfoPacket {
    private static final byte opcode = 1;
    
    public RQFileInfoPacket(){};

    public byte[] serialize() throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(RQFileInfoPacket.opcode);
        while (dos.size() < 1300) dos.writeByte(0);
        return bos.toByteArray();
    }
}
