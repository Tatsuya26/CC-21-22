import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;


public class Security {
    private HMac hmac;
    private final String key = "chave muito segura";

    public Security(){
        hmac = new HMac(new SHA1Digest());
    }

    private byte[] getMac(byte[] bytes){
        hmac.init(new KeyParameter(key.getBytes(StandardCharsets.UTF_8)));
        hmac.update(bytes,0,bytes.length);
        byte[] mac = new byte[hmac.getMacSize()];
        hmac.doFinal(mac,0);
        return mac;
    }

    private String getSignature(byte[] mac){
        return Base64.getEncoder().withoutPadding().encodeToString(mac);
    }

    public byte[] addSecurityToPacket (byte[] message){
        byte[] result = new byte[20 + message.length];

        byte[] mac = getMac(message);

        System.arraycopy(mac,0,result,0,20);
        System.arraycopy(message,0,result,20,message.length);

        return result;
    }

    public Boolean verifyPacketAuthenticity(byte[] packet){
        int size = packet.length;

        byte[] macR = Arrays.copyOfRange(packet,0,20);
        byte[] message = Arrays.copyOfRange(packet,20,size);

        byte[] mac = getMac(message);

        String sign = getSignature(mac);
        String signR = getSignature(macR);

        return sign.compareTo(signR) == 0;
    }

}
