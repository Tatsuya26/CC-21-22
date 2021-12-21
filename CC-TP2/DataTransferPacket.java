import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataTransferPacket {
    public static final byte opcode = 3;

    private short numeroBloco;
    private short lengthData;
    private byte[] data;

    public DataTransferPacket(int numBloco,int length,byte[] data) {
        this.numeroBloco = (short) numBloco;
        this.lengthData = (short) length;
        this.data = data;
    }

    public int getNumBloco() {
        return this.numeroBloco;
    }

    public short getLengthData() {
        return this.lengthData;
    }

    public byte[] getData() {
        return this.data;
    }

    public byte[] serialize() throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(DataTransferPacket.opcode);
        dos.writeShort(this.numeroBloco);
        dos.writeShort(this.data.length);
        dos.write(this.data);
        while (dos.size() < 1300) dos.writeByte(0);
        return bos.toByteArray();
    }

    public static DataTransferPacket deserialize(ByteArrayInputStream bis) throws IOException{
        DataInputStream dis = new DataInputStream(bis);
        short nB = dis.readShort();
        short length = dis.readShort();
        byte[] data = dis.readNBytes(length);
        return new DataTransferPacket(nB,length,data);
    }
}