import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RQFileInfoPacket {
    public static final byte opcode = 1;
    
    public RQFileInfoPacket() {};

    public byte[] serialize() throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(RQFileInfoPacket.opcode);
        return bos.toByteArray();
    }
    
}
