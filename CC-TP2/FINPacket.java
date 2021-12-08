import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FINPacket {
    public static final byte opcode = 5;

    public FINPacket () {};

    public byte[] serialize() throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(FINPacket.opcode);
        return bos.toByteArray();
    }
}
