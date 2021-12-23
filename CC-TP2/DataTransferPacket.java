import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataTransferPacket {
    public static final byte opcode = 3;

    private int numeroBloco;
    private short lengthData;
    private short window;
    private byte[] data;

    public DataTransferPacket(int numBloco,int length,int window,byte[] data) {
        this.numeroBloco = numBloco;
        this.lengthData = (short) length;
        this.window = (short) window;
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

    public int getWindow() {
        return this.window;
    }

    public void setWindow(int win) {
        this.window = (short) win;
    }

    public byte[] serialize() throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(DataTransferPacket.opcode);
        dos.writeInt(this.numeroBloco);
        dos.writeShort(this.data.length);
        dos.writeShort(this.window);
        dos.write(this.data);
        while (dos.size() < 1300) dos.writeByte(0);
        return bos.toByteArray();
    }

    public static DataTransferPacket deserialize(ByteArrayInputStream bis) throws IOException{
        DataInputStream dis = new DataInputStream(bis);
        int nB = dis.readInt();
        short length = dis.readShort();
        short window = dis.readShort();
        byte[] data = dis.readNBytes(length);
        return new DataTransferPacket(nB,length,window,data);
    }
}
