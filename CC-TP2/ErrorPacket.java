import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ErrorPacket {
    public static final byte opcode = 4;

    public byte errorCode;
    public String errorDescription;

    public ErrorPacket(byte error,String descricao) {
        this.errorCode = error;
        this.errorDescription = descricao;
    }

    public byte getErrorCode() {
        return this.errorCode;
    }

    public String getErrorDescription() {
        return this.errorDescription;
    }

    public byte[] serialize() throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(ErrorPacket.opcode);
        dos.writeByte(this.errorCode);
        dos.writeUTF(errorDescription);
        return bos.toByteArray();
    }

    public static ErrorPacket deserialize(ByteArrayInputStream bis) throws IOException{
        DataInputStream dis = new DataInputStream(bis);
        byte error = dis.readByte();
        String descricao = dis.readUTF();
        return new ErrorPacket(error, descricao);
    }
}
