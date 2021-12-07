import java.io.ByteArrayOutputStream;

public class FINPacket {
    public static final byte opcode = 5;

    public FINPacket () {};

    public byte[] serialize() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(5);
        return bos.toByteArray();
    }
}
