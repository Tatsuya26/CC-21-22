import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FINPacket {
    public static final byte opcode = 5;
    private byte fincode;

    public FINPacket () {
        this.fincode = 0;
    }

    public FINPacket (byte fin) {
        this.fincode = fin;
    };

    public byte getFincode() {
        return this.fincode;
    }

    public byte[] serialize() throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(FINPacket.opcode);
        dos.writeByte(this.fincode);
        while (dos.size() < 1300) dos.writeByte(0);
        return bos.toByteArray();
    }

    public static FINPacket deserialise(ByteArrayInputStream bis) throws IOException{
        DataInputStream dis = new DataInputStream(bis);
        byte fin = dis.readByte();
        return new FINPacket(fin);
    }
}
