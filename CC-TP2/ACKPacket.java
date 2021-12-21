import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ACKPacket {
    public static final byte opcode = 6;
    public short numBloco;

    public ACKPacket(int numBloco) {
        this.numBloco = (short) numBloco;
    }

    public int getNumBloco() {
        return this.numBloco;
    }

    public byte[] serialize() throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(ACKPacket.opcode);
        dos.writeShort(this.numBloco);
        while (dos.size() < 1300) dos.writeByte(0);
        return bos.toByteArray();
    }

    public static ACKPacket deserialize(ByteArrayInputStream bis) throws IOException{
        DataInputStream dis = new DataInputStream(bis);
        short nb = dis.readShort();
        return new ACKPacket(nb);
    }
}
